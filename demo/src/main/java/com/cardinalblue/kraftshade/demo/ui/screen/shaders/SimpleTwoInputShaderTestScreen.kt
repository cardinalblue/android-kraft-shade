package com.cardinalblue.kraftshade.demo.ui.screen.shaders

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.cardinalblue.kraftshade.demo.ui.screen.basic_env.BasicGlEnvScreen
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer

@Composable
fun SimpleTwoInputShaderTestScreen(
    sampleAssetPath1: String = "sample/cat.jpg",
    sampleAssetPath2: String = "sample/cat2.jpg",
    createShader: (Context, Bitmap, Bitmap) -> TwoTextureInputKraftShader,
) {
    val context = LocalContext.current
    BasicGlEnvScreen {
        val bitmap1 = context.loadBitmapFromAsset(sampleAssetPath1)
        val texture1 = LoadedTexture(bitmap1)

        val bitmap2 = context.loadBitmapFromAsset(sampleAssetPath2)
        val texture2 = LoadedTexture(bitmap2)

        val resultBuffer = TextureBuffer(bitmap1.width * 2, bitmap1.height * 2)

        val shader = createShader(context, bitmap1, bitmap2).apply {
            setInputTexture(texture1)
            setSecondInputTexture(texture2)
            draw(texture1, texture2, resultBuffer.size, false)
        }

        shader.drawTo(resultBuffer)
        resultBuffer.getBitmap()
    }
}
