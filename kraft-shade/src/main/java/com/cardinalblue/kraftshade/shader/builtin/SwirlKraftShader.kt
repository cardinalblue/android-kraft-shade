package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import com.cardinalblue.kraftshade.model.GlVec2

/**
 * Creates a swirl distortion effect on the image.
 *
 * @param radius The radius of the distortion, ranging from 0.0 to 1.0, with a default of 0.5
 * @param angle The amount of distortion to apply, with a minimum of 0.0 and a default of 1.0
 * @param center The center about which to apply the distortion, with a default of (0.5, 0.5)
 */
class SwirlKraftShader(
    radius: Float = 0.5f,
    angle: Float = 1.0f,
    center: GlVec2 = GlVec2(0.5f, 0.5f)
) : TextureInputKraftShader() {
    /**
     * The radius of the distortion, ranging from 0.0 to 1.0, with a default of 0.5.
     */
    var radius: Float by GlUniformDelegate("radius")

    /**
     * The amount of distortion to apply, with a minimum of 0.0 and a default of 1.0.
     */
    var angle: Float by GlUniformDelegate("angle")

    /**
     * The center about which to apply the distortion, with a default of (0.5, 0.5).
     */
    var center: GlVec2 by GlUniformDelegate("center")

    init {
        this.radius = radius
        this.angle = angle
        this.center = center
    }

    override fun loadFragmentShader(): String = SWIRL_FRAGMENT_SHADER
}

@Language("GLSL")
private const val SWIRL_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;

uniform highp vec2 center;
uniform highp float radius;
uniform highp float angle;

void main()
{
    highp vec2 textureCoordinateToUse = textureCoordinate;
    highp float dist = distance(center, textureCoordinate);
    if (dist < radius)
    {
        textureCoordinateToUse -= center;
        highp float percent = (radius - dist) / radius;
        highp float theta = percent * percent * angle * 8.0;
        highp float s = sin(theta);
        highp float c = cos(theta);
        textureCoordinateToUse = vec2(dot(textureCoordinateToUse, vec2(c, -s)), dot(textureCoordinateToUse, vec2(s, c)));
        textureCoordinateToUse += center;
    }

    gl_FragColor = texture2D(inputImageTexture, textureCoordinateToUse);
}
"""
