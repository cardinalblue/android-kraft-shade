package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader

/**
 * A source-over blend filter that blends two textures using the source alpha.
 * The resulting color is a mix of the base and overlay colors based on the overlay's alpha value.
 * This implements the standard "source over" alpha compositing operation.
 */
class SourceOverBlendKraftShader : TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = SOURCE_OVER_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val SOURCE_OVER_BLEND_FRAGMENT_SHADER = """
    precision mediump float;
    varying vec2 textureCoordinate;
    varying vec2 texture2Coordinate;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;

    void main() {
        vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
        vec4 textureColor2 = texture2D(inputImageTexture2, texture2Coordinate);

        gl_FragColor = mix(textureColor, textureColor2, textureColor2.a);
    }
"""
