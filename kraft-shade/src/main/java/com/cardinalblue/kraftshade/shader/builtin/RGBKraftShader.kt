package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

/**
 * Adjusts the individual RGB channels of an image
 * @param red Normalized value by which the red channel is multiplied. The range is from 0.0 up, with 1.0 as the default.
 * @param green Normalized value by which the green channel is multiplied. The range is from 0.0 up, with 1.0 as the default.
 * @param blue Normalized value by which the blue channel is multiplied. The range is from 0.0 up, with 1.0 as the default.
 */
class RGBKraftShader(
    red: Float = 1.0f,
    green: Float = 1.0f,
    blue: Float = 1.0f,
) : TextureInputKraftShader() {
    var red: Float by GlUniformDelegate("red")
    var green: Float by GlUniformDelegate("green")
    var blue: Float by GlUniformDelegate("blue")

    init {
        this.red = red
        this.green = green
        this.blue = blue
    }

    override fun loadFragmentShader(): String = RGB_FRAGMENT_SHADER
}

@Language("GLSL")
private const val RGB_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;
uniform highp float red;
uniform highp float green;
uniform highp float blue;

void main() {
    highp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);

    gl_FragColor = vec4(textureColor.r * red, textureColor.g * green, textureColor.b * blue, 1.0);
}
"""