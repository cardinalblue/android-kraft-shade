package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

class HardLightBlendKraftShader : TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = HARD_LIGHT_BLEND_FRAGMENT_SHADER
    var intensity: Float by GlUniformDelegate("intensity")

    init {
        intensity = 1.0f
    }
}

@Language("GLSL")
private const val HARD_LIGHT_BLEND_FRAGMENT_SHADER = """
precision mediump float;
varying highp vec2 textureCoordinate;
varying highp vec2 textureCoordinate2;

uniform sampler2D inputImageTexture;
uniform sampler2D inputImageTexture2;

uniform float intensity;

const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main() {
    mediump vec4 base = texture2D(inputImageTexture, textureCoordinate);
    mediump vec4 overlay = texture2D(inputImageTexture2, textureCoordinate2);

    highp float ra;
    if (2.0 * overlay.r < overlay.a) {
        ra = 2.0 * overlay.r * base.r + overlay.r * (1.0 - base.a) + base.r * (1.0 - overlay.a);
    } else {
        ra = overlay.a * base.a - 2.0 * (base.a - base.r) * (overlay.a - overlay.r) + overlay.r * (1.0 - base.a) + base.r * (1.0 - overlay.a);
    }

    highp float ga;
    if (2.0 * overlay.g < overlay.a) {
        ga = 2.0 * overlay.g * base.g + overlay.g * (1.0 - base.a) + base.g * (1.0 - overlay.a);
    } else {
        ga = overlay.a * base.a - 2.0 * (base.a - base.g) * (overlay.a - overlay.g) + overlay.g * (1.0 - base.a) + base.g * (1.0 - overlay.a);
    }

    highp float ba;
    if (2.0 * overlay.b < overlay.a) {
        ba = 2.0 * overlay.b * base.b + overlay.b * (1.0 - base.a) + base.b * (1.0 - overlay.a);
    } else {
        ba = overlay.a * base.a - 2.0 * (base.a - base.b) * (overlay.a - overlay.b) + overlay.b * (1.0 - base.a) + base.b * (1.0 - overlay.a);
    }
    
    vec4 blendColor = vec4(ra, ga, ba, 1.0);

    gl_FragColor = mix(base, blendColor, intensity);
}
"""
