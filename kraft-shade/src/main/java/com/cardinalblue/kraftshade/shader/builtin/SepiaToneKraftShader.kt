package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlMat4

class SepiaToneKraftShader(intensity: Float = 1.0f) : ColorMatrixKraftShader(
    intensity = intensity,
    colorMatrix = GlMat4(
        0.3588f, 0.7044f, 0.1368f, 0.0f,
        0.2990f, 0.5870f, 0.1140f, 0.0f,
        0.2392f, 0.4696f, 0.0912f, 0.0f,
        0f, 0f, 0f, 1.0f
    )
)