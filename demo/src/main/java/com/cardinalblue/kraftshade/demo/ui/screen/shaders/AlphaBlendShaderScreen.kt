package com.cardinalblue.kraftshade.demo.ui.screen.shaders

import androidx.compose.runtime.Composable
import com.cardinalblue.kraftshade.shader.builtin.AlphaBlendKraftShader

@Composable
fun AlphaBlendShaderScreen() {
    SimpleTwoInputShaderTestScreen { _, bitmap1, bitmap2 ->
        AlphaBlendKraftShader().apply {
            mixRatio = 0.5f
            val aspectRatio1 = bitmap1.width.toFloat() / bitmap1.height
            val aspectRatio2 = bitmap2.width.toFloat() / bitmap2.height

            val scaleOnXToFillBitmap2 = aspectRatio2 / aspectRatio1
            updateTexture2SamplingTransformMatrix {
                scale2D(1f / scaleOnXToFillBitmap2, 1f, 0.5f, 0.5f)
            }
        }
    }
}
