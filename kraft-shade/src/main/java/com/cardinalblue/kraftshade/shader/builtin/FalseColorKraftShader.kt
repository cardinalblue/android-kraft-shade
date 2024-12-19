package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

class FalseColorKraftShader(
    firstColor: FloatArray = floatArrayOf(0f, 0f, 0f),
    secondColor: FloatArray = floatArrayOf(1f, 1f, 1f),
) : TextureInputKraftShader() {

    var firstColor: FloatArray by GlUniformDelegate("firstColor")
    var secondColor: FloatArray by GlUniformDelegate("secondColor")

    init {
        this.firstColor = firstColor
        this.secondColor = secondColor
    }

    override fun loadFragmentShader(): String {
        return FALSE_COLOR_FRAGMENT_SHADER
    }
}

@Language("GLSL")
private const val FALSE_COLOR_FRAGMENT_SHADER = """
precision lowp float;

varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;
uniform float intensity;
uniform vec3 firstColor;
uniform vec3 secondColor;

const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

void main()
{
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    float luminance = dot(textureColor.rgb, luminanceWeighting);

    gl_FragColor = vec4(mix(firstColor, secondColor, luminance), textureColor.a);
}
"""