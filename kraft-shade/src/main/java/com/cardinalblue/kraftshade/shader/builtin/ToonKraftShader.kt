package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.Sample3x3KraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

/**
 * This uses Sobel edge detection to place a black border around objects,
 * and then it quantizes the colors present in the image to give a cartoon-like quality to the image.
 */
class ToonKraftShader : Sample3x3KraftShader() {
    /**
     * The threshold at which to apply the edges, default of 0.2.
     */
    var threshold: Float by GlUniformDelegate("threshold")

    /**
     * The levels of quantization for the posterization of colors within the scene, with a default of 10.0.
     */
    var quantizationLevels: Float by GlUniformDelegate("quantizationLevels")

    override fun loadFragmentShader(): String = TOON_FRAGMENT_SHADER

    init {
        threshold = 0.2f
        quantizationLevels = 10.0f
    }
}

@Language("glsl")
const val TOON_FRAGMENT_SHADER = """
precision highp float;

uniform sampler2D inputImageTexture;

uniform highp float threshold;
uniform highp float quantizationLevels;

varying vec2 textureCoordinate;
varying vec2 leftTextureCoordinate;
varying vec2 rightTextureCoordinate;

varying vec2 topTextureCoordinate;
varying vec2 topLeftTextureCoordinate;
varying vec2 topRightTextureCoordinate;

varying vec2 bottomTextureCoordinate;
varying vec2 bottomLeftTextureCoordinate;
varying vec2 bottomRightTextureCoordinate;

const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main()
{
    vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);

    float bottomLeftIntensity = dot(texture2D(inputImageTexture, bottomLeftTextureCoordinate).rgb, W);
    float topRightIntensity = dot(texture2D(inputImageTexture, topRightTextureCoordinate).rgb, W);
    float topLeftIntensity = dot(texture2D(inputImageTexture, topLeftTextureCoordinate).rgb, W);
    float bottomRightIntensity = dot(texture2D(inputImageTexture, bottomRightTextureCoordinate).rgb, W);
    float leftIntensity = dot(texture2D(inputImageTexture, leftTextureCoordinate).rgb, W);
    float rightIntensity = dot(texture2D(inputImageTexture, rightTextureCoordinate).rgb, W);
    float bottomIntensity = dot(texture2D(inputImageTexture, bottomTextureCoordinate).rgb, W);
    float topIntensity = dot(texture2D(inputImageTexture, topTextureCoordinate).rgb, W);
    float h = -topLeftIntensity - 2.0 * topIntensity - topRightIntensity + bottomLeftIntensity + 2.0 * bottomIntensity + bottomRightIntensity;
    float v = -bottomLeftIntensity - 2.0 * leftIntensity - topLeftIntensity + bottomRightIntensity + 2.0 * rightIntensity + topRightIntensity;

    float mag = length(vec2(h, v));

    vec3 posterizedImageColor = floor((textureColor.rgb * quantizationLevels) + 0.5) / quantizationLevels;

    float thresholdTest = 1.0 - step(threshold, mag);

    gl_FragColor = vec4(posterizedImageColor * thresholdTest, textureColor.a);
}
"""
