package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlSizeF
import com.cardinalblue.kraftshade.shader.SingleDirectionForTwoPassSamplingKraftShader
import org.intellij.lang.annotations.Language

/**
 * Two-pass separable 9-tap Gaussian-weighted blur. Issue both directions via
 * [com.cardinalblue.kraftshade.shader.stepWithTwoPassSamplingFilter] — the base class derives the
 * per-axis texel offset from the live render buffer resolution every frame, so [radius] (in
 * texels) reads consistently regardless of what buffer this runs against.
 */
class BlurKraftShader : SingleDirectionForTwoPassSamplingKraftShader() {

    /** Blur radius in texels; each of the 4 taps per side steps out by this amount. */
    var radius: Float
        get() = texelSizeRatio.width
        set(value) {
            texelSizeRatio = GlSizeF(value)
        }

    override fun loadVertexShader(): String = BLUR_VERTEX_SHADER
    override fun loadFragmentShader(): String = BLUR_FRAGMENT_SHADER
}

@Language("GLSL")
private const val BLUR_VERTEX_SHADER = """
attribute vec4 position;
attribute vec4 inputTextureCoordinate;

uniform vec2 texelSize;

varying vec2 textureCoordinate;
varying vec2 blurCoordinates[9];

void main()
{
    gl_Position = position;
    textureCoordinate = inputTextureCoordinate.xy;

    blurCoordinates[0] = inputTextureCoordinate.xy - texelSize * 4.0;
    blurCoordinates[1] = inputTextureCoordinate.xy - texelSize * 3.0;
    blurCoordinates[2] = inputTextureCoordinate.xy - texelSize * 2.0;
    blurCoordinates[3] = inputTextureCoordinate.xy - texelSize;
    blurCoordinates[4] = inputTextureCoordinate.xy;
    blurCoordinates[5] = inputTextureCoordinate.xy + texelSize;
    blurCoordinates[6] = inputTextureCoordinate.xy + texelSize * 2.0;
    blurCoordinates[7] = inputTextureCoordinate.xy + texelSize * 3.0;
    blurCoordinates[8] = inputTextureCoordinate.xy + texelSize * 4.0;
}
"""

@Language("GLSL")
private const val BLUR_FRAGMENT_SHADER = """
precision mediump float;

uniform sampler2D inputImageTexture;

varying vec2 textureCoordinate;
varying vec2 blurCoordinates[9];

void main()
{
    vec4 sum = vec4(0.0);
    sum += texture2D(inputImageTexture, blurCoordinates[0]) * 0.05;
    sum += texture2D(inputImageTexture, blurCoordinates[1]) * 0.09;
    sum += texture2D(inputImageTexture, blurCoordinates[2]) * 0.12;
    sum += texture2D(inputImageTexture, blurCoordinates[3]) * 0.15;
    sum += texture2D(inputImageTexture, blurCoordinates[4]) * 0.18;
    sum += texture2D(inputImageTexture, blurCoordinates[5]) * 0.15;
    sum += texture2D(inputImageTexture, blurCoordinates[6]) * 0.12;
    sum += texture2D(inputImageTexture, blurCoordinates[7]) * 0.09;
    sum += texture2D(inputImageTexture, blurCoordinates[8]) * 0.05;
    gl_FragColor = sum;
}
"""
