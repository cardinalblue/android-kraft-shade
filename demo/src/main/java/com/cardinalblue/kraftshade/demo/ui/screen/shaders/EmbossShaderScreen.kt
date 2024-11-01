package com.cardinalblue.kraftshade.demo.ui.screen.shaders

import androidx.compose.runtime.Composable
import com.cardinalblue.kraftshade.shader.builtin.EmbossKraftShader

@Composable
fun EmbossShaderScreen() {
    SimpleShaderTestScreen { _, bitmap ->
        EmbossKraftShader().apply {
            intensity = 50f
            texelWidth = 1f / bitmap.width
            texelHeight = 1f / bitmap.height
        }
    }
}
