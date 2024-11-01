package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

class AlphaBlendKraftShader : TwoTextureInputKraftShader() {
    var mixRatio by GlUniformDelegate<Float>("mixRatio")

    init {
        mixRatio = 0.5f
    }

    override fun loadFragmentShader(): String = ALPHA_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val ALPHA_BLEND_FRAGMENT_SHADER = """
    precision mediump float;
    varying vec2 textureCoordinate;
    varying vec2 texture2Coordinate;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;

    uniform float mixRatio;

    vec4 sampleInside(sampler2D sampler, vec2 coord) {
        if (coord.x < 0.0 || coord.x > 1.0) return vec4(0.0);
        if (coord.y < 0.0 || coord.y > 1.0) return vec4(0.0);
        return texture2D(sampler, coord);
    }    

    void main() {
        vec4 textureColor = sampleInside(inputImageTexture, textureCoordinate);
        vec4 textureColor2 = sampleInside(inputImageTexture2, texture2Coordinate);
        
        gl_FragColor = mix(textureColor, textureColor2, mixRatio);
    }
"""
