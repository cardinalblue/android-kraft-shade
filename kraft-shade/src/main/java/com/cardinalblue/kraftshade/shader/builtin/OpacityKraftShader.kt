package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

class OpacityKraftShader(opacity: Float = 1.0f) : TextureInputKraftShader() {
    var opacity: Float by GlUniformDelegate("opacity")

    init {
        this.opacity = opacity
    }

    override fun loadFragmentShader(): String = OPACITY_FRAGMENT_SHADER
}

@Language("GLSL")
private const val OPACITY_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;
uniform lowp float opacity;

void main()
{
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    
    gl_FragColor = vec4(textureColor.rgb, textureColor.a * opacity);
}
"""