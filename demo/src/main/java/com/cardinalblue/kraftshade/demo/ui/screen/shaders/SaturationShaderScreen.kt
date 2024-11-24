package com.cardinalblue.kraftshade.demo.ui.screen.shaders

import androidx.compose.runtime.Composable
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader

@Composable
fun SaturationShaderScreen() {
    SimpleShaderTestScreen { _, _ ->
        SaturationKraftShader()
    }
}