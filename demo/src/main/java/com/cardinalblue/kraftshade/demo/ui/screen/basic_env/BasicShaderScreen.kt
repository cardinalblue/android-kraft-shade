package com.cardinalblue.kraftshade.demo.ui.screen.basic_env

import androidx.compose.runtime.Composable
import com.cardinalblue.kraftshade.model.Color
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer
import com.cardinalblue.kraftshade.shader.builtin.DrawCircleKraftShader

@Composable
fun BasicShaderScreen() {
    BasicGlEnvScreen {
        DrawCircleKraftShader(
            color = Color.Red,
            backgroundColor = Color.Green,
        ).use { shader ->
            shader.debug = true
            // for verifying the runOnDraw only happen once with the same key. There should be only
            // one log like below:
            // D/KraftShader [DrawCircleKraftShader] runOnDraw: color
            shader.setColor(Color.Green)
            shader.setColor(Color.Yellow)
            shader.setColor(Color.Blue)
            TextureBuffer(500, 500).use { buffer ->
                shader.drawTo(buffer)
                buffer.getBitmap()
            }
        }
    }
}