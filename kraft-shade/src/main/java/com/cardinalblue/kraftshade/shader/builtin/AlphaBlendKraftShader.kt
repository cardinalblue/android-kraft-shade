package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language

class AlphaBlendKraftShader(
    mixturePercent: Float = 0.5f
) : MixBlendKraftShader(mixturePercent) {
    override fun loadFragmentShader(): String = ALPHA_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val ALPHA_BLEND_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;
varying highp vec2 textureCoordinate2;

uniform sampler2D inputImageTexture;
uniform sampler2D inputImageTexture2;

uniform lowp float mixturePercent;

void main() {
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    lowp vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);

    gl_FragColor = vec4(mix(textureColor.rgb, textureColor2.rgb, textureColor2.a * mixturePercent), textureColor.a);
}
"""
