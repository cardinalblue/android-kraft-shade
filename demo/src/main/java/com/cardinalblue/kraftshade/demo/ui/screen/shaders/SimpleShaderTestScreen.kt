package com.cardinalblue.kraftshade.demo.ui.screen.shaders

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.cardinalblue.kraftshade.demo.ui.screen.basic_env.BasicGlEnvScreen
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer

@Composable
fun SimpleShaderTestScreen(
    sampleAssetPath: String = "sample/cat.jpg",
    createShader: (Context, Bitmap) -> TextureInputKraftShader,
) {
    val context = LocalContext.current
    BasicGlEnvScreen {
        val bitmap = context.loadBitmapFromAsset(sampleAssetPath)
        val texture = LoadedTexture(bitmap)
        val resultBuffer = TextureBuffer(bitmap.width, bitmap.height)
        val shader = createShader(context, bitmap)

        shader.inputTextureId = texture.textureId
        shader.drawTo(resultBuffer)
        resultBuffer.getBitmap()
    }
}
