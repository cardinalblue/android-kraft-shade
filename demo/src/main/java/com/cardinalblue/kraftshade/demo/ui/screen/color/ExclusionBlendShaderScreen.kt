package com.cardinalblue.kraftshade.demo.ui.screen.color

import androidx.compose.runtime.Composable
import com.cardinalblue.kraftshade.shader.builtin.ExclusionBlendKraftShader

@Composable
fun ExclusionBlendShaderScreen() {
    TwoTextureInputShaderScreen(
        shaderCreator = { ExclusionBlendKraftShader() }
    )
}