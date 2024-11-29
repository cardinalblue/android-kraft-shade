package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language

class SimpleMixtureBlendKraftShader(
    mixturePercent: Float = 0.5f,
) : MixBlendKraftShader(mixturePercent) {
    override fun loadFragmentShader(): String = ALPHA_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val ALPHA_BLEND_FRAGMENT_SHADER = """
    precision mediump float;
    varying vec2 textureCoordinate;
    varying vec2 texture2Coordinate;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;

    uniform float mixturePercent;

    void main() {
        vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
        vec4 textureColor2 = texture2D(inputImageTexture2, texture2Coordinate);

        gl_FragColor = mix(textureColor, textureColor2, mixturePercent);
    }
"""
