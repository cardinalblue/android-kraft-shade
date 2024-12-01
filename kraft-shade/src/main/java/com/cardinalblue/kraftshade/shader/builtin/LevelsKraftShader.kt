package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlFloatArray
import com.cardinalblue.kraftshade.model.GlFloatArrayDelegate
import com.cardinalblue.kraftshade.model.glFloatArrayOf
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

class LevelsKraftShader : TextureInputKraftShader() {
    var levelMinimum: GlFloatArray by GlFloatArrayDelegate("levelMinimum")
    var levelMiddle: GlFloatArray by GlFloatArrayDelegate("levelMiddle")
    var levelMaximum: GlFloatArray by GlFloatArrayDelegate("levelMaximum")
    var minOutput: GlFloatArray by GlFloatArrayDelegate("minOutput")
    var maxOutput: GlFloatArray by GlFloatArrayDelegate("maxOutput")

    init {
        levelMinimum = glFloatArrayOf(0.0f, 0.0f, 0.0f)
        levelMiddle = glFloatArrayOf(1.0f, 1.0f, 1.0f)
        levelMaximum = glFloatArrayOf(1.0f, 1.0f, 1.0f)
        minOutput = glFloatArrayOf(0.0f, 0.0f, 0.0f)
        maxOutput = glFloatArrayOf(1.0f, 1.0f, 1.0f)
    }

    override fun loadFragmentShader(): String = LEVELS_FRAGMENT_SHADER

    fun adjustAll(min: Float, mid: Float, max: Float, minOut: Float = 0.0f, maxOut: Float = 1.0f) {
        adjustRed(min, mid, max, minOut, maxOut)
        adjustGreen(min, mid, max, minOut, maxOut)
        adjustBlue(min, mid, max, minOut, maxOut)
    }

    fun adjustRed(min: Float, mid: Float, max: Float, minOut: Float = 0.0f, maxOut: Float = 1.0f) {
        levelMinimum[0] = min
        levelMiddle[0] = mid
        levelMaximum[0] = max
        minOutput[0] = minOut
        maxOutput[0] = maxOut
    }

    fun adjustGreen(min: Float, mid: Float, max: Float, minOut: Float = 0.0f, maxOut: Float = 1.0f) {
        levelMinimum[1] = min
        levelMiddle[1] = mid
        levelMaximum[1] = max
        minOutput[1] = minOut
        maxOutput[1] = maxOut
    }

    fun adjustBlue(min: Float, mid: Float, max: Float, minOut: Float = 0.0f, maxOut: Float = 1.0f) {
        levelMinimum[2] = min
        levelMiddle[2] = mid
        levelMaximum[2] = max
        minOutput[2] = minOut
        maxOutput[2] = maxOut
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
