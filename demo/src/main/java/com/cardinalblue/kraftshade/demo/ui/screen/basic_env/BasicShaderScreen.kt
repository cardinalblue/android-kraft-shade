package com.cardinalblue.kraftshade.demo.ui.screen.basic_env

import androidx.compose.runtime.Composable
import com.cardinalblue.kraftshade.demo.shader.DrawCircleKraftShader
import com.cardinalblue.kraftshade.model.GlColor
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer
import com.cardinalblue.kraftshade.util.use

@Composable
fun BasicShaderScreen() {
    BasicGlEnvScreen {
        DrawCircleKraftShader(
            color = GlColor.Red,
            backgroundColor = GlColor.Green,
        ).use { shader ->
            shader.debug = true
            // for verifying the runOnDraw only happen once with the same key. There should be only
            // one log like below:
            // D/KraftShader [DrawCircleKraftShader] runOnDraw: color
            shader.color = GlColor.Green
            shader.color = GlColor.Yellow
            shader.color = GlColor.Blue
            TextureBuffer(500, 500).use { buffer ->
                shader.drawTo(buffer)
                buffer.getBitmap()
            }
        }
    }
}