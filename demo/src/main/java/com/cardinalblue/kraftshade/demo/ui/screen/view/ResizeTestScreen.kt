package com.cardinalblue.kraftshade.demo.ui.screen.view

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
import com.cardinalblue.kraftshade.shader.buffer.sampledBitmapTextureProvider
import com.cardinalblue.kraftshade.shader.builtin.DrawTextureKraftShader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Demo screen that demonstrates resizing capabilities of KraftShadeEffectView.
 *
 * This screen showcases how to use [KraftShadeEffectView] with dynamic resizing
 * while maintaining the proper aspect ratio of the source image.
 *
 * Features demonstrated:
 * - Using [KraftShadeEffectState] with [KraftShadeEffectView]
 * - Loading and displaying an image from assets
 * - Dynamically resizing the view while preserving content
 * - Using [DrawTextureKraftShader] for direct image rendering
 * - Maintaining proper aspect ratio during resizing
 *
 * Implementation details:
 * - Uses nested [Box] composables for controlled resizing
 * - Applies [fillMaxWidth] with a dynamic fraction for resizing
 * - Uses [sampledBitmapTextureProvider] to load the image texture
 * - Creates a simple pipeline with [DrawTextureKraftShader]
 * - Uses [derivedStateOf] for calculating aspect ratio
 *
 * User interactions:
 * - Slider to adjust the size of the view from very small (0.1%) to full width (100%)
 * - The image maintains its aspect ratio regardless of the view size
 *
 * Technical background:
 * - Demonstrates how KraftShade handles texture sampling at different sizes
 * - Shows the flexibility of the rendering pipeline for various view dimensions
 */
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
                    step(DrawTextureKraftShader())
                }
            }
        }
    }
}
