package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

/**
 * A multiply blend filter that multiplies the source color with the destination color.
 * The resulting color is always at least as dark as either of the two constituent colors.
 * Multiplying any color with black produces black. Multiplying any color with white leaves the original color unchanged.
 */
class MultiplyBlendKraftShader : TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = MULTIPLY_BLEND_FRAGMENT_SHADER
    var intensity: Float by GlUniformDelegate("intensity")

    init {
        intensity = 1.0f
    }
}

@Language("GLSL")
private const val MULTIPLY_BLEND_FRAGMENT_SHADER = """
    precision mediump float;
    varying vec2 textureCoordinate;
    varying vec2 textureCoordinate2;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;
    
    uniform float intensity;

    vec4 sampleInside(sampler2D sampler, vec2 coord) {
        if (coord.x < 0.0 || coord.x > 1.0) return vec4(0.0);
        if (coord.y < 0.0 || coord.y > 1.0) return vec4(0.0);
        return texture2D(sampler, coord);
    }    

    void main() {
        vec4 base = sampleInside(inputImageTexture, textureCoordinate);
        vec4 overlayer = sampleInside(inputImageTexture2, textureCoordinate2);

        vec4 outputColor = overlayer * base + overlayer * (1.0 - base.a) + base * (1.0 - overlayer.a);
        
        gl_FragColor = mix(base, outputColor, intensity);
    }
"""
