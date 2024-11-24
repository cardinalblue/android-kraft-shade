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
    internal val glEnv: GlEnv,
    internal val bufferPool: TextureBufferPool,
) : EffectExecution {
    private val sampledInputs: MutableSet<SampledInput<*>> = mutableSetOf()
    private val steps: MutableList<PipelineStep> = mutableListOf()
    val stepCount: Int get() = steps.size

    private val postponedTasks: MutableList<suspend GlEnv.() -> Unit> = mutableListOf()

    protected fun runDeferred(block: suspend GlEnv.() -> Unit) {
        postponedTasks.add(block)
    }

    fun addStep(step: PipelineStep) {
        steps.add(step)
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

        RunShaderStep(
            stepIndex = steps.size,
            shader = shader,
            inputs = inputs,
            targetBuffer = targetBuffer,
            setupAction = setupAction,
        ).let(this::addStep)
    }

    override suspend fun run() {
        with(glEnv) { runPostponedTasks() }

        // Mark all sampled inputs as dirty at the start of the frame
        sampledInputs.forEach { it.markDirty() }
        sampledInputs.forEach { it.get() }

        logger.d("run $stepCount steps")
        steps.forEach { step ->
            step.run()
            logger.d {
                when (step) {
                    is RunShaderStep<*> -> "step [${step.type}:${step.stepIndex}] done with ${step.shader::class.simpleName}"
                    is RunTaskStep -> "step [${step.type}:${step.stepIndex}] for [${step.purposeForDebug}] done"
                }
            }
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
