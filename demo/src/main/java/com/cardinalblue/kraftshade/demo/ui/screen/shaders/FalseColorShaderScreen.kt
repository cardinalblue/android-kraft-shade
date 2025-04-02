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
import com.cardinalblue.kraftshade.shader.builtin.FalseColorKraftShader
import kotlin.random.Random

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