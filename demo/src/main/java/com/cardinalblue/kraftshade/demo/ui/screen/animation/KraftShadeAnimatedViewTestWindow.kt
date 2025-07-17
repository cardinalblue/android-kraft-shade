package com.cardinalblue.kraftshade.demo.ui.screen.animation

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
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.pipeline.input.bounceBetween
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader

/**
 * Demo screen that demonstrates the animated shader capabilities in KraftShade.
 *
 * This screen showcases how to use [KraftShadeAnimatedView] with [KraftShadeAnimatedState]
 * to create time-based animations of shader effects, specifically a saturation effect
 * that oscillates between grayscale and full color.
 *
 * Features demonstrated:
 * - Using [KraftShadeAnimatedState] with [KraftShadeAnimatedView]
 * - Loading and displaying an image from assets
 * - Creating time-based animations with [timeInput]
 * - Using [bounceBetween] to create oscillating values
 * - Applying [SaturationKraftShader] with animated parameters
 * - Controlling animation playback with play/pause functionality
 *
 * Implementation details:
 * - Uses [setEffectAndPlay] to configure and start the animation
 * - Demonstrates creating a serial pipeline with animated steps
 * - Shows how to bind time-based inputs to shader parameters
 * - Maintains proper aspect ratio of the source image
 *
 * User interactions:
 * - Button to toggle between play and pause states for the animation
 */
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
            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.width.toFloat() / bitmap.height

            val saturationInput = timeInput
                .bounceBetween(0f, 1f)

            pipeline(windowSurface) {
                serialSteps(bitmap.asTexture(), windowSurface) {
                    step(SaturationKraftShader()) { shader ->
                        shader.saturation = saturationInput.get()
                    }
                }
            }
        }
    }
}
