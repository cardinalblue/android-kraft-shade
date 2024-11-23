package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.KraftShadeAnimatedView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeAnimatedState
import com.cardinalblue.kraftshade.pipeline.input.CommonInputs
import com.cardinalblue.kraftshade.pipeline.input.bounceBetween
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun KraftShadeAnimatedViewTestWindow() {
    val state = rememberKraftShadeAnimatedState()
    var isPlaying by remember { mutableStateOf(true) }
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KraftShadeAnimatedView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .aspectRatio(aspectRatio),
            state = state
        )

        Button(
            modifier = Modifier.padding(16.dp),
            onClick = {
                if (isPlaying) {
                    state.stop()
                } else {
                    state.play()
                }
                isPlaying = !isPlaying
            }
        ) {
            Text(if (isPlaying) "Pause" else "Play")
        }
    }

    LaunchedEffect(Unit) {
        state.setEffectAndPlay { windowSurface, timeInput ->
            val bitmap = withContext(Dispatchers.IO) {
                context.assets.open("sample/cat.jpg").use {
                    BitmapFactory.decodeStream(it)
                }
            }
            aspectRatio = bitmap.width.toFloat() / bitmap.height

            val saturationInput = timeInput
                .bounceBetween(0f, 1f)

            SaturationKraftShader()
                .apply { setInputTexture(bitmap.asTexture()) }
                .asEffectExecution(
                    saturationInput,
                    targetBuffer = windowSurface
                ) { (saturationInput) ->
                    this.saturation = saturationInput.getCasted()
                }
        }
    }
}
