package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.pipeline.input.sampledInput
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.builtin.BrightnessKraftShader
import com.cardinalblue.kraftshade.shader.builtin.ContrastKraftShader
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.components.ParameterSlider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun KraftShadeEffectViewTestWindow() {
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1.2f) }
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
                label = "Saturation",
                value = saturation,
                onValueChange = { 
                    saturation = it
                    state.requestRender()
                },
                valueRange = 0f..2f
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSlider(
                label = "Brightness",
                value = brightness,
                onValueChange = { 
                    brightness = it
                    state.requestRender()
                },
                valueRange = -1f..1f
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSlider(
                label = "Contrast",
                value = contrast,
                onValueChange = { 
                    contrast = it
                    state.requestRender()
                },
                valueRange = 0f..4f
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

                // First apply saturation
                +SaturationKraftShader()
                    .withInput(sampledInput { saturation }) { saturationInput, shader ->
                        shader.saturation = saturationInput.get()
                    }

                // Then apply brightness
                +BrightnessKraftShader()
                    .withInput(sampledInput { brightness }) { brightnessInput, shader ->
                        shader.brightness = brightnessInput.get()
                    }

                // Finally apply contrast
                +ContrastKraftShader()
                    .withInput(sampledInput { contrast }) { contrastInput, shader ->
                        shader.contrast = contrastInput.get()
                    }
            }
        }
    }
}
