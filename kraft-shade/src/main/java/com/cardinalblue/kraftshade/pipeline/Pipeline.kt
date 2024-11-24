package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.SampledInput
import com.cardinalblue.kraftshade.pipeline.input.TextureReferenceInput
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.util.KraftLogger

/**
 * This will be renamed to Pipeline later
 * Implementation will be added based on future instructions
 */
class Pipeline internal constructor(
    internal val glEnv: GlEnv,
    internal val bufferPool: TextureBufferPool,
    internal val automaticRecycle: Boolean = true,
) : EffectExecution {
    private val sampledInputs: MutableSet<SampledInput<*>> = mutableSetOf()
    private val steps: MutableList<PipelineStep> = mutableListOf()
    val stepCount: Int get() = steps.size

    /**
     * Used for tracking the index of the last step using a [BufferReference]
     */
    private val bufferReferenceUsage = mutableMapOf<BufferReference, Int>()
    
    private val postponedTasks: MutableList<suspend GlEnv.() -> Unit> = mutableListOf()

    protected fun runDeferred(block: suspend GlEnv.() -> Unit) {
        postponedTasks.add(block)
    }

    private fun trackInputUsage(input: Input<*>, stepIndex: Int) {
        when (input) {
            is TextureReferenceInput -> {
                val bufferReference = input.bufferReference
                bufferReferenceUsage[bufferReference] = stepIndex
                logger.d("[BufferedReference] ${bufferReference.nameForDebug} is used at step $stepIndex")
            }

            // for sampling all the sampled input at the beginning of the frame, so stepIndex is
            // not important here
            is SampledInput<*> -> {
                sampledInputs.add(input)
            }
        }
    }

    private fun recycleUnusedBuffers(currentStep: Int) {
        val buffersToRecycle = bufferReferenceUsage.entries
            .filter { (_, lastUsedStepIndex) -> lastUsedStepIndex == currentStep }
            .map { it.key }
            .toTypedArray()

        if (buffersToRecycle.isNotEmpty()) {
            bufferPool.recycle(*buffersToRecycle, afterStepIndex = currentStep)
            buffersToRecycle.forEach { bufferReferenceUsage.remove(it) }
        }
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
        // Track input usage for this step, the step index is [steps.size] since this step is not
        // added yet otherwise it would be [steps.size - 1]
        inputs.forEach { input -> trackInputUsage(input, steps.size) }

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

            // Recycle buffers that won't be used anymore
            if (automaticRecycle) {
                recycleUnusedBuffers(step.stepIndex)
            }
        }
        bufferPool.recycleAll()
        logger.d("the pool size is ${bufferPool.poolSize} after execution")
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
