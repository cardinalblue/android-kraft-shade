package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.SingleDirectionForTwoPassSamplingKraftShader
import org.intellij.lang.annotations.Language

/**
 * For each pixel, this sets it to the maximum value of the red channel in a rectangular neighborhood
 * extending out dilationRadius pixels from the center. This extends out bright features, and is most
 * commonly used with black-and-white thresholded images.
 */
class DilationKraftShader(
    private val radius: Int = 1
) : SingleDirectionForTwoPassSamplingKraftShader() {
    init {
        require(radius in 1..4) { "Radius must be between 1 and 4" }
    }

    override fun loadVertexShader(): String = getDilationVertexShader(radius)
    override fun loadFragmentShader(): String = getDilationFragmentShader(radius)
}

@Language("GLSL")
private fun getDilationVertexShader(radius: Int) = """
attribute vec4 position;
attribute vec2 inputTextureCoordinate;

uniform vec2 texelSize;

varying vec2 centerTextureCoordinate;
${stepVaryingVec2Declaration(radius)}

void main() {
    gl_Position = position;

    centerTextureCoordinate = inputTextureCoordinate;
    ${stepVaryingVec2Assignment(radius)}
}
"""

private fun stepVaryingVec2Assignment(radius: Int): String {
    return StringBuilder().run {
        for (i in 1..radius) {
            appendLine("step${i}NegativeTextureCoordinate = inputTextureCoordinate - texelSize * $i.0;")
            appendLine("step${i}PositiveTextureCoordinate = inputTextureCoordinate + texelSize * $i.0;")
        }
        toString()
    }
}

private fun stepVaryingVec2Declaration(radius: Int): String {
    return StringBuilder().run {
        for (i in 1..radius) {
            appendLine("varying vec2 step${i}PositiveTextureCoordinate;")
            appendLine("varying vec2 step${i}NegativeTextureCoordinate;")
        }
        toString()
    }
}

@Language("GLSL")
private fun getDilationFragmentShader(radius: Int) = """
precision mediump float;

varying vec2 centerTextureCoordinate;
${stepVaryingVec2Declaration(radius)}

uniform sampler2D inputImageTexture;

void main() {
    float centerIntensity = texture2D(inputImageTexture, centerTextureCoordinate).r;
    float maxValue = 0.0;
    ${maxValueCalculation(radius)}

    gl_FragColor = vec4(vec3(maxValue), 1.0);
}
"""

fun maxValueCalculation(radius: Int): String {
    return StringBuilder().run {
        for (i in 1..radius) {
            appendLine("float step${i}PositiveIntensity = texture2D(inputImageTexture, step${i}PositiveTextureCoordinate).r;")
            appendLine("maxValue = max(maxValue, step${i}PositiveIntensity);")
            appendLine("float step${i}NegativeIntensity = texture2D(inputImageTexture, step${i}NegativeTextureCoordinate).r;")
            appendLine("maxValue = max(maxValue, step${i}NegativeIntensity);")
        }
        toString()
    }
}
