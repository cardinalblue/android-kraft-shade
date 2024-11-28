package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider

sealed class PipelineStep(
    val stepIndex: Int,
) {
    abstract suspend fun run()

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
    val shader: T,
    val inputs: List<Input<*>> = emptyList(),
    val targetBuffer: GlBufferProvider,
    val setupAction: suspend T.(List<Input<*>>) -> Unit = {},
) : PipelineStep(stepIndex) {
    override suspend fun run() {
        shader.setupAction(inputs)
        try {
            shader.drawTo(targetBuffer.provideBuffer())
        } catch (e: Exception) {
            throw RuntimeException("failed to draw shader at step $stepIndex: $shader", e)
        }
    }
}

class RunTaskStep(
    stepIndex: Int,
    val purposeForDebug: String = "",
    private val task: suspend () -> Unit,
) : PipelineStep(stepIndex) {
    override suspend fun run() {
        task()
    }
}