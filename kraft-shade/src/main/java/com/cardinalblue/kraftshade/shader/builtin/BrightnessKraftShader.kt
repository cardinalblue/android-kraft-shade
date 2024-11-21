package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

class BrightnessKraftShader(
    brightness: Float = 0f,
) : TextureInputKraftShader() {
    /**
     * Value from -1.0 to 1.0, with 0.0 as the normal level
     */
    var brightness: Float by GlUniformDelegate("brightness")

    init {
        this.brightness = brightness
    }

    override fun loadFragmentShader(): String = BRIGHTNESS_FRAGMENT_SHADER
}

@Language("GLSL")
private const val BRIGHTNESS_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;
uniform lowp float brightness;

void main()
{
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    gl_FragColor = vec4((textureColor.rgb + vec3(brightness)), textureColor.w);
}
"""
