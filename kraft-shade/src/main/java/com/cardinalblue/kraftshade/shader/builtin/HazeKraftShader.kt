package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

/**
 * A shader that adds or removes haze effect from the image.
 * This is similar to a UV filter.
 *
 * @param distance Strength of the color applied. Values between -0.3 and 0.3 are best
 * @param slope Amount of color change. Values between -0.3 and 0.3 are best
 */
class HazeKraftShader(
    distance: Float = 0.2f,
    slope: Float = 0.0f
) : TextureInputKraftShader() {
    /**
     * Strength of the color applied. Values between -0.3 and 0.3 are best.
     */
    var distance: Float by GlUniformDelegate("distance")

    /**
     * Amount of color change. Values between -0.3 and 0.3 are best.
     */
    var slope: Float by GlUniformDelegate("slope")

    init {
        this.distance = distance
        this.slope = slope
    }

    override fun loadFragmentShader(): String = HAZE_FRAGMENT_SHADER
}

@Language("GLSL")
private const val HAZE_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;

uniform lowp float distance;
uniform highp float slope;

void main()
{
    highp vec4 color = vec4(1.0);

    highp float d = textureCoordinate.y * slope + distance;

    highp vec4 c = texture2D(inputImageTexture, textureCoordinate);

    c = (c - d * color) / (1.0 - d);

    gl_FragColor = c;
}
"""
