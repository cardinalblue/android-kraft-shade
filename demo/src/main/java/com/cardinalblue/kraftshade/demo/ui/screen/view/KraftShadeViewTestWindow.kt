package com.cardinalblue.kraftshade.demo.ui.screen.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.cardinalblue.kraftshade.compose.*
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun KraftShadeViewTestWindow() {
    val state: KraftShadeState = rememberKraftShadeState()
    var image: Bitmap? by remember { mutableStateOf(null) }
    var imageAspectRatio: Float by remember { mutableFloatStateOf(1.0f) }

    KraftShadeView(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(imageAspectRatio),
        state = state
    )

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        withContext(Dispatchers.IO) {
            image = context.assets.open("sample/cat.jpg").use {
                BitmapFactory.decodeStream(it)
            }.also {
                imageAspectRatio = it.width.toFloat() / it.height
            }
        }
    }

    LaunchedEffect(state, image) {
        val image = image ?: return@LaunchedEffect
        state.runGlTask { windowSurface ->
            SaturationKraftShader(0f).apply {
                setInputTexture(LoadedTexture(image))
                drawTo(windowSurface)
            }
        }
    }
}
