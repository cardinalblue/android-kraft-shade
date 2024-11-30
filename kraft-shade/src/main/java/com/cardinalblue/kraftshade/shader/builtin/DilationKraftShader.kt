package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.SingleDirectionForTwoPassSamplingKraftShader

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

    companion object {
        private const val VERTEX_SHADER_1 = """
            attribute vec4 position;
            attribute vec2 inputTextureCoordinate;

            uniform vec2 texelSize;

            varying vec2 centerTextureCoordinate;
            varying vec2 oneStepPositiveTextureCoordinate;
            varying vec2 oneStepNegativeTextureCoordinate;

            void main() {
                gl_Position = position;

                centerTextureCoordinate = inputTextureCoordinate;
                oneStepNegativeTextureCoordinate = inputTextureCoordinate - texelSize;
                oneStepPositiveTextureCoordinate = inputTextureCoordinate + texelSize;
            }
        """

        private const val VERTEX_SHADER_2 = """
            attribute vec4 position;
            attribute vec2 inputTextureCoordinate;

            uniform vec2 texelSize;

            varying vec2 centerTextureCoordinate;
            varying vec2 oneStepPositiveTextureCoordinate;
            varying vec2 oneStepNegativeTextureCoordinate;
            varying vec2 twoStepsPositiveTextureCoordinate;
            varying vec2 twoStepsNegativeTextureCoordinate;

            void main() {
                gl_Position = position;

                centerTextureCoordinate = inputTextureCoordinate;
                oneStepNegativeTextureCoordinate = inputTextureCoordinate - texelSize;
                oneStepPositiveTextureCoordinate = inputTextureCoordinate + texelSize;
                twoStepsNegativeTextureCoordinate = inputTextureCoordinate - (texelSize * 2.0);
                twoStepsPositiveTextureCoordinate = inputTextureCoordinate + (texelSize * 2.0);
            }
        """

        private const val VERTEX_SHADER_3 = """
            attribute vec4 position;
            attribute vec2 inputTextureCoordinate;

            uniform vec2 texelSize;

            varying vec2 centerTextureCoordinate;
            varying vec2 oneStepPositiveTextureCoordinate;
            varying vec2 oneStepNegativeTextureCoordinate;
            varying vec2 twoStepsPositiveTextureCoordinate;
            varying vec2 twoStepsNegativeTextureCoordinate;
            varying vec2 threeStepsPositiveTextureCoordinate;
            varying vec2 threeStepsNegativeTextureCoordinate;

            void main() {
                gl_Position = position;

                centerTextureCoordinate = inputTextureCoordinate;
                oneStepNegativeTextureCoordinate = inputTextureCoordinate - texelSize;
                oneStepPositiveTextureCoordinate = inputTextureCoordinate + texelSize;
                twoStepsNegativeTextureCoordinate = inputTextureCoordinate - (texelSize * 2.0);
                twoStepsPositiveTextureCoordinate = inputTextureCoordinate + (texelSize * 2.0);
                threeStepsNegativeTextureCoordinate = inputTextureCoordinate - (texelSize * 3.0);
                threeStepsPositiveTextureCoordinate = inputTextureCoordinate + (texelSize * 3.0);
            }
        """

        private const val VERTEX_SHADER_4 = """
            attribute vec4 position;
            attribute vec2 inputTextureCoordinate;

            uniform vec2 texelSize;

            varying vec2 centerTextureCoordinate;
            varying vec2 oneStepPositiveTextureCoordinate;
            varying vec2 oneStepNegativeTextureCoordinate;
            varying vec2 twoStepsPositiveTextureCoordinate;
            varying vec2 twoStepsNegativeTextureCoordinate;
            varying vec2 threeStepsPositiveTextureCoordinate;
            varying vec2 threeStepsNegativeTextureCoordinate;
            varying vec2 fourStepsPositiveTextureCoordinate;
            varying vec2 fourStepsNegativeTextureCoordinate;

            void main() {
                gl_Position = position;

                centerTextureCoordinate = inputTextureCoordinate;
                oneStepNegativeTextureCoordinate = inputTextureCoordinate - texelSize;
                oneStepPositiveTextureCoordinate = inputTextureCoordinate + texelSize;
                twoStepsNegativeTextureCoordinate = inputTextureCoordinate - (texelSize * 2.0);
                twoStepsPositiveTextureCoordinate = inputTextureCoordinate + (texelSize * 2.0);
                threeStepsNegativeTextureCoordinate = inputTextureCoordinate - (texelSize * 3.0);
                threeStepsPositiveTextureCoordinate = inputTextureCoordinate + (texelSize * 3.0);
                fourStepsNegativeTextureCoordinate = inputTextureCoordinate - (texelSize * 4.0);
                fourStepsPositiveTextureCoordinate = inputTextureCoordinate + (texelSize * 4.0);
            }
        """

        private const val FRAGMENT_SHADER_1 = """
            precision mediump float;

            varying vec2 centerTextureCoordinate;
            varying vec2 oneStepPositiveTextureCoordinate;
            varying vec2 oneStepNegativeTextureCoordinate;

            uniform sampler2D inputImageTexture;

            void main() {
                float centerIntensity = texture2D(inputImageTexture, centerTextureCoordinate).r;
                float oneStepPositiveIntensity = texture2D(inputImageTexture, oneStepPositiveTextureCoordinate).r;
                float oneStepNegativeIntensity = texture2D(inputImageTexture, oneStepNegativeTextureCoordinate).r;

                float maxValue = max(centerIntensity, oneStepPositiveIntensity);
                maxValue = max(maxValue, oneStepNegativeIntensity);

                gl_FragColor = vec4(vec3(maxValue), 1.0);
            }
        """

        private const val FRAGMENT_SHADER_2 = """
            precision mediump float;

            varying vec2 centerTextureCoordinate;
            varying vec2 oneStepPositiveTextureCoordinate;
            varying vec2 oneStepNegativeTextureCoordinate;
            varying vec2 twoStepsPositiveTextureCoordinate;
            varying vec2 twoStepsNegativeTextureCoordinate;

            uniform sampler2D inputImageTexture;

            void main() {
                float centerIntensity = texture2D(inputImageTexture, centerTextureCoordinate).r;
                float oneStepPositiveIntensity = texture2D(inputImageTexture, oneStepPositiveTextureCoordinate).r;
                float oneStepNegativeIntensity = texture2D(inputImageTexture, oneStepNegativeTextureCoordinate).r;
                float twoStepsPositiveIntensity = texture2D(inputImageTexture, twoStepsPositiveTextureCoordinate).r;
                float twoStepsNegativeIntensity = texture2D(inputImageTexture, twoStepsNegativeTextureCoordinate).r;

                float maxValue = max(centerIntensity, oneStepPositiveIntensity);
                maxValue = max(maxValue, oneStepNegativeIntensity);
                maxValue = max(maxValue, twoStepsPositiveIntensity);
                maxValue = max(maxValue, twoStepsNegativeIntensity);

                gl_FragColor = vec4(vec3(maxValue), 1.0);
            }
        """

        private const val FRAGMENT_SHADER_3 = """
            precision mediump float;

            varying vec2 centerTextureCoordinate;
            varying vec2 oneStepPositiveTextureCoordinate;
            varying vec2 oneStepNegativeTextureCoordinate;
            varying vec2 twoStepsPositiveTextureCoordinate;
            varying vec2 twoStepsNegativeTextureCoordinate;
            varying vec2 threeStepsPositiveTextureCoordinate;
            varying vec2 threeStepsNegativeTextureCoordinate;

            uniform sampler2D inputImageTexture;

            void main() {
                float centerIntensity = texture2D(inputImageTexture, centerTextureCoordinate).r;
                float oneStepPositiveIntensity = texture2D(inputImageTexture, oneStepPositiveTextureCoordinate).r;
                float oneStepNegativeIntensity = texture2D(inputImageTexture, oneStepNegativeTextureCoordinate).r;
                float twoStepsPositiveIntensity = texture2D(inputImageTexture, twoStepsPositiveTextureCoordinate).r;
                float twoStepsNegativeIntensity = texture2D(inputImageTexture, twoStepsNegativeTextureCoordinate).r;
                float threeStepsPositiveIntensity = texture2D(inputImageTexture, threeStepsPositiveTextureCoordinate).r;
                float threeStepsNegativeIntensity = texture2D(inputImageTexture, threeStepsNegativeTextureCoordinate).r;

                float maxValue = max(centerIntensity, oneStepPositiveIntensity);
                maxValue = max(maxValue, oneStepNegativeIntensity);
                maxValue = max(maxValue, twoStepsPositiveIntensity);
                maxValue = max(maxValue, twoStepsNegativeIntensity);
                maxValue = max(maxValue, threeStepsPositiveIntensity);
                maxValue = max(maxValue, threeStepsNegativeIntensity);

                gl_FragColor = vec4(vec3(maxValue), 1.0);
            }
        """

        private const val FRAGMENT_SHADER_4 = """
            precision mediump float;

            varying vec2 centerTextureCoordinate;
            varying vec2 oneStepPositiveTextureCoordinate;
            varying vec2 oneStepNegativeTextureCoordinate;
            varying vec2 twoStepsPositiveTextureCoordinate;
            varying vec2 twoStepsNegativeTextureCoordinate;
            varying vec2 threeStepsPositiveTextureCoordinate;
            varying vec2 threeStepsNegativeTextureCoordinate;
            varying vec2 fourStepsPositiveTextureCoordinate;
            varying vec2 fourStepsNegativeTextureCoordinate;

            uniform sampler2D inputImageTexture;

            void main() {
                float centerIntensity = texture2D(inputImageTexture, centerTextureCoordinate).r;
                float oneStepPositiveIntensity = texture2D(inputImageTexture, oneStepPositiveTextureCoordinate).r;
                float oneStepNegativeIntensity = texture2D(inputImageTexture, oneStepNegativeTextureCoordinate).r;
                float twoStepsPositiveIntensity = texture2D(inputImageTexture, twoStepsPositiveTextureCoordinate).r;
                float twoStepsNegativeIntensity = texture2D(inputImageTexture, twoStepsNegativeTextureCoordinate).r;
                float threeStepsPositiveIntensity = texture2D(inputImageTexture, threeStepsPositiveTextureCoordinate).r;
                float threeStepsNegativeIntensity = texture2D(inputImageTexture, threeStepsNegativeTextureCoordinate).r;
                float fourStepsPositiveIntensity = texture2D(inputImageTexture, fourStepsPositiveTextureCoordinate).r;
                float fourStepsNegativeIntensity = texture2D(inputImageTexture, fourStepsNegativeTextureCoordinate).r;

                float maxValue = max(centerIntensity, oneStepPositiveIntensity);
                maxValue = max(maxValue, oneStepNegativeIntensity);
                maxValue = max(maxValue, twoStepsPositiveIntensity);
                maxValue = max(maxValue, twoStepsNegativeIntensity);
                maxValue = max(maxValue, threeStepsPositiveIntensity);
                maxValue = max(maxValue, threeStepsNegativeIntensity);
                maxValue = max(maxValue, fourStepsPositiveIntensity);
                maxValue = max(maxValue, fourStepsNegativeIntensity);

                gl_FragColor = vec4(vec3(maxValue), 1.0);
            }
        """

        private fun getVertexShader(radius: Int): String = when (radius) {
            1 -> VERTEX_SHADER_1
            2 -> VERTEX_SHADER_2
            3 -> VERTEX_SHADER_3
            else -> VERTEX_SHADER_4
        }

        private fun getFragmentShader(radius: Int): String = when (radius) {
            1 -> FRAGMENT_SHADER_1
            2 -> FRAGMENT_SHADER_2
            3 -> FRAGMENT_SHADER_3
            else -> FRAGMENT_SHADER_4
        }
    }

    override fun loadVertexShader(): String {
        return getVertexShader(radius)
    }

    override fun loadFragmentShader(): String {
        return getFragmentShader(radius)
    }
}
