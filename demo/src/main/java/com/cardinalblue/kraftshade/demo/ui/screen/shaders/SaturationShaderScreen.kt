package com.cardinalblue.kraftshade.demo.ui.screen.shaders

import androidx.compose.runtime.Composable
import kotlinx.coroutines.runBlocking
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader

@Composable
fun SaturationShaderScreen() {
    SimpleShaderTestScreen { _, _ ->
        SaturationKraftShader()
    }
}