package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import android.graphics.BitmapFactory
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
import com.cardinalblue.kraftshade.pipeline.input.sampledInput
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.builtin.HazeKraftShader
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.components.ParameterSlider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HazeFilterTestWindow() {
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var distance by remember { mutableFloatStateOf(0.2f) }
    var slope by remember { mutableFloatStateOf(0.0f) }
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
                label = "Distance",
                value = distance,
                onValueChange = { 
                    distance = it
                    state.requestRender()
                },
                valueRange = -0.3f..0.3f
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSlider(
                label = "Slope",
                value = slope,
                onValueChange = { 
                    slope = it
                    state.requestRender()
                },
                valueRange = -0.3f..0.3f
            )
        }
    }

    LaunchedEffect(Unit) {
        state.setEffect { windowSurface ->
            val bitmap = withContext(Dispatchers.IO) {
                context.assets.open("sample/cat.jpg").use {
                    BitmapFactory.decodeStream(it)
                }
            }
            aspectRatio = bitmap.width.toFloat() / bitmap.height

            serialTextureInputPipeline {
                setInputTexture(LoadedTexture(bitmap))
                setTargetBuffer(windowSurface)

                // Apply haze filter
                +HazeKraftShader()
                    .withInput(sampledInput { distance }) { distanceInput, shader ->
                        shader.distance = distanceInput.get()
                    }
                    .withInput(sampledInput { slope }) { slopeInput, shader ->
                        shader.slope = slopeInput.get()
                    }
            }
        }
    }
}
