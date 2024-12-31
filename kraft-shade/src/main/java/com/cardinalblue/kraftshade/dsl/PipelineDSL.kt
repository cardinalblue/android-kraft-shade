package com.cardinalblue.kraftshade.dsl

import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.*
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.shader.*
import com.cardinalblue.kraftshade.shader.buffer.*
import com.cardinalblue.kraftshade.shader.builtin.AlphaBlendKraftShader
import com.cardinalblue.kraftshade.util.DangerousKraftShadeApi
import com.cardinalblue.kraftshade.util.KraftLogger

@KraftShadeDsl
sealed class BasePipelineSetupScope(
    env: GlEnv,
    protected val pipeline: Pipeline
) : GlEnvDslScope(env) {
    fun getPoolBufferSize(): GlSize = pipeline.bufferPool.bufferSize

    @KraftShadeDsl
    suspend fun <T> withPipeline(block: suspend Pipeline.() -> T): T {
        return block(pipeline)
    }

    fun createBufferReferences(
        vararg namesForDebug: String
    ): BufferReferenceCreator = BufferReferenceCreator(pipeline, *namesForDebug)

    @KraftShadeDsl
    open suspend fun step(
        purposeForDebug: String = "",
        block: suspend GlEnvDslScope.(runContext: Pipeline.PipelineRunContext) -> Unit
    ) {
        val env = pipeline.glEnv
        RunTaskStep(
            stepIndex = pipeline.stepCount,
            purposeForDebug = purposeForDebug,
            runContext = pipeline.runContext,
            task = {
                block(GlEnvDslScope(env), context)
            },
        ).let(pipeline::addStep)
    }

    @KraftShadeDsl
    open suspend fun graphSteps(
        targetBuffer: GlBufferProvider,
        block: suspend GraphPipelineSetupScope.() -> Unit
    ) {
        val scope = GraphPipelineSetupScope(env, pipeline, targetBuffer)
        block(scope)
    }

    @KraftShadeDsl
    open suspend fun serialSteps(
        inputTexture: TextureProvider,
        targetBuffer: GlBufferProvider,
        block: suspend SerialTextureInputPipelineScope.() -> Unit
    ) {
        val scope = SerialTextureInputPipelineScope(
            currentStepIndex = pipeline.stepCount,
            env = env,
            pipeline = pipeline,
            serialStartTexture = inputTexture,
            serialTargetBuffer = targetBuffer
        )

        // we have to do it in two steps, because before the block is finished. We don't know which
        // of the step is the last step that we have to draw to the target buffer.
        scope.block()
        scope.applyToPipeline()
    }
}

@KraftShadeDsl
class GraphPipelineSetupScope(
    glEnv: GlEnv,
    pipeline: Pipeline,
    val graphTargetBuffer: GlBufferProvider,
) : BasePipelineSetupScope(glEnv, pipeline) {
    /**
     * The setup action should include the input texture if the [KraftShader] needs it. Unless it's
     * a shader doesn't need any input texture. If a input texture is a BufferReference, by listing
     * it as one of [inputs] will help the pipeline to automatically recycle the buffer correctly.
     * See the implementation of [stepWithInputTexture] that takes [BufferReference] as the input.
     * It is following the described pattern (not the one taking constant texture).
     */
    @KraftShadeDsl
    suspend fun <S : KraftShader> step(
        shader: S,
        targetBuffer: GlBufferProvider,
        setupAction: suspend PipelineRunningScope.(S) -> Unit = {},
    ) {
        pipeline.addStep(
            shader = shader,
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
     * texture id here is immediate, so if it's not a constant texture (LoadedTexture), then can
     * change. For example, if it's a [BufferReference], if we take the texture id immediately. For
     * the first rendering when WindowSurfaceBuffer is ready, it works fine. If later the size of
     * the WindowSurfaceBuffer changes, the texture will be deleted by [TextureBufferPool]. However,
     * the texture id is set to the shader, and it won't change since it's not using a reference.
     */
    suspend fun <S : TextureInputKraftShader> stepWithInputTexture(
        shader: S,
        constantTexture: Texture,
        targetBuffer: GlBufferProvider,
        setupAction: suspend PipelineRunningScope.(S) -> Unit = {},
    ) {
        shader.setInputTexture(constantTexture)

        step(
            shader = shader,
            targetBuffer = targetBuffer,
            setupAction = setupAction
        )
    }

    /**
     * This is for distinguishing the step using a constant texture input.
     */
    suspend fun <S : TextureInputKraftShader> stepWithInputTexture(
        shader: S,
        inputTexture: TextureProvider,
        targetBuffer: GlBufferProvider,
        setupAction: suspend (S) -> Unit = {},
    ) {
        step(
            shader,
            targetBuffer = targetBuffer,
            setupAction = { _shader ->
                _shader.setInputTexture(inputTexture)
                setupAction.invoke(_shader)
            },
        )
    }
}

/**
 * @param serialStartTexture this is extremely useful for serial scope when you want to blend your
 * result with the input texture as the target output of this whole serial scope.
 */
@KraftShadeDsl
class SerialTextureInputPipelineScope internal constructor(
    currentStepIndex: Int,
    env: GlEnv,
    pipeline: Pipeline,
    val serialStartTexture: TextureProvider,
    val serialTargetBuffer: GlBufferProvider,
) : BasePipelineSetupScope(env, pipeline) {
    private val bufferReferencePrefix = "s$currentStepIndex"
    private val steps = mutableListOf<InternalStep>()

    /**
     * We have to follow the order of the setup with the other internal steps, so we keep it an
     * internal step first as well.
     */
    @KraftShadeDsl
    override suspend fun step(
        purposeForDebug: String,
        block: suspend GlEnvDslScope.(runContext: Pipeline.PipelineRunContext) -> Unit
    ) {
        steps.add(InternalRunStep(purposeForDebug, block))
    }

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
    suspend fun <S : TextureInputKraftShader> step(
        shader: S,
        setupAction: suspend PipelineRunningScope.(S) -> Unit = {},
    ) {
        steps.add(InternalSingleShaderSimpleStep(shader, setupAction))
    }

    /**
     * Normally you would use [step], but since you can get the [serialStartTexture] in a new scope.
     * You can use this method to generate something and then blend it in the later step. Note that
     * if you use this method, you are allowed to use any type of [KraftShader] not like [step],
     * However, you have to handle the input texture yourself even your shader is a [TextureInputKraftShader].
     * Sometimes you need this because you want the input texture to be set to the second texture of
     * a [TwoTextureInputKraftShader] instead of the first one.
     */
    @DangerousKraftShadeApi
    @KraftShadeDsl
    suspend fun <S : KraftShader> stepIgnoringInputTexture(
        shader: S,
        setupAction: suspend PipelineRunningScope.(S, TextureProvider) -> Unit = { _, _ -> },
    ) {
        steps.add(InternalStepIgnoringInputTexture(shader, setupAction))
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
     * - 0f means the output of the shader is ignored (bypass)
     * - 1f means the result is exactly the output of the shader (no input texture is mixed)
     *
     * TODO: Refactor this by using [serialStep] with [serialStartTexture], so we don't need
     *  [InternalSingleShaderMixtureStep] at all
     */
    @KraftShadeDsl
    fun <S : TextureInputKraftShader> stepWithMixture(
        shader: S,
        mixturePercentInput: Input<Float>,
        setupAction: suspend PipelineRunningScope.(S) -> Unit = {},
    ) {
        steps.add(InternalSingleShaderMixtureStep(shader, mixturePercentInput, setupAction))
    }

    /**
     * In [SerialTextureInputPipelineScope], you should use [graphStep] instead. It creates a new
     * [GraphPipelineSetupScope] and set the target buffer to one of the ping-pong buffer from
     * this [SerialTextureInputPipelineScope]. Therefore, in the new scope, you can just draw to the
     * graphTargetBuffer you can access in the scope.
     */
    @DangerousKraftShadeApi
    @KraftShadeDsl
    override suspend fun graphSteps(
        targetBuffer: GlBufferProvider,
        block: suspend GraphPipelineSetupScope.() -> Unit
    ) {
        error("please use graphStep instead of graphSteps in serial scope")
    }

    /**
     * Using this method, you can get the input texture from serial scope, and you have to render to
     * [GraphPipelineSetupScope.graphTargetBuffer] since it's a new [GraphPipelineSetupScope] instance
     * created specifically for this graph step.
     */
    @KraftShadeDsl
    suspend fun graphStep(
        block: suspend GraphPipelineSetupScope.(inputTexture: TextureProvider) -> Unit
    ) {
        steps.add(InternalGraphStep(block))
    }

    /**
     * In [SerialTextureInputPipelineScope], you should use [serialStep] instead. It creates a new
     * [SerialTextureInputPipelineScope] and set the target buffer to one of the ping-pong buffer from
     * this [SerialTextureInputPipelineScope] as well as the input texture using the previous texture
     * from [SerialTextureInputPipelineScope]. Therefore, in the new scope, you can use
     * [SerialTextureInputPipelineScope.serialStartTexture] as the input.
     */
    @DangerousKraftShadeApi
    @KraftShadeDsl
    override suspend fun serialSteps(
        inputTexture: TextureProvider,
        targetBuffer: GlBufferProvider,
        block: suspend SerialTextureInputPipelineScope.() -> Unit
    ) {
        error("please use graphStep instead of serialSteps in serial scope")
    }

    @KraftShadeDsl
    suspend fun serialStep(block: suspend SerialTextureInputPipelineScope.() -> Unit) {
        steps.add(InternalSerialStep(block))
    }

    internal suspend fun applyToPipeline() {
        check(!steps.isEmpty()) {
            "serial steps should have at least one step"
        }

        // this is ping pong buffer mechanism
        var drawToBuffer1 = true
        val (buffer1, buffer2) = BufferReferenceCreator(
            pipeline,
            "$bufferReferencePrefix-ping",
            "$bufferReferencePrefix-pong",
        )

        var isFirstShaderStep = true
        val lastShaderStep = steps.indexOfLast { it is InternalShaderStep }

        steps.forEachIndexed { index, step ->
            val isShaderStep = step is InternalShaderStep
            val targetBufferForStep: GlBufferProvider = if (index == lastShaderStep) this.serialTargetBuffer else {
                if (drawToBuffer1) buffer1 else buffer2
            }

            val inputTextureForStep: TextureProvider = if (isFirstShaderStep) serialStartTexture else {
                if (drawToBuffer1) buffer2 else buffer1
            }

            when (step) {
                is InternalRunStep -> {
                    super.step(purposeForDebug = step.purposeForDebug, block = step.block)
                }

                is InternalSingleShaderStep<*> -> {
                    addSingleShaderStepToPipeline(
                        step = step,
                        textureForStep = inputTextureForStep,
                        targetBufferForStep = targetBufferForStep,
                    )
                }

                is InternalStepIgnoringInputTexture<*> -> {
                    pipeline.addStep(
                        shader = step.shader,
                        targetBuffer = targetBufferForStep,
                        setupAction = { step.setup(this, inputTextureForStep) },
                    )
                }

                is InternalGraphStep -> {
                    super.graphSteps(targetBufferForStep) {
                        step.block(this, inputTextureForStep)
                    }
                }

                is InternalSerialStep -> {
                    super.serialSteps(inputTextureForStep, targetBufferForStep) {
                        step.block(this)
                    }
                }

                is InternalShaderStep -> error("not possible")
            }

            if (isShaderStep) {
                isFirstShaderStep = false
                drawToBuffer1 = !drawToBuffer1
            }
        }
    }

    private fun addSingleShaderStepToPipeline(
        step: InternalSingleShaderStep<*>,
        textureForStep: TextureProvider,
        targetBufferForStep: GlBufferProvider,
    ) {
        fun addStepForEffect(
            target: GlBufferProvider,
        ) {
            pipeline.addStep(
                shader = step.shader,
                targetBuffer = target,
                setupAction = { shader ->
                    step.shader.setInputTexture(textureForStep.provideTexture())
                    step.setup(this)
                },
            )
        }

        when (step) {
            is InternalSingleShaderSimpleStep -> {
                addStepForEffect(targetBufferForStep)
            }

            is InternalSingleShaderMixtureStep -> {
                val debugStepName = KraftLogger.debugStringOrEmpty {
                    val currentIndex = pipeline.stepCount
                    val effectType = step.shader::class.java.simpleName
                    "m$currentIndex-$effectType"
                }

                val (effectResult) = BufferReferenceCreator(
                    pipeline,
                    "$bufferReferencePrefix-$debugStepName",
                )

                addStepForEffect(effectResult)

                pipeline.addStep(
                    shader = AlphaBlendKraftShader(),
                    targetBuffer = targetBufferForStep,
                    setupAction = { shader ->
                        shader.setInputTexture(textureForStep.provideTexture())
                        shader.setSecondInputTexture(effectResult.provideTexture())
                        shader.mixturePercent = step.mixturePercentInput.get()
                    },
                )
            }
        }
    }

    private sealed class InternalStep

    private class InternalRunStep(
        val purposeForDebug: String,
        val block: suspend GlEnvDslScope.(runContext: Pipeline.PipelineRunContext) -> Unit
    ) : InternalStep()

    private abstract class InternalShaderStep : InternalStep()

    private abstract class InternalSingleShaderStep<S : TextureInputKraftShader>(
        val shader: S,
        val setupAction: suspend PipelineRunningScope.(S) -> Unit = {},
    ) : InternalShaderStep() {
        suspend fun setup(scope: PipelineRunningScope) {
            setupAction.invoke(scope, shader)
        }
    }

    private class InternalSingleShaderSimpleStep<S : TextureInputKraftShader>(
        shader: S,
        setupAction: suspend PipelineRunningScope.(S) -> Unit = {},
    )  : InternalSingleShaderStep<S>(shader, setupAction)

    private class InternalSingleShaderMixtureStep<S : TextureInputKraftShader>(
        shader: S,
        val mixturePercentInput: Input<Float>,
        setupAction: suspend PipelineRunningScope.(S) -> Unit = {},
    ) : InternalSingleShaderStep<S>(shader, setupAction)

    private class InternalStepIgnoringInputTexture<S : KraftShader>(
        val shader: S,
        val setupAction: suspend PipelineRunningScope.(S, TextureProvider) -> Unit = { _, _ -> },
    ) : InternalShaderStep() {
        suspend fun setup(scope: PipelineRunningScope, inputTexture: TextureProvider) {
            setupAction.invoke(scope, shader, inputTexture)
        }
    }

    private class InternalGraphStep(
        val block: suspend GraphPipelineSetupScope.(inputTextureProvider: TextureProvider) -> Unit
    ) : InternalShaderStep()

    private class InternalSerialStep(
        val block: suspend SerialTextureInputPipelineScope.() -> Unit
    ) : InternalShaderStep()
}
