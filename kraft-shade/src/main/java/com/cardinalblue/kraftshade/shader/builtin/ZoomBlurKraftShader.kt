package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlVec2
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

/**
 * Creates a zoom blur effect on the image.
 *
 * @param blurCenter The center of the blur effect, with a default of (0.5, 0.5)
 * @param blurSize The size of the blur, with a default of 1.0
 */
class ZoomBlurKraftShader(
    blurCenter: GlVec2 = GlVec2(0.5f, 0.5f),
    blurSize: Float = 1.0f
) : TextureInputKraftShader() {
    /**
     * The center of the blur effect, with a default of (0.5, 0.5)
     */
    var blurCenter: GlVec2 by GlUniformDelegate("blurCenter")

    /**
     * The size of the blur, with a default of 1.0
     */
    var blurSize: Float by GlUniformDelegate("blurSize")

    init {
        this.blurCenter = blurCenter
        this.blurSize = blurSize
    }

    override fun loadFragmentShader(): String = ZOOM_BLUR_FRAGMENT_SHADER
}

@Language("GLSL")
private const val ZOOM_BLUR_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;

uniform highp vec2 blurCenter;
uniform highp float blurSize;

void main()
{
    // TODO: Do a more intelligent scaling based on resolution here
    highp vec2 samplingOffset = 1.0/100.0 * (blurCenter - textureCoordinate) * blurSize;
    
    lowp vec4 fragmentColor = texture2D(inputImageTexture, textureCoordinate) * 0.18;
    fragmentColor += texture2D(inputImageTexture, textureCoordinate + samplingOffset) * 0.15;
    fragmentColor += texture2D(inputImageTexture, textureCoordinate + (2.0 * samplingOffset)) *  0.12;
    fragmentColor += texture2D(inputImageTexture, textureCoordinate + (3.0 * samplingOffset)) * 0.09;
    fragmentColor += texture2D(inputImageTexture, textureCoordinate + (4.0 * samplingOffset)) * 0.05;
    fragmentColor += texture2D(inputImageTexture, textureCoordinate - samplingOffset) * 0.15;
    fragmentColor += texture2D(inputImageTexture, textureCoordinate - (2.0 * samplingOffset)) *  0.12;
    fragmentColor += texture2D(inputImageTexture, textureCoordinate - (3.0 * samplingOffset)) * 0.09;
    fragmentColor += texture2D(inputImageTexture, textureCoordinate - (4.0 * samplingOffset)) * 0.05;
    
    gl_FragColor = fragmentColor;
}
"""