package com.cardinalblue.kraftshade.demo.ui.screen.shaders

import androidx.compose.runtime.Composable
import com.cardinalblue.kraftshade.shader.builtin.LookUpTableKraftShader

@Composable
fun LookUpTableShaderTestScreen() {
    SimpleTwoInputShaderTestScreen(
        sampleAssetPath2 = "sample/lookup_bw1.jpg"
    ) { _, _, _ ->
        LookUpTableKraftShader()
    }
}
