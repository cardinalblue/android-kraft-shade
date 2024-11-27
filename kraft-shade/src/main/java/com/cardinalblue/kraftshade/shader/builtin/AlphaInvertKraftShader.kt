package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import org.intellij.lang.annotations.Language

class AlphaInvertKraftShader : TextureInputKraftShader() {
    override fun loadFragmentShader(): String = ALPHA_INVERT_FRAGMENT_SHADER
}

@Language("GLSL")
private const val ALPHA_INVERT_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;

void main()
{
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    gl_FragColor = vec4(textureColor.rgb, 1. - textureColor.a);
}
"""
