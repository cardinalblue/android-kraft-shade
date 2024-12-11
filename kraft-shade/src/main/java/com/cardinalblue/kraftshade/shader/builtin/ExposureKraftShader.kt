package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

/**
 * @param exposure The adjusted exposure (-10.0 - 10.0, with 0.0 as the default)
 */
class ExposureKraftShader(exposure: Float = 0f) : TextureInputKraftShader() {
    /**
     * Value from -10.0 to 10.0, with 0.0 as the normal level
     */
    var exposure: Float by GlUniformDelegate("exposure")

    init {
        this.exposure = exposure
    }

    override fun loadFragmentShader(): String = EXPOSURE_FRAGMENT_SHADER
}

@Language("GLSL")
private const val EXPOSURE_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;
uniform highp float exposure;

void main()
{
    highp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);

    gl_FragColor = vec4(textureColor.rgb * pow(2.0, exposure), textureColor.w);
}
"""
