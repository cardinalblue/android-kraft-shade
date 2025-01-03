package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader

@Composable
fun ResizeTestWindow() {
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var scale by remember { mutableFloatStateOf(1f) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = scale),
                contentAlignment = Alignment.Center
            ) {
                KraftShadeEffectView(
                    modifier = Modifier
                        .aspectRatio(aspectRatio),
                    state = state
                )
            }
        }

        Slider(value = scale, onValueChange = {
            scale = it
        })
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        val image = context.loadBitmapFromAsset("sample/cat.jpg")
        aspectRatio = image.width.toFloat() / image.height

        state.setEffect { windowSurface ->
            val inputTexture = image.asTexture()
            pipeline(windowSurface) {
                serialSteps(inputTexture, windowSurface) {
                    step(SaturationKraftShader(0f))
                }
            }
        }
    }
}
