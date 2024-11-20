package com.cardinalblue.kraftshade.pipeline

import androidx.annotation.CallSuper
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.SampledInput
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class Pipeline(
    private val glEnv: GlEnv,
) : Effect {
    private val sampledInputs = mutableListOf<SampledInput<*>>()
    private val sampledInputSetupActions = mutableListOf<() -> Unit>()


    protected var targetBuffer: GlBuffer? = null
        private set

    private val mutex = Mutex()
    private val postponedTasks: MutableList<suspend GlEnv.() -> Unit> = mutableListOf()

    abstract suspend fun GlEnv.execute()
    abstract suspend fun GlEnv.destroy()

    @CallSuper
    open fun setTargetBuffer(buffer: GlBuffer)  {
        targetBuffer = buffer
    }

    fun <T : Any, IN : Input<T>, E : Effect> connectInput(
        input: IN,
        shader: E,
        sampledFromExternal: Boolean = false,
        action: (IN, E) -> Unit
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

    override suspend fun destroy() {
        targetBuffer = null
        glEnv.use { destroy() }
    }

    protected suspend fun runDeferred(block: suspend GlEnv.() -> Unit) {
        mutex.withLock {
            postponedTasks.add(block)
        }
    }

    private suspend fun GlEnv.runPostponedTasks() {
        mutex.withLock {
            postponedTasks.forEach {
                it()
            }
            postponedTasks.clear()
        }
    }

    @CallSuper
    open suspend fun internalRun() {
        sampledInputs.forEach { it.sample() }
        sampledInputSetupActions.forEach { it() }
        glEnv.use {
            runPostponedTasks()
            execute()
        }
    }
}
