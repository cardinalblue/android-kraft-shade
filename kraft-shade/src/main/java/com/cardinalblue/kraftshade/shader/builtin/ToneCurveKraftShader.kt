package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import org.intellij.lang.annotations.Language

class ToneCurveKraftShader : TwoTextureInputKraftShader(
    secondTextureSampleName = "toneCurveTexture"
) {
    override fun loadFragmentShader(): String {
        return TONE_CURVE_FRAGMENT_SHADER
    }
}

@Language("GLSL")
private const val TONE_CURVE_FRAGMENT_SHADER = """
    varying highp vec2 textureCoordinate;
    uniform sampler2D inputImageTexture;
    uniform sampler2D toneCurveTexture;
    
    void main()
    {
         lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
         lowp float redCurveValue = texture2D(toneCurveTexture, vec2(textureColor.r, 0.0)).r;
         lowp float greenCurveValue = texture2D(toneCurveTexture, vec2(textureColor.g, 0.0)).g;
         lowp float blueCurveValue = texture2D(toneCurveTexture, vec2(textureColor.b, 0.0)).b;
        
         gl_FragColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, textureColor.a);
    }
"""