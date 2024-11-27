package com.cardinalblue.kraftshade.dsl

import com.cardinalblue.kraftshade.pipeline.*
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.TextureInput
import com.cardinalblue.kraftshade.pipeline.input.asTextureInput
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider
import com.cardinalblue.kraftshade.shader.builtin.SimpleMixtureBlendKraftShader
import com.cardinalblue.kraftshade.util.KraftLogger

@KraftShadeDsl
class PipelineSetupScope(
    private val pipeline: Pipeline,
) {
    fun withPipeline(block: Pipeline.() -> Unit) {
        block(pipeline)
    }

    fun createBufferReferences(
        vararg namesForDebug: String
    ): BufferReferenceCreator = BufferReferenceCreator(pipeline.bufferPool, *namesForDebug)

    @KraftShadeDsl
    fun step(
        purposeForDebug: String = "",
        block: suspend GlEnvDslScope.() -> Unit
    ) {
        RunTaskStep(
            stepIndex = pipeline.stepCount,
            purposeForDebug = purposeForDebug,
            task = {
                block(GlEnvDslScope(pipeline.glEnv))
            },
        ).let(pipeline::addStep)
    }

    /**
     * The setup action should include the input texture if the [KraftShader] needs it. Unless it's
     * a shader doesn't need any input texture.
     *
     * @param oneTimeSetupAction This is immediately applied to the shader. A useful example is to
     *  set up the input texture if this KraftShader is [TextureInputKraftShader]. See
     *  [stepWithInputTexture] if you are actually working on the setup of [TextureInputKraftShader].
     */
    @KraftShadeDsl
    suspend fun <S : KraftShader> step(
        shader: S,
        vararg inputs: Input<*>,
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
    suspend fun <S : TextureInputKraftShader> stepWithInputTexture(
        shader: S,
        constantTexture: Texture,
        vararg inputs: Input<*>,
        targetBuffer: GlBufferProvider,
        oneTimeSetupAction: suspend S.() -> Unit = {},
        setupAction: suspend S.(List<Input<*>>) -> Unit = {},
    ) {
        step(
            shader = shader,
            inputs = inputs,
            targetBuffer = targetBuffer,
            oneTimeSetupAction = {
                setInputTexture(constantTexture)
                oneTimeSetupAction()
            },
            setupAction = setupAction
        )
    }

    suspend fun <S : TextureInputKraftShader> stepWithInputTexture(
        shader: S,
        inputBufferReference: BufferReference,
        vararg inputs: Input<*>,
        targetBuffer: GlBufferProvider,
        oneTimeSetupAction: suspend S.() -> Unit = {},
        setupAction: suspend S.(List<Input<*>>) -> Unit = {},
    ) {
        val inputsPlusTextureInput = inputs.toList() + inputBufferReference.asTextureInput()
        step(
            shader,
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

    @KraftShadeDsl
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

@KraftShadeDsl
class SerialTextureInputPipelineScope internal constructor(
    currentStepIndex: Int,
    private val pipeline: Pipeline,
    private val pipelineSetupScope: PipelineSetupScope,
    private val inputTexture: TextureProvider,
    private val targetBuffer: GlBufferProvider,
) {
    private val bufferReferencePrefix = "s$currentStepIndex"
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
    @KraftShadeDsl
    fun <S : TextureInputKraftShader> step(
        shader: S,
        vararg inputs: Input<*>,
        setupAction: suspend S.(List<Input<*>>) -> Unit = {},
    ) {
        steps.add(InternalSimpleStep(shader, inputs.toList(), setupAction))
    }

    /**
     * This is a convenient way to add a step that mix the shader output with its input texture
     * using [mixturePercentInput]. Some of the [KraftShader] has intensity which is usually jus a
     * value to control the intensity of the effect by mixing the output of the shader with the
     * original input texture. This method provides the exactly same functionality, but [shader]
     * doesn't need to have the intensity parameter.
     *
     * For example, you can use [GrayScaleKraftShader] with this method, to control how colorful it
     * is by setting the [mixturePercentInput] to 0f to 1f. By doing this, the shader turns into a
     * saturation filter that only drop the saturation value.
     *
     * @param mixturePercentInput the valid range of this parameter is [0f, 1f].
     *  - 0f means the output of the shader is ignored (bypass)
     *  - 1f means the result is exactly the output of the shader (no input texture is mixed)
     */
    @KraftShadeDsl
    fun <S : TextureInputKraftShader> stepWithMixture(
        shader: S,
        mixturePercentInput: Input<Float>,
        vararg inputs: Input<*>,
        setupAction: suspend S.(List<Input<*>>) -> Unit = {},
    ) {
        steps.add(InternalMixtureStep(shader, mixturePercentInput, inputs.toList(), setupAction))
    }

    internal fun applyToPipeline() {
        val stepIterator = steps.iterator()

        // this is ping pong buffer mechanism
        var drawToBuffer1 = true
        val (buffer1, buffer2) = BufferReferenceCreator(
            pipeline.bufferPool,
            "$bufferReferencePrefix-ping",
            "$bufferReferencePrefix-pong",
        )

        check(stepIterator.hasNext()) {
            "serial steps should have at least one step"
        }

        var isFirstStep = true

        while (stepIterator.hasNext()) {
            val step = stepIterator.next()
            val isLastStep = !stepIterator.hasNext()
            val targetBufferForStep: GlBufferProvider = if (isLastStep) this.targetBuffer else {
                if (drawToBuffer1) buffer1 else buffer2
            }

            val inputTextureForStep: TextureProvider = if (isFirstStep) inputTexture else {
                if (drawToBuffer1) buffer2 else buffer1
            }

            addSingleStepToPipeline(
                step = step,
                textureForStep = inputTextureForStep,
                targetBufferForStep = targetBufferForStep,
            )

            isFirstStep = false
            drawToBuffer1 = !drawToBuffer1
        }

        if (!pipeline.automaticRecycle) {
            with(pipelineSetupScope) {
                step("clean up ping pong buffers") {
                    this@SerialTextureInputPipelineScope
                        .pipeline
                        .bufferPool
                        .recycle("serial_end", buffer1, buffer2)
                }
            }
        }
    }

    private fun addSingleStepToPipeline(
        step: InternalStep<*>,
        textureForStep: TextureProvider,
        targetBufferForStep: GlBufferProvider,
    ) {
        fun addStepForEffect(
            target: GlBufferProvider,
        ) {
            pipeline.addStep(
                shader = step.shader,
                // this is needed to trigger the automatic recycle mechanism
                inputs = (step.inputs + textureForStep.asTextureInput()).toTypedArray(),
                targetBuffer = target,
                setupAction = { inputs ->
                    val input = inputs.last() as TextureInput
                    step.shader.setInputTexture(input.get())
                    step.setup()
                },
            )
        }
        when (step) {
            is InternalSimpleStep<*> -> {
                addStepForEffect(targetBufferForStep)
            }

            is InternalMixtureStep<*> -> {
                val debugStepName = KraftLogger.debugStringOrEmpty {
                    val currentIndex = pipeline.stepCount
                    val effectType = step.shader::class.java.simpleName
                    "m$currentIndex-$effectType"
                }

                val (effectResult) = BufferReferenceCreator(
                    pipeline.bufferPool,
                    "$bufferReferencePrefix-$debugStepName",
                )

                addStepForEffect(effectResult)

                pipeline.addStep(
                    shader = SimpleMixtureBlendKraftShader(),
                    textureForStep.asTextureInput(),
                    effectResult.asTextureInput(),
                    step.mixturePercentInput,
                    targetBuffer = targetBufferForStep,
                    setupAction = { (originalTextureInput, fullyAppliedInput, mixturePercentInput) ->
                        setInputTexture(originalTextureInput.getCasted())
                        secondInputTextureId = fullyAppliedInput.getCasted<Texture>().textureId
                        mixturePercent = mixturePercentInput.getCasted()
                    },
                )
            }
        }
    }

    private sealed class InternalStep<S : TextureInputKraftShader>(
        val shader: S,
        val inputs: List<Input<*>> = emptyList(),
        val setupAction: suspend (S, List<Input<*>>) -> Unit = { _, _ ->},
    ) {
        suspend fun setup() {
            setupAction.invoke(shader, inputs)
        }
    }

    private class InternalSimpleStep<S : TextureInputKraftShader>(
        shader: S,
        inputs: List<Input<*>> = emptyList(),
        setupAction: suspend (S, List<Input<*>>) -> Unit = { _, _ ->},
    )  : InternalStep<S>(shader, inputs, setupAction)

    private class InternalMixtureStep<S : TextureInputKraftShader>(
        shader: S,
        val mixturePercentInput: Input<Float>,
        inputs: List<Input<*>> = emptyList(),
        setupAction: suspend (S, List<Input<*>>) -> Unit = { _, _ ->},
    )  : InternalStep<S>(shader, inputs, setupAction)
}
