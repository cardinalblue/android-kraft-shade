package com.cardinalblue.kraftshade.demo.ui.screen.color

import androidx.compose.runtime.Composable
import com.cardinalblue.kraftshade.shader.builtin.ColorDodgeBlendKraftShader

@Composable
fun ColorDodgeBlendShaderScreen() {
    TwoTextureInputShaderScreen(
        shaderCreator = { ColorDodgeBlendKraftShader() }
    )
}