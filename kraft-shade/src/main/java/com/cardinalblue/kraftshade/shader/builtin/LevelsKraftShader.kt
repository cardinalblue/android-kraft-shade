package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

class LevelsKraftShader : TextureInputKraftShader() {
    var levelMinimum: FloatArray by GlUniformDelegate("levelMinimum")
    var levelMiddle: FloatArray by GlUniformDelegate("levelMiddle")
    var levelMaximum: FloatArray by GlUniformDelegate("levelMaximum")
    var minOutput: FloatArray by GlUniformDelegate("minOutput")
    var maxOutput: FloatArray by GlUniformDelegate("maxOutput")

    init {
        levelMinimum = floatArrayOf(0.0f, 0.0f, 0.0f)
        levelMiddle = floatArrayOf(1.0f, 1.0f, 1.0f)
        levelMaximum = floatArrayOf(1.0f, 1.0f, 1.0f)
        minOutput = floatArrayOf(0.0f, 0.0f, 0.0f)
        maxOutput = floatArrayOf(1.0f, 1.0f, 1.0f)
    }

    override fun loadFragmentShader(): String = LEVELS_FRAGMENT_SHADER

    private fun uploadValues() {
        levelMinimum = levelMinimum
        levelMiddle = levelMiddle
        levelMaximum = levelMaximum
        minOutput = minOutput
        maxOutput = maxOutput
    }

    fun adjustAll(min: Float, mid: Float, max: Float, minOut: Float = 0.0f, maxOut: Float = 1.0f) {
        adjustRed(min, mid, max, minOut, maxOut)
        adjustGreen(min, mid, max, minOut, maxOut)
        adjustBlue(min, mid, max, minOut, maxOut)
        uploadValues()
    }

    fun adjustRed(min: Float, mid: Float, max: Float, minOut: Float = 0.0f, maxOut: Float = 1.0f) {
        levelMinimum[0] = min
        levelMiddle[0] = mid
        levelMaximum[0] = max
        minOutput[0] = minOut
        maxOutput[0] = maxOut
        uploadValues()
    }

    fun adjustGreen(min: Float, mid: Float, max: Float, minOut: Float = 0.0f, maxOut: Float = 1.0f) {
        levelMinimum[1] = min
        levelMiddle[1] = mid
        levelMaximum[1] = max
        minOutput[1] = minOut
        maxOutput[1] = maxOut
        uploadValues()
    }

    fun adjustBlue(min: Float, mid: Float, max: Float, minOut: Float = 0.0f, maxOut: Float = 1.0f) {
        levelMinimum[2] = min
        levelMiddle[2] = mid
        levelMaximum[2] = max
        minOutput[2] = minOut
        maxOutput[2] = maxOut
        uploadValues()
    }

    companion object {
        @Language("glsl")
        private const val LEVELS_FRAGMENT_SHADER = """
            precision mediump float;
            varying vec2 textureCoordinate;
            uniform sampler2D inputImageTexture;
            uniform mediump vec3 levelMinimum;
            uniform mediump vec3 levelMiddle;
            uniform mediump vec3 levelMaximum;
            uniform mediump vec3 minOutput;
            uniform mediump vec3 maxOutput;

            void main() {
                mediump vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
                gl_FragColor = vec4(
                    mix(minOutput, maxOutput, 
                        pow(
                            min(
                                max(textureColor.rgb - levelMinimum, vec3(0.0)) / (levelMaximum - levelMinimum),
                                vec3(1.0)
                            ),
                            1.0 / levelMiddle
                        )
                    ),
                    textureColor.a
                );
            }
        """
    }
}
