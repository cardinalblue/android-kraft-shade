package com.cardinalblue.kraftshade.demo.ui.screen.basic_env

import androidx.compose.runtime.Composable
import com.cardinalblue.kraftshade.demo.shader.DrawCircleKraftShader
import com.cardinalblue.kraftshade.model.GlColor
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer
import com.cardinalblue.kraftshade.util.use

@Composable
fun BlendingExampleScreen() {
    BasicGlEnvScreen {
        DrawCircleKraftShader(
            color = GlColor.Red,
            backgroundColor = GlColor.White.copyColor(a = 0.5f),
        ).use { shader ->
            TextureBuffer(500, 500).use { buffer ->
                shader.drawTo(buffer)
                buffer.getBitmap()
            }
        }
    }
}