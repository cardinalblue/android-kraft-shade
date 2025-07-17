package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.aspectRatio
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.components.CollapsibleSection
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.components.ParameterSlider
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.KuwaharaKraftShader

/**
 * Demo screen that demonstrates the Kuwahara filter effect in KraftShade.
 *
 * This screen showcases how to use [KraftShadeEffectView] with [KuwaharaKraftShader]
 * to apply a Kuwahara filter to an image with adjustable radius.
 *
 * Features demonstrated:
 * - Using [KraftShadeEffectState] with [KraftShadeEffectView]
 * - Loading and displaying an image from assets
 * - Applying [KuwaharaKraftShader] for edge-preserving smoothing
 * - Interactive adjustment of filter radius using a slider
 * - Using [CollapsibleSection] for organizing UI controls
 *
 * Implementation details:
 * - Uses [setEffect] to configure the rendering pipeline
 * - Demonstrates creating a serial pipeline with steps
 * - Shows how to update shader parameters in real-time
 * - Maintains proper aspect ratio of the source image
 *
 * User interactions:
 * - Slider to adjust the filter radius from 1 to 10
 *
 * Technical background:
 * - The Kuwahara filter is a non-linear smoothing filter that preserves edges
 * - It works by dividing the area around each pixel into quadrants, computing the
 *   mean and variance in each quadrant, and replacing the pixel with the mean of
 *   the quadrant with the smallest variance
 * - Larger radius values create a more pronounced painterly effect
 */
@Composable
fun KuwaharaTestWindow() {
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var radius by remember { mutableIntStateOf(3) }
    var effectExpanded by remember { mutableStateOf(true) }

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
                modifier = Modifier.aspectRatio(aspectRatio),
                state = state
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            CollapsibleSection(
                title = "Kuwahara Effect",
                expanded = effectExpanded,
                onExpandedChange = { effectExpanded = it }
            ) {
                ParameterSlider(
                    label = "Radius",
                    value = radius.toFloat(),
                    onValueChange = { 
                        radius = it.toInt()
                        state.requestRender()
                    },
                    valueRange = 1f..10f
                )
            }
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
                    step(KuwaharaKraftShader()) { shader ->
                        shader.radius = radius
                    }
                }
            }
        }
    }
}
