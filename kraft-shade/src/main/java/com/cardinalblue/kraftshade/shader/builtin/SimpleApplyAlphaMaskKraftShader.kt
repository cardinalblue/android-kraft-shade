package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import org.intellij.lang.annotations.Language

/**
 * If you need a more advanced version that supports using specific channel instead of the alpha
 * channel, please use [ApplyAlphaMaskKraftShader] instead.
 * In this shader, we only check alpha channel. Also, when the texture coordinate is out of bounds,
 * we treat it as fully transparent.
 */
class SimpleApplyAlphaMaskKraftShader : TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = APPLY_ALPHA_MASK_FRAGMENT_SHADER
}

@Language("GLSL")
private const val APPLY_ALPHA_MASK_FRAGMENT_SHADER = """
    precision highp float;

    varying vec2 textureCoordinate;
    varying vec2 textureCoordinate2;
    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;

    void main() {
        vec4 inputColor = texture2D(inputImageTexture, textureCoordinate);
        float inRange = step(0.0, textureCoordinate2.x) * step(textureCoordinate2.x, 1.0) * step(0.0, textureCoordinate2.y) * step(textureCoordinate2.y, 1.0);
        float maskValue = texture2D(inputImageTexture2, textureCoordinate2).a * inRange;
        gl_FragColor = vec4(inputColor.rgb * maskValue, inputColor.a * maskValue);
    }
"""
