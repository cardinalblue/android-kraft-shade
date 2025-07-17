package com.cardinalblue.kraftshade.demo.ui.screen.shaders

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.KraftShadeView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.compose.rememberKraftShadeState
import com.cardinalblue.kraftshade.demo.util.aspectRatio
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.EmbossKraftShader

/**
 * Demo screen that demonstrates the Emboss shader effect in KraftShade.
 *
 * This screen showcases how to use [KraftShadeEffectView] with [EmbossKraftShader]
 * to apply an emboss effect to an image, with adjustable intensity.
 *
 * Features demonstrated:
 * - Using [KraftShadeEffectState] with [KraftShadeEffectView]
 * - Loading and displaying an image from assets
 * - Applying [EmbossKraftShader] to create an emboss effect
 * - Interactive adjustment of shader parameters using a slider
 *
 * Implementation details:
 * - Uses [setEffect] to configure the rendering pipeline
 * - Demonstrates creating a serial pipeline with steps
 * - Shows how to update shader parameters in real-time
 * - Maintains proper aspect ratio of the source image
 *
 * User interactions:
 * - Slider to adjust the emboss effect intensity from 0.0 to 1.0
 */
@Composable
fun EmbossShaderScreen() {
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    val state = rememberKraftShadeEffectState()
    var intensity by remember { mutableFloatStateOf(0.5f) }

    Column(modifier = Modifier.fillMaxSize()) {
        KraftShadeEffectView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio),
            state = state,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = intensity,
            valueRange = 0f..1f,
            onValueChange = { newValue ->
                intensity = newValue
                state.requestRender()
            }
        )
    }


    val context = LocalContext.current
    LaunchedEffect(Unit) {
        state.setEffect { windowBuffer ->
            pipeline(windowBuffer) {
                val shader = EmbossKraftShader().apply {
                    this.intensity = intensity
                }
                val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
                aspectRatio = bitmap.aspectRatio
                shader.setInputTexture(bitmap.asTexture())

                serialSteps(bitmap.asTexture(), targetBuffer = windowBuffer) {
                    step(shader) { _shader ->
                        _shader.intensity = intensity
                    }
                }
            }
        }
    }
}
