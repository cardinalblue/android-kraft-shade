package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

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
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.components.ParameterSlider
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.CrosshatchKraftShader

/**
 * Demo screen that demonstrates the Crosshatch shader effect in KraftShade.
 *
 * This screen showcases how to use [KraftShadeEffectView] with [CrosshatchKraftShader]
 * to apply a crosshatch drawing effect to an image with adjustable parameters.
 *
 * Features demonstrated:
 * - Using [KraftShadeEffectState] with [KraftShadeEffectView]
 * - Loading and displaying an image from assets
 * - Applying [CrosshatchKraftShader] for artistic crosshatching effects
 * - Interactive adjustment of shader parameters using sliders
 * - Using [LaunchedEffect] to trigger re-rendering when parameters change
 *
 * Implementation details:
 * - Uses [setEffect] to configure the rendering pipeline
 * - Demonstrates creating a serial pipeline with steps
 * - Shows how to update shader parameters in real-time
 * - Maintains proper aspect ratio of the source image
 * - Uses precise parameter control with [numberOfFractionDigits]
 *
 * User interactions:
 * - Slider to adjust the crosshatch spacing from 0.01 to 0.1
 * - Slider to adjust the line width from 0.001 to 0.01
 *
 * Technical background:
 * - Crosshatching is a drawing technique that uses closely spaced parallel lines
 *   to create tone and shading in illustrations
 * - The crosshatch spacing controls the density of the hatching pattern
 * - The line width controls the thickness of individual lines in the pattern
 */
@Composable
fun CrosshatchTestScreen() {
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var crossHatchSpacing by remember { mutableFloatStateOf(0.03f) }
    var lineWidth by remember { mutableFloatStateOf(0.003f) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KraftShadeEffectView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio),
            state = state
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ParameterSlider(
                label = "Cross Hatch Spacing",
                value = crossHatchSpacing,
                onValueChange = { crossHatchSpacing = it },
                valueRange = 0.01f..0.1f
            )

            ParameterSlider(
                label = "Line Width",
                value = lineWidth,
                onValueChange = { lineWidth = it },
                valueRange = 0.001f..0.01f,
                numberOfFractionDigits = 4
            )

            LaunchedEffect(Unit) {
                state.setEffect { windowSurface ->
                    val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
                    aspectRatio = bitmap.width.toFloat() / bitmap.height

                    pipeline(windowSurface) {
                        serialSteps(bitmap.asTexture(), windowSurface) {
                            step(CrosshatchKraftShader()) { shader ->
                                shader.crossHatchSpacing = crossHatchSpacing
                                shader.lineWidth = lineWidth
                            }
                        }
                    }
                }
            }

            LaunchedEffect(crossHatchSpacing, lineWidth) {
                state.requestRender()
            }
        }
    }
}
