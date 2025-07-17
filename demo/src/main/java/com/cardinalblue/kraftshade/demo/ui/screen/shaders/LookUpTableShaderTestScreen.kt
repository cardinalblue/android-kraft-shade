package com.cardinalblue.kraftshade.demo.ui.screen.shaders

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.demo.util.aspectRatio
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.EmbossKraftShader
import com.cardinalblue.kraftshade.shader.builtin.LookUpTableKraftShader

@Composable
fun LookUpTableShaderTestScreen() {
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    val state = rememberKraftShadeEffectState()
    KraftShadeEffectView(
        modifier = Modifier.aspectRatio(aspectRatio),
        state = state,
    )

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        state.runGlTask { windowSurface ->
            val shader = LookUpTableKraftShader()

            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.aspectRatio
            val lutBitmap = context.loadBitmapFromAsset("sample/lookup_bw1.jpg")
            shader.setInputTexture(bitmap.asTexture())
            shader.setSecondInputTexture(lutBitmap.asTexture())
            shader.drawTo(windowSurface)
        }
    }
}
