package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import com.cardinalblue.kraftshade.model.GlMat4

class ColorMatrixKraftShader(
    intensity: Float = 1.0f,
    colorMatrix: GlMat4 = GlMat4(
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    ),
    colorOffset: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f),
) : TextureInputKraftShader() {
    var intensity: Float by GlUniformDelegate("intensity")
    var colorMatrix: GlMat4 by GlUniformDelegate("colorMatrix")
    var colorOffset: FloatArray by GlUniformDelegate("colorOffset")

    init {
        this.intensity = intensity
        this.colorMatrix = colorMatrix
        this.colorOffset = colorOffset
    }

    override fun loadFragmentShader(): String = COLOR_MATRIX_FRAGMENT_SHADER
}

@Language("GLSL")
private const val COLOR_MATRIX_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;
uniform lowp mat4 colorMatrix;
uniform lowp vec4 colorOffset;
uniform lowp float intensity;

void main()
{
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    lowp vec4 outputColor = textureColor * colorMatrix + colorOffset;
    
    gl_FragColor = mix(textureColor, outputColor, intensity);
}
"""
