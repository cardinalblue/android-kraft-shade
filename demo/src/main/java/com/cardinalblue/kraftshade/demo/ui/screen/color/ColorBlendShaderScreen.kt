package com.cardinalblue.kraftshade.demo.ui.screen.color

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.cardinalblue.kraftshade.compose.*
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.ColorBlendKraftShader

@Composable
fun ColorBlendShaderScreen() {
    TwoTextureInputShaderScreen(
        shaderCreator = { ColorBlendKraftShader() }
    )
}

@Composable
fun TwoTextureInputShaderScreen(
    shaderCreator: () -> TwoTextureInputKraftShader
) {
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var selectedOverlay by remember { mutableIntStateOf(0) }

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

    }

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        state.setEffect { windowSurface ->
            val baseBitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = baseBitmap.width.toFloat() / baseBitmap.height
            
            val overlayBitmap = context.loadBitmapFromAsset("sample/cat2.jpg")
            
            pipeline(windowSurface) {
                graphSteps(windowSurface) {
                    step(shaderCreator(), targetBuffer = graphTargetBuffer) {
                        it.setInputTexture(baseBitmap.asTexture())
                        it.setSecondInputTexture(overlayBitmap.asTexture())
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = selectedOverlay) {
        state.requestRender()
    }
}