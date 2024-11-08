package com.cardinalblue.kraftshade.pipeline

import androidx.annotation.CallSuper
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.env.ProtectedGlEnv
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.SampledInput
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer

abstract class Pipeline(
    private val glEnv: GlEnv,
) {
    private val sampledInputs = mutableListOf<SampledInput<*>>()
    private val sampledInputSetupActions = mutableListOf<() -> Unit>()


    protected var targetBuffer: GlBuffer? = null
        private set

    private val mutex = Mutex()
    private val postponedTasks: MutableList<suspend GlEnv.(ProtectedGlEnv) -> Unit> = mutableListOf()

    abstract suspend fun GlEnv.execute(protectedGlEnv: ProtectedGlEnv)
    abstract suspend fun GlEnv.destroy(protectedGlEnv: ProtectedGlEnv)

    @CallSuper
    open fun setTargetBuffer(buffer: GlBuffer)  {
        targetBuffer = buffer
    }

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
        targetBuffer = null
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
