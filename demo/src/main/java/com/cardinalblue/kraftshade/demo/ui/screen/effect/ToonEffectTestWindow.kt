package com.cardinalblue.kraftshade.demo.ui.screen.effect

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.demo.ui.components.ParameterSlider
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.ToonKraftShader

/**
 * Demo screen that demonstrates the Toon/Cartoon effect in KraftShade.
 *
 * This screen showcases how to use [KraftShadeEffectView] with [ToonKraftShader]
 * to apply a cartoon/toon effect to an image with adjustable edge detection and
 * color quantization parameters.
 *
 * Features demonstrated:
 * - Using [KraftShadeEffectState] with [KraftShadeEffectView]
 * - Loading and displaying an image from assets
 * - Applying [ToonKraftShader] for cartoon-style rendering
 * - Interactive adjustment of shader parameters using sliders
 *
 * Implementation details:
 * - Uses [setEffect] to configure the rendering pipeline
 * - Demonstrates creating a serial pipeline with steps
 * - Shows how to update shader parameters in real-time
 * - Maintains proper aspect ratio of the source image
 *
 * User interactions:
 * - Slider to adjust the edge detection threshold from 0.0 to 1.0
 * - Slider to adjust the color quantization levels from 2 to 20
 *
 * Technical background:
 * - The toon effect combines edge detection with color quantization
 * - Edge threshold controls the sensitivity of edge detection
 * - Quantization levels control how many distinct colors appear in the output
 */
@Composable
fun ToonEffectTestWindow() {
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var threshold by remember { mutableFloatStateOf(0.2f) }
    var quantizationLevels by remember { mutableFloatStateOf(10.0f) }

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            KraftShadeEffectView(
                modifier = Modifier
                    .aspectRatio(aspectRatio),
                state = state
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ParameterSlider(
                label = "Edge Threshold",
                value = threshold,
                onValueChange = {
                    threshold = it
                    state.requestRender()
                },
                valueRange = 0f..1f
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSlider(
                label = "Color Quantization",
                value = quantizationLevels,
                onValueChange = {
                    quantizationLevels = it
                    state.requestRender()
                },
                valueRange = 2f..20f
            )
        }
    }

    LaunchedEffect(Unit) {
        state.setEffect { windowSurface ->
            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.width.toFloat() / bitmap.height

            pipeline(windowSurface) {
                serialSteps(
                    inputTexture = bitmap.asTexture(),
                    targetBuffer = windowSurface,
                ) {
                    step(ToonKraftShader()) { shader ->
                        shader.threshold = threshold
                        shader.quantizationLevels = quantizationLevels
                    }
                }
            }
        }
    }
}
