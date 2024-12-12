package com.cardinalblue.kraftshade.shader

import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
import com.cardinalblue.kraftshade.dsl.SerialTextureInputPipelineScope
import com.cardinalblue.kraftshade.model.GlSizeF
import com.cardinalblue.kraftshade.pipeline.PipelineRunningScope
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
    setupActionForSecondDirection: suspend PipelineRunningScope.(S) -> Unit = {},
    setupAction: suspend PipelineRunningScope.(S) -> Unit = {},
) where S : SingleDirectionForTwoPassSamplingKraftShader {
    step(
        shader = shader,
        setupAction = { _shader ->
            _shader.direction = SingleDirectionForTwoPassSamplingKraftShader.Direction.Horizontal
            setupAction(this, _shader)
        },
    )

    step(
        shader = shader,
        setupAction = { _shader ->
            _shader.direction = SingleDirectionForTwoPassSamplingKraftShader.Direction.Vertical
            setupActionForSecondDirection(this, _shader)
        },
    )
}

suspend fun GraphPipelineSetupScope.stepWithTwoPassSamplingFilter(
    shader: SingleDirectionForTwoPassSamplingKraftShader,
    inputTexture: TextureProvider,
    targetBuffer: GlBufferProvider,
    setupActionForSecondDirection: suspend PipelineRunningScope.(SingleDirectionForTwoPassSamplingKraftShader) -> Unit = {},
    setupAction: suspend PipelineRunningScope.(SingleDirectionForTwoPassSamplingKraftShader) -> Unit = {},
) {
    serialSteps(inputTexture, targetBuffer) {
        stepWithTwoPassSamplingFilter(
            shader = shader,
            setupActionForSecondDirection = setupActionForSecondDirection,
            setupAction = setupAction,
        )
    }
}
