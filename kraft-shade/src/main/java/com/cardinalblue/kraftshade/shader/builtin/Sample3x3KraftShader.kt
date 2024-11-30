package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlSizeF
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

abstract class Sample3x3KraftShader : TextureInputKraftShader(), KraftShaderWithTexelSize {
    override var texelSize: GlSizeF by GlUniformDelegate("texelSize")
    override var texelSizeRatio: GlSizeF = GlSizeF(1.0f, 1.0f)
    override fun loadVertexShader(): String = SAMPLE_3x3_VERTEX_SHADER
}

@Language("glsl")
const val SAMPLE_3x3_VERTEX_SHADER = """
attribute vec4 position;
attribute vec4 inputTextureCoordinate;

uniform highp vec2 texelSize;

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
    gl_Position = position;

    vec2 widthStep = vec2(texelSize[0], 0.0);
    vec2 heightStep = vec2(0.0, texelSize[1]);
    vec2 widthHeightStep = vec2(texelSize[0], texelSize[1]);
    vec2 widthNegativeHeightStep = vec2(texelSize[0], -texelSize[1]);

    textureCoordinate = inputTextureCoordinate.xy;
    leftTextureCoordinate = inputTextureCoordinate.xy - widthStep;
    rightTextureCoordinate = inputTextureCoordinate.xy + widthStep;

    topTextureCoordinate = inputTextureCoordinate.xy - heightStep;
    topLeftTextureCoordinate = inputTextureCoordinate.xy - widthHeightStep;
    topRightTextureCoordinate = inputTextureCoordinate.xy + widthNegativeHeightStep;

    bottomTextureCoordinate = inputTextureCoordinate.xy + heightStep;
    bottomLeftTextureCoordinate = inputTextureCoordinate.xy - widthNegativeHeightStep;
    bottomRightTextureCoordinate = inputTextureCoordinate.xy + widthHeightStep;
}
"""
