package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.pipeline.input.sampledInput
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun KraftShadeEffectViewTestWindow() {
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KraftShadeEffectView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .aspectRatio(aspectRatio),
            state = state
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Saturation")
            Slider(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                value = saturation,
                onValueChange = { 
                    saturation = it
                    state.requestRender()
                },
                valueRange = 0f..2f
            )
            Text("%.2f".format(saturation))
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

                +SaturationKraftShader()
                    .withInput(sampledInput { saturation }) { saturationInput, shader ->
                        shader.saturation = saturationInput.get()
                    }
            }
        }
    }
}
