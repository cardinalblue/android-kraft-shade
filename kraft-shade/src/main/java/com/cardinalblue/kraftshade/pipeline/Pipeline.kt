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
    private val sampledInputs = mutableSetOf<SampledInput<*>>()
    private val inputSetupActions = mutableListOf<() -> Unit>()


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

        inputSetupActions.add {
            action(input, shader)
        }
    }

    suspend fun run() {
        glEnv.use {
            internalRun()
        }
    }

    /**
     * Only call this method when input texture is set. You can just set the input texture once if
     * it doesn't change at all, and then call this function to render the pipeline
     */
    override suspend fun drawTo(buffer: GlBuffer) {
        setTargetBuffer(buffer)
        run()
    }

    @CallSuper
    override suspend fun destroy() {
        targetBuffer = null
        mutex.withLock {
            postponedTasks.clear()
        }
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
        inputSetupActions.forEach { it() }
        glEnv.use {
            env.runPostponedTasks()
            env.execute()
        }
    }
}
