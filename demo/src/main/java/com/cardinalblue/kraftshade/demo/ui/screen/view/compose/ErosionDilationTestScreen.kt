package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.demo.shader.DrawCircleKraftShader
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.components.ParameterSlider
import com.cardinalblue.kraftshade.model.GlColor
import com.cardinalblue.kraftshade.model.GlSizeF
import com.cardinalblue.kraftshade.shader.builtin.DilationKraftShader
import com.cardinalblue.kraftshade.shader.builtin.ErosionKraftShader
import com.cardinalblue.kraftshade.shader.stepWithTwoPassSamplingFilter

/**
 * Demo screen that demonstrates morphological operations (erosion and dilation) in KraftShade.
 *
 * This screen showcases how to use [KraftShadeEffectView] with [ErosionKraftShader] and
 * [DilationKraftShader] to apply morphological operations to a simple shape (circle).
 *
 * Features demonstrated:
 * - Using [KraftShadeEffectState] with [KraftShadeEffectView]
 * - Creating a simple shape using [DrawCircleKraftShader]
 * - Applying morphological operations with [ErosionKraftShader] and [DilationKraftShader]
 * - Using [stepWithTwoPassSamplingFilter] for efficient two-pass filtering
 * - Interactive adjustment of shader parameters using sliders
 * - Creating and using intermediate buffers with [createBufferReferences]
 *
 * Implementation details:
 * - Uses [setEffect] to configure the rendering pipeline
 * - Demonstrates creating buffer references for multi-pass rendering
 * - Shows how to chain multiple morphological operations
 * - Uses named steps with [step] for better debugging
 * - Configures texel size ratios for controlling operation intensity
 *
 * User interactions:
 * - Slider to adjust the erosion sample ratio from 1 to 100
 * - Slider to adjust the dilation sample ratio from 1 to 100
 *
 * Technical background:
 * - Erosion shrinks bright regions and enlarges dark regions
 * - Dilation enlarges bright regions and shrinks dark regions
 * - These operations are fundamental in image processing for tasks like
 *   noise removal, feature extraction, and edge detection
 * - The sample ratio controls the intensity of the morphological operation
 */
@Composable
fun ErosionDilationTestScreen() {
    val state = rememberKraftShadeEffectState()
    var dilationSampleRatio by remember { mutableFloatStateOf(1f) }
    var erosionSampleRatio by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KraftShadeEffectView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            state = state
        )

        ParameterSlider(
            modifier = Modifier.padding(16.dp),
            label = "Erosion Sample Ratio",
            value = erosionSampleRatio,
            onValueChange = {
                erosionSampleRatio = it
                state.requestRender()
            },
            valueRange = 1f..100f,
        )

        ParameterSlider(
            modifier = Modifier.padding(16.dp),
            label = "Dilation Sample Ratio",
            value = dilationSampleRatio,
            onValueChange = {
                dilationSampleRatio = it
                state.requestRender()
            },
            valueRange = 1f..100f,
        )

        LaunchedEffect(Unit) {
            state.setEffect { windowSurface ->
                pipeline(windowSurface) {
                    val (circle) = createBufferReferences("circle")
                    val shader = DrawCircleKraftShader(
                        color = GlColor.Magenta,
                        scale = 0.5f,
                    )

                    step("draw_circle") {
                        shader.drawTo(circle.provideBuffer())
                    }

                    serialSteps(circle, windowSurface) {
                        stepWithTwoPassSamplingFilter(
                            ErosionKraftShader(4),
                        ) { shader ->
                            shader.texelSizeRatio = GlSizeF(erosionSampleRatio)
                        }

                        stepWithTwoPassSamplingFilter(
                            DilationKraftShader(4),
                        ) { shader ->
                            shader.texelSizeRatio = GlSizeF(dilationSampleRatio)
                        }
                    }
                }
            }
        }
    }
}
