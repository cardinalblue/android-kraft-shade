package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

class VignetteKraftShader(
    vignetteCenter: FloatArray = floatArrayOf(0.0f, 0.0f),
    vignetteColor: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f),
    vignetteStart: Float = 0.3f,
    vignetteEnd: Float = 0.75f
) : TextureInputKraftShader() {

    var vignetteCenter: FloatArray by GlUniformDelegate("vignetteCenter")
    var vignetteColor: FloatArray by GlUniformDelegate("vignetteColor")
    var vignetteStart: Float by GlUniformDelegate("vignetteStart")
    var vignetteEnd: Float by GlUniformDelegate("vignetteEnd")

    init {
        this.vignetteCenter = vignetteCenter
        this.vignetteColor = vignetteColor
        this.vignetteStart = vignetteStart
        this.vignetteEnd = vignetteEnd
    }

    override fun loadFragmentShader(): String {
        return VIGNETTE_FRAGMENT_SHADER
    }
}

@Language("GLSL")
private const val VIGNETTE_FRAGMENT_SHADER = """
     uniform sampler2D inputImageTexture;
     varying highp vec2 textureCoordinate;
     
     uniform lowp vec2 vignetteCenter;
     uniform lowp vec3 vignetteColor;
     uniform highp float vignetteStart;
     uniform highp float vignetteEnd;
     
     void main()
     {
         /*
         lowp vec3 rgb = texture2D(inputImageTexture, textureCoordinate).rgb;
         lowp float d = distance(textureCoordinate, vec2(0.5,0.5));
         rgb *= (1.0 - smoothstep(vignetteStart, vignetteEnd, d));
         gl_FragColor = vec4(vec3(rgb),1.0);
          */
         
         lowp vec3 rgb = texture2D(inputImageTexture, textureCoordinate).rgb;
         lowp float d = distance(textureCoordinate, vec2(vignetteCenter.x, vignetteCenter.y));
         lowp float percent = smoothstep(vignetteStart, vignetteEnd, d);
         gl_FragColor = vec4(mix(rgb.x, vignetteColor.x, percent), mix(rgb.y, vignetteColor.y, percent), mix(rgb.z, vignetteColor.z, percent), 1.0);
     }
"""