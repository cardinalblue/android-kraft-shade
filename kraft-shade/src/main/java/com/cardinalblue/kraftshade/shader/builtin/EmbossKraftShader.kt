package com.cardinalblue.kraftshade.shader.builtin

class EmbossKraftShader : Convolution3x3KraftShader() {
    var intensity: Float = 1f
        set(value) {
            field = value
            setConvolutionMatrix(floatArrayOf(
                intensity * (-2.0f), -intensity, 0.0f,
                -intensity, 1.0f, intensity,
                0.0f, intensity, intensity * 2.0f,
            ))
        }
}
