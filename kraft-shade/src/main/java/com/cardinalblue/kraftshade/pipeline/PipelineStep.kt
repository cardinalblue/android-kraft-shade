package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider

sealed class PipelineStep(
    val stepIndex: Int,
    val runContext: Pipeline.PipelineRunContext,
) {
    abstract suspend fun run(scope: PipelineRunningScope)

    val type: String get() = this::class.simpleName ?: "impossible"
}

/**
 * @property stepIndex TODO maybe we don't need this at all. (for debugging?)
 * @property setupAction In setupAction, you should only add the setup based on inputs that may
 *  change. There are usually two cases you need to consider:
 *  1. The input is a sampled input, and the source of the input may change. For example, [TimeInput]
 *  2. This step is using a shared [effect], and the setup for the effect used in a previous step is
 *     different from the current step. For example, if you are using the same instance of
 *     [SaturationKraftShader] in two steps, but the saturation values used are different, then you
 *     need to set the saturation value in the setupAction.
 *  If these two are not the cases, that means that part of the setup on the effect is constant and
 *  you can just set it once when you create the [KraftShader] or child pipeline.
 */
class RunShaderStep<T : KraftShader> internal constructor(
    stepIndex: Int,
    runContext: Pipeline.PipelineRunContext,
    val shader: T,
    val targetBuffer: GlBufferProvider,
    val setupAction: suspend PipelineRunningScope.(T) -> Unit = {},
) : PipelineStep(stepIndex, runContext) {
    override suspend fun run(scope: PipelineRunningScope) {
        // this has to be set first so we can track the last step BufferReference is used. The mechanism
        // is working inside setupAction.
        runContext.currentStepIndex = stepIndex
        with(scope) {
            setupAction(shader)
        }

        if (!runContext.isRenderPhase) return

        try {
            targetBuffer.provideBuffer().let { buffer ->
                shader.drawTo(buffer)
                runContext.markPreviousShaderName(shader::class.simpleName)
                runContext.markPreviousBuffer(buffer)
            }
        } catch (e: Exception) {
            throw RuntimeException("failed to draw shader at step $stepIndex: $shader", e)
        }
    }
}

class RunTaskStep(
    stepIndex: Int,
    val purposeForDebug: String = "",
    runContext: Pipeline.PipelineRunContext,
    private val task: suspend PipelineRunningScope.() -> Unit,
) : PipelineStep(stepIndex, runContext) {
    override suspend fun run(scope: PipelineRunningScope) {
        with(scope) {
            task()
        }
    }
}