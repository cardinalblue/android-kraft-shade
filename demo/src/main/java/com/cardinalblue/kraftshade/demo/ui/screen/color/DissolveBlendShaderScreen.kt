package com.cardinalblue.kraftshade.demo.ui.screen.color

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.*
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.DissolveBlendKraftShader
import com.cardinalblue.kraftshade.shader.builtin.MixBlendKraftShader

@Composable
fun DissolveBlendShaderScreen() {
    MixBlendShaderScreen(
        shaderCreator = { mixturePercent -> DissolveBlendKraftShader(mixturePercent) }
    )
}

@Composable
fun MixBlendShaderScreen(
    shaderCreator: (Float) -> MixBlendKraftShader,
) {
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var mixturePercent by remember { mutableFloatStateOf(0.5f) }

    val state = rememberKraftShadeEffectState()

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
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Mixture: ${(mixturePercent * 100).toInt()}%")
            
            Slider(
                value = mixturePercent,
                onValueChange = { mixturePercent = it },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        state.setEffect { windowSurface ->
            val baseBitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = baseBitmap.width.toFloat() / baseBitmap.height
            
            val overlayBitmap = context.loadBitmapFromAsset("sample/cat2.jpg")
            
            pipeline(windowSurface) {
                graphSteps(windowSurface) {
                    val shader = shaderCreator(mixturePercent)
                    shader.setInputTexture(baseBitmap.asTexture())
                    shader.setSecondInputTexture(overlayBitmap.asTexture())
                    step(shader, targetBuffer = graphTargetBuffer) {
                        it.mixturePercent = mixturePercent
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = mixturePercent) {
        state.requestRender()
    }
}