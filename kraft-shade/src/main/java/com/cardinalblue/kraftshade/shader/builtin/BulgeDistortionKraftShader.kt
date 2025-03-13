package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.model.GlVec2
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

/**
 * Creates a bulge distortion effect on the image.
 *
 * @param radius The radius of the distortion, ranging from 0.0 to 1.0, with a default of 0.25
 * @param scale The amount of distortion to apply, from -1.0 to 1.0, with a default of 0.5
 * @param center The center about which to apply the distortion, with a default of (0.5, 0.5)
 */
class BulgeDistortionKraftShader(
    radius: Float = 0.25f,
    scale: Float = 0.5f,
    center: GlVec2 = GlVec2(0.5f, 0.5f)
) : TextureInputKraftShader() {
    /**
     * The radius of the distortion, ranging from 0.0 to 1.0, with a default of 0.25
     */
    var radius: Float by GlUniformDelegate("radius")

    /**
     * The amount of distortion to apply, from -1.0 to 1.0, with a default of 0.5
     */
    var scale: Float by GlUniformDelegate("scale")

    /**
     * The center about which to apply the distortion, with a default of (0.5, 0.5)
     */
    var center: GlVec2 by GlUniformDelegate("center")

    /**
     * The aspect ratio of the image
     */
    var aspectRatio: Float by GlUniformDelegate("aspectRatio")

    init {
        this.radius = radius
        this.scale = scale
        this.center = center
        this.aspectRatio = 1.0f
    }

    override fun loadFragmentShader(): String = BULGE_FRAGMENT_SHADER
    
    override fun draw(bufferSize: GlSize, isScreenCoordinate: Boolean) {
        // Update aspect ratio based on buffer size
        aspectRatio = bufferSize.height.toFloat() / bufferSize.width.toFloat()
        super.draw(bufferSize, isScreenCoordinate)
    }
}

@Language("GLSL")
private const val BULGE_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;

uniform highp float aspectRatio;
uniform highp vec2 center;
uniform highp float radius;
uniform highp float scale;

void main()
{
highp vec2 textureCoordinateToUse = vec2(textureCoordinate.x, (textureCoordinate.y * aspectRatio + 0.5 - 0.5 * aspectRatio));
highp float dist = distance(center, textureCoordinateToUse);
textureCoordinateToUse = textureCoordinate;

if (dist < radius)
{
textureCoordinateToUse -= center;
highp float percent = 1.0 - ((radius - dist) / radius) * scale;
percent = percent * percent;

textureCoordinateToUse = textureCoordinateToUse * percent;
textureCoordinateToUse += center;
}

gl_FragColor = texture2D(inputImageTexture, textureCoordinateToUse );    
}
"""