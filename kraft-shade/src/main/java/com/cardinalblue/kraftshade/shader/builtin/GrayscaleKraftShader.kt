package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import org.intellij.lang.annotations.Language

class GrayscaleKraftShader : TextureInputKraftShader() {
    override fun loadFragmentShader(): String = GRAYSCALE_FRAGMENT_SHADER
}

@Language("GLSL")
private const val GRAYSCALE_FRAGMENT_SHADER = """
precision highp float;

varying vec2 textureCoordinate;

uniform sampler2D inputImageTexture;

const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main()
{
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    float luminance = dot(textureColor.rgb, W);

    gl_FragColor = vec4(vec3(luminance), textureColor.a);
}
"""