package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

/**
 * A normal blend filter that implements Photoshop-like blending.
 * The equation used is: D = C1 + C2 * C2a * (1 - C1a)
 * where:
 * D is the resultant color
 * C1 is the first element color (overlayer)
 * C1a is the alpha of the first element
 * C2 is the second element color (base)
 * C2a is the alpha of the second element
 */
class NormalBlendKraftShader : TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = NORMAL_BLEND_FRAGMENT_SHADER
    var intensity: Float by GlUniformDelegate("intensity")
}

@Language("GLSL")
private const val NORMAL_BLEND_FRAGMENT_SHADER = """
    precision mediump float;
    varying vec2 textureCoordinate;
    varying vec2 textureCoordinate2;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;
    
    uniform float intensity;

    void main() {
        vec4 base = texture2D(inputImageTexture, textureCoordinate);
        vec4 overlayer = texture2D(inputImageTexture2, textureCoordinate2);

        vec4 outputColor;
        outputColor.r = overlayer.r + base.r * base.a * (1.0 - overlayer.a);
        outputColor.g = overlayer.g + base.g * base.a * (1.0 - overlayer.a);
        outputColor.b = overlayer.b + base.b * base.a * (1.0 - overlayer.a);
        outputColor.a = overlayer.a + base.a * (1.0 - overlayer.a);

        gl_FragColor = mix(base, outputColor, intensity);
    }
"""
