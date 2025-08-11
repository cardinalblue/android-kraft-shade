package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

/**
 * Separable Gaussian Blur shader implementation that matches GPUImageGaussianBlurFilter behavior.
 * This is a single-pass shader that can be used in horizontal and vertical passes.
 *
 */
class SeparableGaussianBlurKraftShader: TextureInputKraftShader() {
    override fun loadVertexShader(): String = GAUSSIAN_BLUR_VERTEX_SHADER
    override fun loadFragmentShader(): String = GAUSSIAN_BLUR_FRAGMENT_SHADER

    var texelWidthOffset: Float by GlUniformDelegate("texelWidthOffset") 
    var texelHeightOffset: Float by GlUniformDelegate("texelHeightOffset")
    
    init {
        texelWidthOffset = 0.0f
        texelHeightOffset = 0.0f
    }
    
    /**
     * Set up for horizontal pass
     */
    fun setupHorizontalPass(blurSize: Float, textureWidth: Int) {
        texelWidthOffset = blurSize / textureWidth.toFloat()
        texelHeightOffset = 0.0f
    }
    
    /**
     * Set up for vertical pass  
     */
    fun setupVerticalPass(blurSize: Float, textureHeight: Int) {
        texelWidthOffset = 0.0f
        texelHeightOffset = blurSize / textureHeight.toFloat()
    }
}

@Language("GLSL")
private const val GAUSSIAN_BLUR_VERTEX_SHADER = """
attribute vec4 position;
attribute vec4 inputTextureCoordinate;

uniform float texelWidthOffset;
uniform float texelHeightOffset;

varying vec2 textureCoordinate;
varying vec2 blurCoordinates[9];

void main()
{
    gl_Position = position;
    textureCoordinate = inputTextureCoordinate.xy;
    
    // Calculate the positions for the nine input samples to the fragment shader
    // This mimics GPUImageGaussianBlurFilter's vertex shader
    blurCoordinates[0] = inputTextureCoordinate.xy + vec2(-texelWidthOffset * 4.0, -texelHeightOffset * 4.0);
    blurCoordinates[1] = inputTextureCoordinate.xy + vec2(-texelWidthOffset * 3.0, -texelHeightOffset * 3.0);  
    blurCoordinates[2] = inputTextureCoordinate.xy + vec2(-texelWidthOffset * 2.0, -texelHeightOffset * 2.0);
    blurCoordinates[3] = inputTextureCoordinate.xy + vec2(-texelWidthOffset, -texelHeightOffset);
    blurCoordinates[4] = inputTextureCoordinate.xy;
    blurCoordinates[5] = inputTextureCoordinate.xy + vec2(texelWidthOffset, texelHeightOffset);
    blurCoordinates[6] = inputTextureCoordinate.xy + vec2(texelWidthOffset * 2.0, texelHeightOffset * 2.0);
    blurCoordinates[7] = inputTextureCoordinate.xy + vec2(texelWidthOffset * 3.0, texelHeightOffset * 3.0);
    blurCoordinates[8] = inputTextureCoordinate.xy + vec2(texelWidthOffset * 4.0, texelHeightOffset * 4.0);
}
"""

@Language("GLSL")  
private const val GAUSSIAN_BLUR_FRAGMENT_SHADER = """
precision mediump float;

uniform sampler2D inputImageTexture;

varying vec2 textureCoordinate;
varying vec2 blurCoordinates[9];

void main()
{
    vec4 sum = vec4(0.0);
    
    // Gaussian weights matching GPUImageGaussianBlurFilter
    sum += texture2D(inputImageTexture, blurCoordinates[0]) * 0.05;
    sum += texture2D(inputImageTexture, blurCoordinates[1]) * 0.09; 
    sum += texture2D(inputImageTexture, blurCoordinates[2]) * 0.12;
    sum += texture2D(inputImageTexture, blurCoordinates[3]) * 0.15;
    sum += texture2D(inputImageTexture, blurCoordinates[4]) * 0.18;
    sum += texture2D(inputImageTexture, blurCoordinates[5]) * 0.15;
    sum += texture2D(inputImageTexture, blurCoordinates[6]) * 0.12;
    sum += texture2D(inputImageTexture, blurCoordinates[7]) * 0.09;
    sum += texture2D(inputImageTexture, blurCoordinates[8]) * 0.05;
    
    gl_FragColor = sum;
}
"""