package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

/**
 * Reduces the color range of the image to the number of colors specified by colorLevels.
 * colorLevels: 1-256, with a default of 10
 */
class PosterizeKraftShader(colorLevels: Int = 10) : TextureInputKraftShader() {

    private var _colorLevels: Float by GlUniformDelegate("colorLevels")
    var colorLevels: Int
        get() { return _colorLevels.toInt() }
        set(value) { _colorLevels = value.toFloat() }

    init {
        this.colorLevels = colorLevels
    }

    override fun loadFragmentShader(): String {
        return POSTERIZE_FRAGMENT_SHADER
    }
}

@Language("GLSL")
private const val POSTERIZE_FRAGMENT_SHADER = """
    varying highp vec2 textureCoordinate;

    uniform sampler2D inputImageTexture;
    uniform float colorLevels;

    void main() {
       highp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
       
       gl_FragColor = floor((textureColor * colorLevels) + vec4(0.5)) / colorLevels;
    }
"""