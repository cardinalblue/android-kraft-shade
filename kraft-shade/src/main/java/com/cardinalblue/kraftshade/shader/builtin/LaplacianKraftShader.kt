package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlMat3

class LaplacianKraftShader : Convolution3x3WithColorOffsetKraftShader(
    convolutionMatrix = GlMat3(
        0.5f, 1.0f, 0.5f,
        1.0f, -6.0f, 1.0f,
        0.5f, 1.0f, 0.5f
    ),
    colorOffset = floatArrayOf(0.5f, 0.5f, 0.5f, 1.0f)
)
