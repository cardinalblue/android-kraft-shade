package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.shader.builtin.DoNothingKraftShader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ResizeTestScreen() {
    var image by remember { mutableStateOf<Bitmap?>(null) }
    val aspectRatio by remember {
        derivedStateOf {
            image?.let {
                it.width.toFloat() / it.height
            }
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }

    val effectState = rememberKraftShadeEffectState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = scale)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                KraftShadeEffectView(
                    modifier = Modifier
                        .aspectRatio(aspectRatio ?: 1f),
                    state = effectState
                )
            }
        }

        Slider(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
            value = scale,
            onValueChange = { scale = it},
            valueRange = 0.001f..1f,
            steps = 100,
        )
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        val inputBitmap = withContext(Dispatchers.IO) {
            context.assets.open("sample/cat.jpg").use {
                BitmapFactory.decodeStream(it)
            }
        }

        image = inputBitmap

        effectState.setEffect { windowSurfaceBuffer ->
            pipeline(windowSurfaceBuffer) {
                val inputTexture = sampledBitmapTextureProvider { inputBitmap }
                serialSteps(
                    inputTexture,
                    windowSurfaceBuffer,
                ) {
                    step(DoNothingKraftShader())
                }
            }
        }
    }
}
