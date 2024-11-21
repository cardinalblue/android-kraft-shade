package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

class ContrastKraftShader(
    contrast: Float = 1.2f,
) : TextureInputKraftShader() {
    /**
     * Value from 0.0 to 4.0, with 1.0 as the normal level
     */
    var contrast: Float by GlUniformDelegate("contrast")

    init {
        this.contrast = contrast
    }

    override fun loadFragmentShader(): String = CONTRAST_FRAGMENT_SHADER
}

@Language("GLSL")
private const val CONTRAST_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;
uniform lowp float contrast;

void main()
{
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    gl_FragColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w);
}
"""
