package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

class AlphaBlendKraftShader(
    mixturePercent: Float = 0.5f
) : MixBlendKraftShader(mixturePercent) {
    override fun loadFragmentShader(): String = ALPHA_BLEND_FRAGMENT_SHADER
    var intensity: Float by GlUniformDelegate("intensity")

    init {
        intensity = 1.0f
    }
}

@Language("GLSL")
private const val ALPHA_BLEND_FRAGMENT_SHADER = """
precision mediump float;
varying vec2 textureCoordinate;
varying vec2 textureCoordinate2;

uniform sampler2D inputImageTexture;
uniform sampler2D inputImageTexture2;

uniform float intensity;
uniform lowp float mixturePercent;

void main() {
    vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);
    textureColor2.a *= intensity;
    gl_FragColor = vec4(mix(textureColor.rgb, textureColor2.rgb, textureColor2.a * mixturePercent), textureColor.a);
}
"""
