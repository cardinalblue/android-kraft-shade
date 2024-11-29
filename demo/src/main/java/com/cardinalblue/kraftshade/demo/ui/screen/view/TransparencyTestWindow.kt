package com.cardinalblue.kraftshade.demo.ui.screen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cardinalblue.kraftshade.compose.KraftShadeState
import com.cardinalblue.kraftshade.compose.KraftShadeView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeState
import com.cardinalblue.kraftshade.shader.KraftShader

class TransparencyShader : KraftShader() {
    override fun loadFragmentShader(): String = """
        precision mediump float;
        varying vec2 textureCoordinate;

        void main() {
            // Create a circular mask
            vec2 center = vec2(0.5, 0.5);
            float radius = 0.4;
            float dist = distance(textureCoordinate, center);

            // Inside the circle: semi-transparent blue
            // Outside the circle: fully transparent
            if (dist < radius) {
                gl_FragColor = vec4(0.0, 0.0, 1.0, 0.5); // 50% transparent blue
            } else {
                gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0); // Fully transparent
            }
        }
    """.trimIndent()
}

@Composable
fun TransparencyTestWindow() {
    val state: KraftShadeState = rememberKraftShadeState()
    var imageAspectRatio: Float by remember { mutableFloatStateOf(1.0f) }

    // Colorful background to demonstrate transparency
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        KraftShadeView(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(imageAspectRatio),
            state = state
        )
    }

    LaunchedEffect(state) {
        state.runGlTask { windowSurface ->
            // Set a 1:1 aspect ratio for the circular shader
            imageAspectRatio = 1.0f

            TransparencyShader().apply {
                drawTo(windowSurface)
            }
        }
    }
}
