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
import com.cardinalblue.kraftshade.shader.builtin.ColorInvertKraftShader
import com.cardinalblue.kraftshade.shader.builtin.CrosshatchKraftShader
import com.cardinalblue.kraftshade.shader.builtin.MultiplyBlendKraftShader

/**
 * Demo screen that demonstrates a colorful crosshatch effect using multiple shaders in KraftShade.
 *
 * This screen showcases how to use [KraftShadeEffectView] with multiple shaders in sequence
 * to create a complex colorful crosshatch effect by combining [CrosshatchKraftShader],
 * [ColorInvertKraftShader], and [MultiplyBlendKraftShader].
 *
 * Features demonstrated:
 * - Using [KraftShadeEffectState] with [KraftShadeEffectView]
 * - Loading and displaying an image from assets
 * - Creating intermediate buffer references for multi-pass rendering
 * - Chaining multiple shader effects (crosshatch, invert, multiply blend)
 * - Interactive adjustment of shader parameters using sliders
 * - Using [LaunchedEffect] to trigger re-rendering when parameters change
 *
 * Implementation details:
 * - Uses [setEffect] to configure the rendering pipeline
 * - Demonstrates creating buffer references with [createBufferReferences]
 * - Shows how to create a multi-pass rendering pipeline
 * - Uses [stepWithInputTexture] for blending operations
 * - Maintains proper aspect ratio of the source image
 * - Uses precise parameter control with [numberOfFractionDigits]
 *
 * User interactions:
 * - Slider to adjust the crosshatch spacing from 0.01 to 0.1
 * - Slider to adjust the line width from 0.001 to 0.01
 *
 * Technical approach:
 * - First applies a crosshatch effect to the original image
 * - Then inverts the colors of the crosshatched result
 * - Finally multiplies the inverted crosshatch with the original image
 * - This creates a colorful effect where the original colors show through the crosshatching
 */
@Composable
fun ColorfulCrosshatchTestScreen() {
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
                        val (invertedCrosshatch) = createBufferReferences("inverted_crosshatch")

                        val inputTexture = bitmap.asTexture()
                        serialSteps(inputTexture, invertedCrosshatch) {
                            step(CrosshatchKraftShader()) { shader ->
                                shader.crossHatchSpacing = crossHatchSpacing
                                shader.lineWidth = lineWidth
                            }

                            step(ColorInvertKraftShader())
                        }

                        stepWithInputTexture(
                            MultiplyBlendKraftShader().apply {
                                setSecondInputTexture(inputTexture)
                            },
                            invertedCrosshatch,
                            targetBuffer = windowSurface,
                        )
                    }
                }
            }

            LaunchedEffect(crossHatchSpacing, lineWidth) {
                state.requestRender()
            }
        }
    }
}
