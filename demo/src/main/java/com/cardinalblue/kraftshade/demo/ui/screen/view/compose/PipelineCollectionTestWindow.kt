package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.builtin.KuwaharaKraftShader

@Composable
fun PipelineCollectionTestWindow() {
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }

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
                modifier = Modifier.aspectRatio(aspectRatio),
                state = state
            )
        }

        Button(onClick = {
            state.setEffect { windowSurface ->
                val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
                aspectRatio = bitmap.width.toFloat() / bitmap.height

                pipeline(windowSurface) {
                    val texture = bitmap.asTexture()
                    serialSteps(
                        inputTexture = texture,
                        targetBuffer = windowSurface,
                    ) {
                        step(KuwaharaKraftShader()) { shader ->
                            shader.radius = 10
                        }
                    }
                }
            }
        }) {
            Text("Set Effect")
        }
    }
}
