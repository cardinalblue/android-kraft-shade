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
import com.cardinalblue.kraftshade.pipeline.input.sampledInput
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.ToonKraftShader

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
                    step(
                        ToonKraftShader(),
                        sampledInput { threshold },
                        sampledInput { quantizationLevels },
                    ) { (thresholdInput, quantizationLevelsInput) ->
                        this.threshold = thresholdInput.cast()
                        this.quantizationLevels = quantizationLevelsInput.cast()
                    }
                }
            }
        }
    }
}
