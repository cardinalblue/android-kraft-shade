package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

abstract class MixBlendKraftShader(
    mixturePercent: Float = 0.5f,
) : TwoTextureInputKraftShader() {
    var mixturePercent: Float by GlUniformDelegate("mixturePercent")
}
