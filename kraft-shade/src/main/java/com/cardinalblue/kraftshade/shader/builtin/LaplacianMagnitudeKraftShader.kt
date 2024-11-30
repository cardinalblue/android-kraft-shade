package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlMat3
import com.cardinalblue.kraftshade.shader.Sample3x3KraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

class LaplacianMagnitudeKraftShader : Sample3x3KraftShader() {
    private var convolution: GlMat3 by GlUniformDelegate("convolutionMatrix")

    init {
        convolution = GlMat3(
            0.5f, 1.0f, 0.5f,
            1.0f, -6.0f, 1.0f,
            0.5f, 1.0f, 0.5f
        )
    }

    override fun loadFragmentShader(): String = LAPLACIAN_MAGNITUDE_FRAGMENT_SHADER
}


@Language("glsl")
const val LAPLACIAN_MAGNITUDE_FRAGMENT_SHADER = """
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
    mediump vec4 bottomColor = texture2D(inputImageTexture, bottomTextureCoordinate);
    mediump vec4 bottomLeftColor = texture2D(inputImageTexture, bottomLeftTextureCoordinate);
    mediump vec4 bottomRightColor = texture2D(inputImageTexture, bottomRightTextureCoordinate);
    mediump vec4 centerColor = texture2D(inputImageTexture, textureCoordinate);
    mediump vec4 leftColor = texture2D(inputImageTexture, leftTextureCoordinate);
    mediump vec4 rightColor = texture2D(inputImageTexture, rightTextureCoordinate);
    mediump vec4 topColor = texture2D(inputImageTexture, topTextureCoordinate);
    mediump vec4 topRightColor = texture2D(inputImageTexture, topRightTextureCoordinate);
    mediump vec4 topLeftColor = texture2D(inputImageTexture, topLeftTextureCoordinate);

    mediump vec4 resultColor = topLeftColor * convolutionMatrix[0][0] + topColor * convolutionMatrix[0][1] + topRightColor * convolutionMatrix[0][2];
    resultColor += leftColor * convolutionMatrix[1][0] + centerColor * convolutionMatrix[1][1] + rightColor * convolutionMatrix[1][2];
    resultColor += bottomLeftColor * convolutionMatrix[2][0] + bottomColor * convolutionMatrix[2][1] + bottomRightColor * convolutionMatrix[2][2];

    float strength = distance(resultColor.rgb, vec3(0.0, 0.0, 0.0)) * 2.0;
    gl_FragColor = vec4(vec3(strength), 1.0);
}
"""