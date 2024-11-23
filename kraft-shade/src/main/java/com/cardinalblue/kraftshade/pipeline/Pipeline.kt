package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.SampledInput
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.util.KraftLogger

/**
 * This will be renamed to Pipeline later
 * Implementation will be added based on future instructions
 *
 * TODO:
 * [ ] Recycle buffers
 * [ ] Sampled inputs dirty mechanism
 */
class Pipeline internal constructor(
    private val glEnv: GlEnv,
    internal val bufferPool: TextureBufferPool,
) : EffectExecution {
    private val sampledInputs: MutableSet<SampledInput<*>> = mutableSetOf()
    private val steps: MutableList<PipelineStep<*>> = mutableListOf()
    val stepCount: Int get() = steps.size

    private val postponedTasks: MutableList<suspend GlEnv.() -> Unit> = mutableListOf()

    protected fun runDeferred(block: suspend GlEnv.() -> Unit) {
        postponedTasks.add(block)
    }

    fun <T : KraftShader> addStep(
        shader: T,
        inputs: List<Input<*>> = emptyList(),
        targetBuffer: GlBufferProvider,
        setupAction: suspend T.(List<Input<*>>) -> Unit = {},
    ) {
        inputs
            .filterIsInstance<SampledInput<*>>()
            .forEach { sampledInputs.add(it) }

        steps.add(
            PipelineStep(
                stepIndex = steps.size,
                shader = shader,
                inputs = inputs,
                targetBuffer = targetBuffer,
                setupAction = setupAction,
            )
        )
    }

    override suspend fun run() {
        with(glEnv) { runPostponedTasks() }

        // first sample all the inputs. for the steps setup if they use the same input, they will
        // get the same value instead of getting from source of truth which may change while one
        // frame is not done yet.
        sampledInputs.forEach { it.sample() }

        logger.d("run $stepCount steps")
        steps.forEach { step ->
            step.run()
            logger.d("step ${step.stepIndex} done")
        }
    }

    private suspend fun GlEnv.runPostponedTasks() {
        postponedTasks.forEach { it() }
        postponedTasks.clear()
    }

    override suspend fun destroy() {
        logger.d("destroy")
        postponedTasks.clear()
    }

    override suspend fun onBufferSizeChanged(size: GlSize) {
        bufferPool.changeSize(size)
        logger.d("buffer size changed to $size")
    }

    private companion object {
        val logger = KraftLogger("Pipeline")
    }
}
