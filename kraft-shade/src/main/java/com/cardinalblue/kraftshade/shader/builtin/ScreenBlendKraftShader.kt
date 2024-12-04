package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

/**
 * A screen blend filter that implements Photoshop-like screen blending.
 * The equation used is: D = 1 - ((1 - C1) * (1 - C2))
 * where:
 * D is the resultant color
 * C1 is the first element color
 * C2 is the second element color
 * This blend mode results in a lighter image, as it inverts, multiplies, and inverts again.
 */
class ScreenBlendKraftShader : TwoTextureInputKraftShader() {
    var intensity: Float by GlUniformDelegate("intensity")

    override fun loadFragmentShader(): String = SCREEN_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val SCREEN_BLEND_FRAGMENT_SHADER = """
    precision mediump float;
    varying vec2 textureCoordinate;
    varying vec2 texture2Coordinate;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;

    uniform float intensity;

    void main() {
        vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
        vec4 textureColor2 = texture2D(inputImageTexture2, texture2Coordinate);
        textureColor2.a *= intensity;
        textureColor2.rgb = textureColor2.rgb * textureColor2.a;
        vec4 whiteColor = vec4(1.0);
        gl_FragColor = whiteColor - ((whiteColor - textureColor2) * (whiteColor - textureColor));
    }
"""
