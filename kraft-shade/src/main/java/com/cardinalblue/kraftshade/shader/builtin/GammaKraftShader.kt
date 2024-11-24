package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

/**
 * @property gamma gamma value ranges from 0.0 to 3.0, with 1.0 as the normal level
 */
class GammaKraftShader(
    gamma: Float = 1f
) : TextureInputKraftShader() {
    var gamma: Float by GlUniformDelegate("gamma")

    init {
        this.gamma = gamma
    }

    override fun loadFragmentShader(): String = GAMMA_FRAGMENT_SHADER
}

@Language("GLSL")
private const val GAMMA_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;
 
uniform sampler2D inputImageTexture;
uniform lowp float gamma;
 
void main()
{
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    
    gl_FragColor = vec4(pow(textureColor.rgb, vec3(gamma)), textureColor.w);
}
"""