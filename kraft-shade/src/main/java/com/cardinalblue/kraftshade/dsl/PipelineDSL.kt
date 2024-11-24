package com.cardinalblue.kraftshade.dsl

import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.pipeline.*
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.TextureInput
import com.cardinalblue.kraftshade.pipeline.input.asTextureInput
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider


@DslMarker
annotation class PipelineScopeMarker

open class PipelineSetupScope(
    protected val pipeline: Pipeline,
) {
    @PipelineScopeMarker
    fun createBufferReferences(
        vararg namesForDebug: String
    ): BufferReferenceCreator = BufferReferenceCreator(pipeline.bufferPool, *namesForDebug)

    @PipelineScopeMarker
    fun withPipeline(block: Pipeline.() -> Unit) {
        block(pipeline)
    }

    @PipelineScopeMarker
    fun step(
        purposeForDebug: String = "",
        block: suspend GlEnv.() -> Unit
    ) {
        RunTaskStep(
            stepIndex = pipeline.stepCount,
            purposeForDebug = purposeForDebug,
            task = {
                block(pipeline.glEnv)
            },
        ).let(pipeline::addStep)
    }

    /**
     * The setup action should include the input texture if the [KraftShader] needs it. Unless it's
     * a shader doesn't need any input texture.
     */
    @PipelineScopeMarker
    suspend fun <S : KraftShader> step(
        shader: S,
        inputs: List<Input<*>> = emptyList(),
        targetBuffer: GlBufferProvider,
        oneTimeSetupAction: suspend S.() -> Unit = {},
        setupAction: suspend S.(List<Input<*>>) -> Unit = {},
    ) {
        oneTimeSetupAction.invoke(shader)
        pipeline.addStep(
            shader = shader,
            inputs = inputs,
            targetBuffer = targetBuffer,
            setupAction = setupAction,
        )
    }

    /**
     * @param oneTimeSetupAction This is immediately applied to the shader. A useful example is to
     *  set up the input texture if this KraftShader is [TextureInputKraftShader]. See
     *  [addAsStepWithInput] if you are actually working on the setup of [TextureInputKraftShader].
     */
    @PipelineScopeMarker
    suspend fun <S : KraftShader> S.addAsStep(
        vararg inputs: Input<*>,
        targetBuffer: GlBufferProvider,
        oneTimeSetupAction: suspend S.() -> Unit = {},
        setupAction: suspend S.(List<Input<*>>) -> Unit = {},
    ) = step(this, inputs.toList(), targetBuffer, oneTimeSetupAction, setupAction)

    /**
     * If the the shader is [TextureInputKraftShader], this function is more convenient to use. It
     * sets the input texture as a constant and adds the shader as a step to the pipeline. If you
     * use [addAsStep], you can easily forget to set the input texture.
     *
     * @param constantTexture Why we don't use [TextureProvider] here is because the setup of the
     *  texture id here is immediate, so if it's not a constant texture (LoadedTexture), then can
     *  change. For example, if it's a [BufferReference], if we take the texture id immediately. For
     *  the first rendering when WindowSurfaceBuffer is ready, it works fine. If later the size of
     *  the WindowSurfaceBuffer changes, the texture will be deleted by [TextureBufferPool]. However,
     *  the texture id is set to the shader, and it won't change since it's not using a reference.
     */
    @PipelineScopeMarker
    suspend fun <S : TextureInputKraftShader> S.addAsStepWithInput(
        constantTexture: Texture,
        vararg inputs: Input<*>,
        targetBuffer: GlBufferProvider,
        oneTimeSetupAction: suspend S.() -> Unit = {},
        setupAction: suspend S.(List<Input<*>>) -> Unit = {},
    ) {
        addAsStep(
            inputs = inputs,
            targetBuffer = targetBuffer,
            oneTimeSetupAction = {
                setInputTexture(constantTexture)
                oneTimeSetupAction()
            },
            setupAction = setupAction,
        )
    }

    @PipelineScopeMarker
    suspend fun <S : TextureInputKraftShader> S.addAsStepWithInput(
        inputBufferReference: BufferReference,
        vararg inputs: Input<*>,
        targetBuffer: GlBufferProvider,
        oneTimeSetupAction: suspend S.() -> Unit = {},
        setupAction: suspend S.(List<Input<*>>) -> Unit = {},
    ) {
        val inputsPlusTextureInput = inputs.toList() + inputBufferReference.asTextureInput()
        addAsStep(
            inputs = inputsPlusTextureInput.toTypedArray(),
            targetBuffer = targetBuffer,
            oneTimeSetupAction = oneTimeSetupAction,
            setupAction = { _inputs ->
                val textureInput = _inputs.last() as TextureInput
                setInputTexture(textureInput.get())
                setupAction.invoke(this, _inputs.subList(0, _inputs.size - 1))
            },
        )
    }

    @PipelineScopeMarker
    suspend fun serialSteps(
        inputTexture: TextureProvider,
        targetBuffer: GlBufferProvider,
        block: suspend SerialTextureInputPipelineScope.() -> Unit
    ) {
        val scope = SerialTextureInputPipelineScope(
            currentStepIndex = pipeline.stepCount,
            pipeline = pipeline,
            pipelineSetupScope = this,
            inputTexture = inputTexture,
            targetBuffer = targetBuffer
        )

        // we have to do it in two steps, because before the block is finished. We don't know which
        // of the step is the last step that we have to draw to the target buffer.
        scope.block()

        scope.applyToPipeline()
    }
}

class SerialTextureInputPipelineScope internal constructor(
    currentStepIndex: Int,
    private val pipeline: Pipeline,
    private val pipelineSetupScope: PipelineSetupScope,
    private val inputTexture: TextureProvider,
    private val targetBuffer: GlBufferProvider,
) {
    private val bufferReferencePrefix = "$currentStepIndex"
    private val steps = mutableListOf<InternalStep<*>>()

    /**
     * Different from [PipelineSetupScope.step], you don't have to setup the input and provide
     * target buffer, so it's more convenient to use.
     *
     * Also, the step set here isn't added to the [pipeline] immediately. Instead, it will be added
     * to the pipeline when [applyToPipeline] is called. The reason behind this is that we don't
     * know which step is the last step that we have to draw to the target buffer until all the steps
     * are added.
     */
    @PipelineScopeMarker
    fun <S : TextureInputKraftShader> step(
        shader: S,
        vararg inputs: Input<*>,
        setupAction: suspend S.(List<Input<*>>) -> Unit = {},
    ) {
        steps.add(InternalStep(shader, inputs.toList(), setupAction))
    }

    @PipelineScopeMarker
    fun <S : TextureInputKraftShader> S.addAsStep(
        vararg inputs: Input<*>,
        setupAction: suspend S.(List<Input<*>>) -> Unit = {},
    ) = step(this, *inputs, setupAction = setupAction)

    @PipelineScopeMarker
    internal fun applyToPipeline() {
        val stepIterator = steps.iterator()

        // this is ping pong buffer mechanism
        var drawToBuffer1 = true
        val (buffer1, buffer2) = BufferReferenceCreator(
            pipeline.bufferPool,
            "$bufferReferencePrefix-serial-ping",
            "$bufferReferencePrefix-serial-pong",
        )

        check(stepIterator.hasNext()) {
            "serial steps should have at least one step"
        }

        var isFirstStep = true

        while (stepIterator.hasNext()) {
            val step = stepIterator.next()
            val isLastStep = !stepIterator.hasNext()
            val targetBuffer: GlBufferProvider = if (isLastStep) this.targetBuffer else {
                if (drawToBuffer1) buffer1 else buffer2
            }

            val inputTextureProvider: TextureProvider = if (isFirstStep) inputTexture else {
                if (drawToBuffer1) buffer2 else buffer1
            }

            pipeline.addStep(
                shader = step.shader,
                inputs = step.inputs + inputTextureProvider.asTextureInput(),
                targetBuffer = targetBuffer,
                setupAction = { inputs ->
                    val textureInput = inputs.last() as TextureInput
                    step.shader.setInputTexture(textureInput.get())
                    step.setup()
                },
            )

            isFirstStep = false
            drawToBuffer1 = !drawToBuffer1
        }

        if (!pipeline.automaticRecycle) {
            with(pipelineSetupScope) {
                step("clean up ping pong buffers") {
                    pipeline.bufferPool.recycle(buffer1, buffer2)
                }
            }
        }
    }

    private class InternalStep<S : TextureInputKraftShader>(
        val shader: S,
        val inputs: List<Input<*>> = emptyList(),
        val setupAction: suspend (S, List<Input<*>>) -> Unit = { _, _ ->},
    ) {
        suspend fun setup() {
            setupAction.invoke(shader, inputs)
        }
    }
}
