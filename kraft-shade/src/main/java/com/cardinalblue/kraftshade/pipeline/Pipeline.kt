package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.dsl.KraftShadeDsl
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.SampledInput
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer
import com.cardinalblue.kraftshade.util.KraftLogger
import com.cardinalblue.kraftshade.util.SuspendAutoCloseable

/**
 * This will be renamed to Pipeline later
 * Implementation will be added based on future instructions
 */
@KraftShadeDsl
class Pipeline internal constructor(
    internal val glEnv: GlEnv,
    internal val bufferPool: TextureBufferPool,
    internal val automaticBufferRecycle: Boolean = true,
    internal val automaticShaderRecycle: Boolean = true,
    internal val automaticTextureRecycle: Boolean = true,
) : EffectExecution {
    private val sampledInputs: MutableSet<SampledInput<*>> = mutableSetOf()
    private val _steps: MutableList<PipelineStep> = mutableListOf()
    internal val steps: List<PipelineStep> get() = _steps
    val stepCount: Int get() = _steps.size

    internal val runContext = PipelineRunContext()

    var onDebugAfterShaderStep: ((PipelineRunContext) -> Unit)? = null

    /**
     * Used for tracking the index of the last step using a [BufferReference]
     */
    private val bufferReferenceUsage = mutableMapOf<BufferReference, Int>()

    private val postponedTasks: MutableList<suspend GlEnv.() -> Unit> = mutableListOf()

    /**
     * Destroy all the child pipelines when the parent pipeline is destroyed
     */
    private val childPipelines: MutableList<Pipeline> = mutableListOf()
    private val childTextureBuffers: MutableList<TextureBuffer> = mutableListOf()
    
    /**
     * Track all shaders for proper cleanup
     */
    private val shaders: MutableSet<KraftShader> = mutableSetOf()

    internal val pipelineRunningScope = PipelineRunningScope(this)

    init {
        logger.d("initialized with buffer pool buffer size ${bufferPool.bufferSize}")
    }

    fun createIntermediateTextureBuffer(glSize: GlSize): TextureBuffer {
        return TextureBuffer(glSize).also { buffer ->
            childTextureBuffers.add(buffer)
        }
    }

    protected fun runDeferred(block: suspend GlEnv.() -> Unit) {
        postponedTasks.add(block)
    }

    fun getTextureFromBufferPool(bufferReference: BufferReference): Texture {
        if (!runContext.isRenderPhase && automaticBufferRecycle) {
            bufferReferenceUsage[bufferReference] = runContext.currentStepIndex
             logger.d("[BufferedReference] ${bufferReference.nameForDebug} is used at step ${runContext.currentStepIndex}")
        }
        return bufferPool[bufferReference]
    }

    private fun recycleUnusedBuffers(currentStep: Int) {
        val buffersToRecycle = bufferReferenceUsage.entries
            .filter { (_, lastUsedStepIndex) -> lastUsedStepIndex == currentStep }
            .map { it.key }
            .toTypedArray()

        if (buffersToRecycle.isNotEmpty()) {
            bufferPool.recycle(currentStep.toString(), *buffersToRecycle)
        }
    }

    fun trackInput(input: Input<*>) {
        if (runContext.isRenderPhase) return
        if (input !is SampledInput<*>) return
        sampledInputs.add(input)
    }

    fun addStep(step: PipelineStep) {
        _steps.add(step)
        logger.d {
            val detail = when (step) {
                is RunShaderStep<*> -> step.shader.debugName
                is RunTaskStep -> step.purposeForDebug
            }

            "[${step.stepIndex}] add step [${step.type}] - $detail"
        }
    }

    fun <T : KraftShader> addStep(
        shader: T,
        targetBuffer: GlBufferProvider,
        setupAction: suspend PipelineRunningScope.(T) -> Unit = {},
    ) {
        shaders.add(shader)
        
        RunShaderStep(
            stepIndex = _steps.size,
            shader = shader,
            targetBuffer = targetBuffer,
            setupAction = setupAction,
            runContext = runContext,
        ).let(this::addStep)
    }

    override suspend fun run() {
        if (!runContext.isRenderPhase) {
            logger.d("configuration phase starts")
            _steps.forEach {
                runContext.currentStepIndex = it.stepIndex
                it.run(pipelineRunningScope)
            }
            runContext.isRenderPhase = true
        }

        logger.measureAndLog("render with the whole pipeline") {
            with(glEnv) { runPostponedTasks() }

            // Mark all sampled inputs as dirty at the start of the frame
            sampledInputs.forEach { it.markDirty() }
            sampledInputs.forEach { with(it) { internalGet() } }

            runContext.reset()

            logger.d("start to run $stepCount steps")
            run runSteps@{
                _steps.forEach { step ->
                    runContext.currentStepIndex = step.stepIndex
                    step.run(pipelineRunningScope)
                    onDebugAfterShaderStep?.invoke(runContext)

                    if (runContext.forceAbort) {
                        logger.w("force abort the pipeline run")
                        return@runSteps
                    }

                    logger.d {
                        when (step) {
                            is RunShaderStep<*> -> "step ${step.stepIndex} [${step.type}] with ${step.shader.debugName} done"
                            is RunTaskStep -> "step ${step.stepIndex} [${step.type}] for [${step.purposeForDebug}] done"
                        }
                    }

                    // Recycle buffers that won't be used anymore
                    if (automaticBufferRecycle) {
                        recycleUnusedBuffers(step.stepIndex)
                    }
                }
            }

            bufferPool.recycleAll("pipeline_end")
            logger.d("the pool size is ${bufferPool.poolSize} after execution")
        }
    }

    private suspend fun GlEnv.runPostponedTasks() {
        postponedTasks.forEach { it() }
        postponedTasks.clear()
    }

    override suspend fun destroy() {
        logger.d("destroy")
        // Clean up shaders
        if (automaticShaderRecycle) {
            shaders.forEach { shader -> shader.destroy(includeTextures = automaticTextureRecycle) }
            shaders.clear()
        }
        
        // Clean up other resources
        postponedTasks.clear()
        bufferPool.delete()
        childPipelines.forEach { it.destroy() }
        childTextureBuffers.forEach { it.delete() }
        
        logger.d("destroy completed")
    }

    override suspend fun onBufferSizeChanged(size: GlSize) {
        bufferPool.changeSize(size)
        logger.d("buffer size changed to $size")
    }

    private companion object {
        val logger = KraftLogger("Pipeline")
    }

    class PipelineRunContext {
        /**
         * We utilize this flag to do two things after the pipeline is just created
         * 1. Know the step of each [BufferReference] is last used "as texture"
         * 2. Know all the sampled inputs that should be sampled at the beginning of the frame
         *
         * We run [RunShaderStep.setupAction] once for each step, we recorder all the sampled inputs
         * gets called on their [Input.internalGet]. These will be all the inputs that should be sampled at the
         * beginning of the frame.
         * Similarly when [BufferReference] is used in the [RunShaderStep.setupAction], we record the
         * step index to [bufferReferenceUsage] using the same logic, so we can do the automatic recycle.
         */
        var isRenderPhase = false
            internal set

        internal var forceAbort: Boolean = false

        /**
         * Can be used to check the result for the previous step
         */
        var previousBuffer: GlBufferProvider? = null
            private set

        var previousShaderName: String? = null
            private set

        var currentStepIndex: Int = 0
            internal set

        fun abort() {
            forceAbort = true
        }

        fun reset() {
            forceAbort = false
            previousBuffer = null
        }

        fun markPreviousBuffer(buffer: GlBufferProvider) {
            this.previousBuffer = buffer
        }

        fun markPreviousShaderName(shaderName: String?) {
            previousShaderName = shaderName
        }
    }
}

@KraftShadeDsl
class PipelineRunningScope(
    private val pipeline: Pipeline
) {
    val context: Pipeline.PipelineRunContext get() = pipeline.runContext

    fun <T : Any> Input<T>.get(): T {
        return pipeline.internalGet()
    }
}
