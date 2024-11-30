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
import com.cardinalblue.kraftshade.pipeline.input.sampledInput
import com.cardinalblue.kraftshade.shader.builtin.DilationKraftShader
import com.cardinalblue.kraftshade.shader.builtin.ErosionKraftShader
import com.cardinalblue.kraftshade.shader.stepWithTwoPassSamplingFilter

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
                            sampledInput { erosionSampleRatio },
                        ) { (sampleRatio) ->
                            texelSizeRatio = sampleRatio.cast<Float>().let {
                                GlSizeF(it, it)
                            }
                        }

                        stepWithTwoPassSamplingFilter(
                            DilationKraftShader(4),
                            sampledInput { dilationSampleRatio },
                        ) { (sampleRatio) ->
                            texelSizeRatio = sampleRatio.cast<Float>().let {
                                GlSizeF(it, it)
                            }
                        }
                    }
                }
            }
        }
    }
}
