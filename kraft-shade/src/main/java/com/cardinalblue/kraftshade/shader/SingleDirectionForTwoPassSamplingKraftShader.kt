package com.cardinalblue.kraftshade.shader

import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
import com.cardinalblue.kraftshade.dsl.SerialTextureInputPipelineScope
import com.cardinalblue.kraftshade.model.GlSizeF
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider
import com.cardinalblue.kraftshade.shader.builtin.KraftShaderWithTexelSize
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

abstract class SingleDirectionForTwoPassSamplingKraftShader : TextureInputKraftShader(),
    KraftShaderWithTexelSize {
    override var texelSize: GlSizeF by GlUniformDelegate("texelSize")
    override var texelSizeRatio: GlSizeF = GlSizeF.Unit

    var direction: Direction = Direction.Horizontal

    override fun updateTexelSize() {
        texelSize = if (direction == Direction.Horizontal) {
            GlSizeF(texelSizeRatio.width / resolution.width, 0f)
        } else {
            GlSizeF(0f, texelSizeRatio.height / resolution.height)
        }
    }

    enum class Direction {
        Horizontal, Vertical
    }
}

suspend fun <S> SerialTextureInputPipelineScope.stepWithTwoPassSamplingFilter(
    shader: S,
    vararg inputs: Input<*>,
    setupActionForSecondDirection: suspend S.(List<Input<*>>) -> Unit = {},
    setupAction: suspend S.(List<Input<*>>) -> Unit = {},
) where S : SingleDirectionForTwoPassSamplingKraftShader {
    step(
        shader = shader,
        inputs = inputs,
        setupAction = {
            direction = SingleDirectionForTwoPassSamplingKraftShader.Direction.Horizontal
            setupAction(this, inputs.toList())
        },
    )

    step(
        shader = shader,
        inputs = inputs,
        setupAction = {
            direction = SingleDirectionForTwoPassSamplingKraftShader.Direction.Vertical
            setupActionForSecondDirection(this, inputs.toList())
        },
    )
}

suspend fun GraphPipelineSetupScope.stepWithTwoPassSamplingFilter(
    shader: SingleDirectionForTwoPassSamplingKraftShader,
    inputTexture: TextureProvider,
    targetBuffer: GlBufferProvider,
    vararg inputs: Input<*>,
    setupActionForSecondDirection: suspend SingleDirectionForTwoPassSamplingKraftShader.(List<Input<*>>) -> Unit = {},
    setupAction: suspend SingleDirectionForTwoPassSamplingKraftShader.(List<Input<*>>) -> Unit = {},
) {
    serialSteps(inputTexture, targetBuffer) {
        stepWithTwoPassSamplingFilter(
            shader = shader,
            inputs = inputs,
            setupActionForSecondDirection = setupActionForSecondDirection,
            setupAction = setupAction,
        )
    }
}
