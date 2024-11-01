package com.cardinalblue.kraftshade.demo.ui.screen.basic_env

import android.opengl.GLES20
import androidx.compose.runtime.Composable
import com.cardinalblue.kraftshade.model.Color
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer
import com.cardinalblue.kraftshade.shader.builtin.DrawCircleKraftShader

@Composable
fun BlendingExampleScreen() {
    BasicGlEnvScreen {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        DrawCircleKraftShader(
            color = Color.Red,
            backgroundColor = Color.White.alterAlpha(0.5f),
        ).use { shader ->
            TextureBuffer(500, 500).use { buffer ->
                shader.drawTo(buffer)
                buffer.getBitmap()
            }
        }
    }
}