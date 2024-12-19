package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

class SolarizeKraftShader(
    threshold: Float = 0.5f,
) : TextureInputKraftShader() {
    var threshold: Float by GlUniformDelegate("threshold")

    init {
        this.threshold = threshold
    }

    override fun loadFragmentShader(): String = SOLARIZE_FRAGMENT_SHADER
}

@Language("GLSL")
private const val SOLARIZE_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;
uniform highp float threshold;

const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main()
{
    highp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    highp float luminance = dot(textureColor.rgb, W);
    highp float thresholdResult = step(luminance, threshold);
    highp vec3 finalColor = abs(thresholdResult - textureColor.rgb);

    gl_FragColor = vec4(finalColor, textureColor.w);
}
"""
