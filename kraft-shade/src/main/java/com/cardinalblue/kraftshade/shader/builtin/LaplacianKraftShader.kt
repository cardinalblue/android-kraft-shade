package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlMat3
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

class LaplacianKraftShader : Sample3x3KraftShader() {
    private var uniformConvolutionMatrix: GlMat3 by GlUniformDelegate("convolutionMatrix")

    init {
        // Initialize with default Laplacian kernel
        uniformConvolutionMatrix = GlMat3(
            0.5f, 1.0f, 0.5f,
            1.0f, -6.0f, 1.0f,
            0.5f, 1.0f, 0.5f
        )
    }

    override fun loadFragmentShader(): String = LAPLACIAN_FRAGMENT_SHADER
}

@Language("glsl")
private const val LAPLACIAN_FRAGMENT_SHADER = """
precision highp float;

uniform sampler2D inputImageTexture;
uniform mediump mat3 convolutionMatrix;

varying vec2 textureCoordinate;
varying vec2 leftTextureCoordinate;
varying vec2 rightTextureCoordinate;

varying vec2 topTextureCoordinate;
varying vec2 topLeftTextureCoordinate;
varying vec2 topRightTextureCoordinate;

varying vec2 bottomTextureCoordinate;
varying vec2 bottomLeftTextureCoordinate;
varying vec2 bottomRightTextureCoordinate;

void main()
{
    mediump vec3 bottomColor = texture2D(inputImageTexture, bottomTextureCoordinate).rgb;
    mediump vec3 bottomLeftColor = texture2D(inputImageTexture, bottomLeftTextureCoordinate).rgb;
    mediump vec3 bottomRightColor = texture2D(inputImageTexture, bottomRightTextureCoordinate).rgb;
    mediump vec4 centerColor = texture2D(inputImageTexture, textureCoordinate);
    mediump vec3 leftColor = texture2D(inputImageTexture, leftTextureCoordinate).rgb;
    mediump vec3 rightColor = texture2D(inputImageTexture, rightTextureCoordinate).rgb;
    mediump vec3 topColor = texture2D(inputImageTexture, topTextureCoordinate).rgb;
    mediump vec3 topRightColor = texture2D(inputImageTexture, topRightTextureCoordinate).rgb;
    mediump vec3 topLeftColor = texture2D(inputImageTexture, topLeftTextureCoordinate).rgb;

    mediump vec3 resultColor = topLeftColor * convolutionMatrix[0][0] + topColor * convolutionMatrix[0][1] + topRightColor * convolutionMatrix[0][2];
    resultColor += leftColor * convolutionMatrix[1][0] + centerColor.rgb * convolutionMatrix[1][1] + rightColor * convolutionMatrix[1][2];
    resultColor += bottomLeftColor * convolutionMatrix[2][0] + bottomColor * convolutionMatrix[2][1] + bottomRightColor * convolutionMatrix[2][2];

    // Normalize the results to allow for negative gradients in the 0.0-1.0 colorspace
    resultColor = resultColor + 0.5;

    gl_FragColor = vec4(resultColor, centerColor.a);
}
"""
