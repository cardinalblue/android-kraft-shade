package com.cardinalblue.kraftshade.demo.ui.screen.shaders

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.*
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.FalseColorKraftShader
import kotlin.random.Random

/**
 * Demo screen that demonstrates the False Color shader effect in KraftShade.
 *
 * This screen showcases how to use [KraftShadeEffectView] with [FalseColorKraftShader]
 * to apply a false color effect to an image using two customizable colors.
 *
 * Features demonstrated:
 * - Using [KraftShadeEffectState] with [KraftShadeEffectView]
 * - Loading and displaying an image from assets
 * - Applying [FalseColorKraftShader] to create a false color effect
 * - Interactive color selection through clickable color swatches
 * - Converting Compose [Color] to KraftShade color formats
 *
 * Implementation details:
 * - Uses [setEffect] to configure the rendering pipeline
 * - Demonstrates using [stepWithInputTexture] for shader configuration
 * - Shows how to update shader parameters in real-time
 * - Uses [LaunchedEffect] to trigger re-rendering when colors change
 * - Maintains proper aspect ratio of the source image
 *
 * User interactions:
 * - Click on color swatches to randomly change the colors used in the effect
 */
@Composable
fun FalseColorShaderScreen() {

    var aspectRatio by remember { mutableFloatStateOf(1f) }

    var firstColor by remember { mutableStateOf(Color(Random.nextInt())) }
    var secondColor by remember { mutableStateOf(Color(Random.nextInt())) }

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First color
            Spacer(
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, Color.White)
                    .background(color = firstColor)
                    .clickable { firstColor = Color(Random.nextInt()) }
            )

            // Second color
            Spacer(
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, Color.White)
                    .background(color = secondColor)
                    .clickable { secondColor = Color(Random.nextInt()) }
            )
        }
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        state.setEffect { windowSurface ->
            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.width.toFloat() / bitmap.height
            pipeline(windowSurface) {
                stepWithInputTexture(
                    shader = FalseColorKraftShader(),
                    inputTexture = bitmap.asTexture(),
                    targetBuffer = windowSurface
                ) { shader ->
                    shader.firstColor = firstColor.asGlColor().vec3
                    shader.secondColor = secondColor.asGlColor().vec3
                }
            }
        }
    }

    LaunchedEffect(key1 = firstColor, secondColor) {
        state.requestRender()
    }
}