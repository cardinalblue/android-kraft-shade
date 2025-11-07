package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import org.intellij.lang.annotations.Language

/**
 * This can be used to resize from buffer to another buffer
 */
class DrawTextureKraftShader : TextureInputKraftShader() {
    override fun loadFragmentShader(): String = DO_NOTHING_FRAGMENT_SHADER
}

@Language("GLSL")
private const val DO_NOTHING_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;

void main()
{
     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
}
"""
