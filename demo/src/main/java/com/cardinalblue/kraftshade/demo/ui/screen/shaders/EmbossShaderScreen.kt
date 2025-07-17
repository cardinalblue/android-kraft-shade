package com.cardinalblue.kraftshade.demo.ui.screen.shaders

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.KraftShadeView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.compose.rememberKraftShadeState
import com.cardinalblue.kraftshade.demo.util.aspectRatio
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.EmbossKraftShader

@Composable
fun EmbossShaderScreen() {
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    val state = rememberKraftShadeEffectState()
    var intensity by remember { mutableFloatStateOf(0.5f) }

    Column(modifier = Modifier.fillMaxSize()) {
        KraftShadeEffectView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio),
            state = state,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = intensity,
            valueRange = 0f..1f,
            onValueChange = { newValue ->
                intensity = newValue
                state.requestRender()
            }
        )
    }


    val context = LocalContext.current
    LaunchedEffect(Unit) {
        state.setEffect { windowBuffer ->
            pipeline(windowBuffer) {
                val shader = EmbossKraftShader().apply {
                    this.intensity = intensity
                }
                val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
                aspectRatio = bitmap.aspectRatio
                shader.setInputTexture(bitmap.asTexture())

                serialSteps(bitmap.asTexture(), targetBuffer = windowBuffer) {
                    step(shader) { _shader ->
                        _shader.intensity = intensity
                    }
                }
            }
        }
    }
}
