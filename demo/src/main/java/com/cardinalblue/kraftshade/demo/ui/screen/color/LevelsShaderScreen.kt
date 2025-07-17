package com.cardinalblue.kraftshade.demo.ui.screen.color

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
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
import com.cardinalblue.kraftshade.shader.builtin.LevelsKraftShader

/**
 * Demo screen that demonstrates the Levels adjustment shader in KraftShade.
 *
 * This screen showcases how to use [KraftShadeEffectView] with [LevelsKraftShader]
 * to apply comprehensive color level adjustments to an image with per-channel control.
 *
 * Features demonstrated:
 * - Using [KraftShadeEffectState] with [KraftShadeEffectView]
 * - Loading and displaying an image from assets
 * - Applying [LevelsKraftShader] for precise color level adjustments
 * - Independent control of RGB channels
 * - Adjusting input/output ranges and midpoint gamma for each channel
 *
 * Implementation details:
 * - Uses [setEffect] to configure the rendering pipeline
 * - Demonstrates creating a serial pipeline with steps
 * - Shows how to update multiple shader parameters in real-time
 * - Uses [adjustRed], [adjustGreen], and [adjustBlue] methods for channel-specific adjustments
 * - Maintains proper aspect ratio of the source image
 *
 * User interactions:
 * - Multiple sliders to adjust:
 *   - Min (black point) for each channel
 *   - Mid (gamma) for each channel
 *   - Max (white point) for each channel
 *   - Min Out (output black point) for each channel
 *   - Max Out (output white point) for each channel
 */
@Composable
fun LevelsShaderScreen() {
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }

    // Control parameters
    var redMin by remember { mutableFloatStateOf(0f) }
    var redMid by remember { mutableFloatStateOf(0.5f) }
    var redMax by remember { mutableFloatStateOf(1f) }
    var redMinOut by remember { mutableFloatStateOf(0f) }
    var redMaxOut by remember { mutableFloatStateOf(1f) }

    var greenMin by remember { mutableFloatStateOf(0f) }
    var greenMid by remember { mutableFloatStateOf(0.5f) }
    var greenMax by remember { mutableFloatStateOf(1f) }
    var greenMinOut by remember { mutableFloatStateOf(0f) }
    var greenMaxOut by remember { mutableFloatStateOf(1f) }

    var blueMin by remember { mutableFloatStateOf(0f) }
    var blueMid by remember { mutableFloatStateOf(0.5f) }
    var blueMax by remember { mutableFloatStateOf(1f) }
    var blueMinOut by remember { mutableFloatStateOf(0f) }
    var blueMaxOut by remember { mutableFloatStateOf(1f) }

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
                .fillMaxHeight()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Red Channel")
            ParameterSlider(
                value = redMin,
                onValueChange = { 
                    redMin = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Min"
            )
            ParameterSlider(
                value = redMid,
                onValueChange = { 
                    redMid = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Mid"
            )
            ParameterSlider(
                value = redMax,
                onValueChange = { 
                    redMax = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Max"
            )
            ParameterSlider(
                value = redMinOut,
                onValueChange = { 
                    redMinOut = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Min Out"
            )
            ParameterSlider(
                value = redMaxOut,
                onValueChange = { 
                    redMaxOut = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Max Out"
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Green Channel")
            ParameterSlider(
                value = greenMin,
                onValueChange = { 
                    greenMin = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Min"
            )
            ParameterSlider(
                value = greenMid,
                onValueChange = { 
                    greenMid = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Mid"
            )
            ParameterSlider(
                value = greenMax,
                onValueChange = { 
                    greenMax = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Max"
            )
            ParameterSlider(
                value = greenMinOut,
                onValueChange = { 
                    greenMinOut = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Min Out"
            )
            ParameterSlider(
                value = greenMaxOut,
                onValueChange = { 
                    greenMaxOut = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Max Out"
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Blue Channel")
            ParameterSlider(
                value = blueMin,
                onValueChange = { 
                    blueMin = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Min"
            )
            ParameterSlider(
                value = blueMid,
                onValueChange = { 
                    blueMid = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Mid"
            )
            ParameterSlider(
                value = blueMax,
                onValueChange = { 
                    blueMax = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Max"
            )
            ParameterSlider(
                value = blueMinOut,
                onValueChange = { 
                    blueMinOut = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Min Out"
            )
            ParameterSlider(
                value = blueMaxOut,
                onValueChange = { 
                    blueMaxOut = it
                    state.requestRender()
                },
                valueRange = 0f..1f,
                label = "Max Out"
            )
        }
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        state.setEffect { windowSurface ->
            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.width.toFloat() / bitmap.height

            pipeline(windowSurface) {
                serialSteps(
                    inputTexture = bitmap.asTexture(),
                    targetBuffer = windowSurface,
                ) {
                    step(LevelsKraftShader()) { shader ->
                        shader.adjustRed(
                            redMin,
                            redMid,
                            redMax,
                            redMinOut,
                            redMaxOut
                        )
                        shader.adjustGreen(
                            greenMin,
                            greenMid,
                            greenMax,
                            greenMinOut,
                            greenMaxOut
                        )
                        shader.adjustBlue(
                            blueMin,
                            blueMid,
                            blueMax,
                            blueMinOut,
                            blueMaxOut
                        )
                    }
                }
            }
        }
    }
}
