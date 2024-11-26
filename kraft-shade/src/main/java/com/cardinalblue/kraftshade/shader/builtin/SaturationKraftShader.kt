package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

class SaturationKraftShader(
    saturation: Float = 0f,
) : TextureInputKraftShader() {
    /**
     * Value from 0f to 2f
     */
    var saturation: Float by GlUniformDelegate("saturation")

    init {
        this.saturation = saturation
    }

    override fun loadFragmentShader(): String = SATURATION_FRAGMENT_SHADER
}

// Copied from GPUImage
@Language("GLSL")
private const val SATURATION_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;
uniform sampler2D inputImageTexture2;
uniform lowp float saturation;

// Values from \"Graphics Shaders: Theory and Practice\" by Bailey and Cunningham
const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

void main()
{
   lowp vec4 textureColor = texture2D(inputImageTexture2, textureCoordinate);
   lowp float luminance = dot(textureColor.rgb, luminanceWeighting);
   lowp vec3 greyScaleColor = vec3(luminance);

   gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, saturation), textureColor.w);

}
"""