package com.cardinalblue.kraftshade.pipeline

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.env.ProtectedGlEnv
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.SampledInput
import com.cardinalblue.kraftshade.shader.KraftShader

abstract class Pipeline(
    private val glEnv: GlEnv,
) {
    private val sampledInputs = mutableListOf<SampledInput<*>>()
    private val sampledInputSetupActions = mutableListOf<() -> Unit>()

    private val mutex = Mutex()
    private val postponedTasks: MutableList<suspend GlEnv.(ProtectedGlEnv) -> Unit> = mutableListOf()

    abstract suspend fun GlEnv.execute(protectedGlEnv: ProtectedGlEnv)
    abstract suspend fun GlEnv.destroy(protectedGlEnv: ProtectedGlEnv)

    fun <T : Any, IN : Input<T>, S : KraftShader> connectInput(
        input: IN,
        shader: S,
        sampledFromExternal: Boolean = false,
        action: (IN, S) -> Unit
    ) {
        if (!sampledFromExternal && input is SampledInput<*>) {
            sampledInputs.add(input)
        }

        sampledInputSetupActions.add {
            action(input, shader)
        }
    }

    suspend fun run() {
        glEnv.use {
            internalRun()
        }
    }

    suspend fun destroy() {
        glEnv.use { destroy(it) }
    }

    protected suspend fun runDeferred(block: suspend GlEnv.(ProtectedGlEnv) -> Unit) {
        mutex.withLock {
            postponedTasks.add(block)
        }
    }

    private suspend fun GlEnv.runPostponedTasks(env: ProtectedGlEnv) {
        mutex.withLock {
            postponedTasks.forEach {
                it(env)
            }
            postponedTasks.clear()
        }
    }

    private suspend fun internalRun() {
        sampledInputs.forEach { it.sample() }
        sampledInputSetupActions.forEach { it() }
        glEnv.use {
            runPostponedTasks(it)
            execute(it)
        }
    }
}
