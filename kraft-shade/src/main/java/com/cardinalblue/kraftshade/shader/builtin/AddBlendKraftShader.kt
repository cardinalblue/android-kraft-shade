package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

class AddBlendKraftShader : TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = ADD_BLEND_FRAGMENT_SHADER
    var intensity: Float by GlUniformDelegate("intensity")

    init {
        intensity = 1.0f
    }
}

@Language("GLSL")
private const val ADD_BLEND_FRAGMENT_SHADER = """
precision mediump float;
varying highp vec2 textureCoordinate;
varying highp vec2 textureCoordinate2;

uniform sampler2D inputImageTexture;
uniform sampler2D inputImageTexture2;

uniform float intensity;

void main()
{
   lowp vec4 base = texture2D(inputImageTexture, textureCoordinate);
   lowp vec4 overlay = texture2D(inputImageTexture2, textureCoordinate2);

   mediump float r;
   if (overlay.r * base.a + base.r * overlay.a >= overlay.a * base.a) {
     r = overlay.a * base.a + overlay.r * (1.0 - base.a) + base.r * (1.0 - overlay.a);
   } else {
     r = overlay.r + base.r;
   }

   mediump float g;
   if (overlay.g * base.a + base.g * overlay.a >= overlay.a * base.a) {
     g = overlay.a * base.a + overlay.g * (1.0 - base.a) + base.g * (1.0 - overlay.a);
   } else {
     g = overlay.g + base.g;
   }

   mediump float b;
   if (overlay.b * base.a + base.b * overlay.a >= overlay.a * base.a) {
     b = overlay.a * base.a + overlay.b * (1.0 - base.a) + base.b * (1.0 - overlay.a);
   } else {
     b = overlay.b + base.b;
   }

   mediump float a = overlay.a + base.a - overlay.a * base.a;
   vec4 outputColor = vec4(r, g, b, a);

   gl_FragColor = mix(base, outputColor, intensity);
}
"""
