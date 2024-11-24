package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

/**
 * A crosshatch filter that creates a drawing-like effect by applying a series of crossed lines
 * based on the luminance of the image.
 *
 * @property crossHatchSpacing The fractional width of the image to use as the spacing for the crosshatch. Default is 0.03.
 * @property lineWidth A relative width for the crosshatch lines. Default is 0.003.
 */
class CrosshatchKraftShader(
    crossHatchSpacing: Float = 0.03f,
    lineWidth: Float = 0.003f
) : TextureInputKraftShader() {

    var crossHatchSpacing: Float by GlUniformDelegate("crossHatchSpacing")
    var lineWidth: Float by GlUniformDelegate("lineWidth")

    init {
        this.crossHatchSpacing = crossHatchSpacing
        this.lineWidth = lineWidth
    }

    override fun loadFragmentShader(): String = CROSSHATCH_FRAGMENT_SHADER
}

@Language("GLSL")
private const val CROSSHATCH_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;
uniform sampler2D inputImageTexture;
uniform highp float crossHatchSpacing;
uniform highp float lineWidth;

const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main() {
    highp float luminance = dot(texture2D(inputImageTexture, textureCoordinate).rgb, W);
    lowp vec4 colorToDisplay = vec4(1.0, 1.0, 1.0, 1.0);

    if (luminance < 1.00) {
        if (mod(textureCoordinate.x + textureCoordinate.y, crossHatchSpacing) <= lineWidth) {
            colorToDisplay = vec4(0.0, 0.0, 0.0, 1.0);
        }
    }

    if (luminance < 0.75) {
        if (mod(textureCoordinate.x - textureCoordinate.y, crossHatchSpacing) <= lineWidth) {
            colorToDisplay = vec4(0.0, 0.0, 0.0, 1.0);
        }
    }

    if (luminance < 0.50) {
        if (mod(textureCoordinate.x + textureCoordinate.y - (crossHatchSpacing / 2.0), crossHatchSpacing) <= lineWidth) {
            colorToDisplay = vec4(0.0, 0.0, 0.0, 1.0);
        }
    }

    if (luminance < 0.3) {
        if (mod(textureCoordinate.x - textureCoordinate.y - (crossHatchSpacing / 2.0), crossHatchSpacing) <= lineWidth) {
            colorToDisplay = vec4(0.0, 0.0, 0.0, 1.0);
        }
    }

    gl_FragColor = colorToDisplay;
}
"""