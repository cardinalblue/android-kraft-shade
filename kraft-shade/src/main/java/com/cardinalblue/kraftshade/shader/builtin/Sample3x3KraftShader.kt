package com.cardinalblue.kraftshade.shader.builtin

import androidx.annotation.CallSuper
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

abstract class Sample3x3KraftShader : TextureInputKraftShader() {
    private var texelWidth: Float by GlUniformDelegate("texelWidth")
    private var texelHeight: Float by GlUniformDelegate("texelHeight")

    override fun loadVertexShader(): String = SAMPLE_3x3_VERTEX_SHADER

    @CallSuper
    override fun beforeActualDraw() {
        super.beforeActualDraw()
        texelWidth = 1f / resolution[0]
        texelHeight = 1f / resolution[1]
    }
}

@Language("glsl")
const val SAMPLE_3x3_VERTEX_SHADER = """
attribute vec4 position;
attribute vec4 inputTextureCoordinate;

uniform highp float texelWidth;
uniform highp float texelHeight;

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

    vec2 widthStep = vec2(texelWidth, 0.0);
    vec2 heightStep = vec2(0.0, texelHeight);
    vec2 widthHeightStep = vec2(texelWidth, texelHeight);
    vec2 widthNegativeHeightStep = vec2(texelWidth, -texelHeight);

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