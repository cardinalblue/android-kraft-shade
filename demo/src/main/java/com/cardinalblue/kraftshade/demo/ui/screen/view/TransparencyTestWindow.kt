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

/**
 * A shader that demonstrates transparency handling in OpenGL.
 *
 * This shader creates a semi-transparent circular shape in the center of the view,
 * showcasing how to work with alpha channel values in fragment shaders.
 *
 * Features demonstrated:
 * - Creating a circular mask using distance calculation
 * - Setting partial transparency (alpha channel) values
 * - Rendering fully transparent areas
 * - Demonstrating alpha blending with background content
 *
 * Implementation details:
 * - Uses distance calculation from center point to create a circular shape
 * - Sets a semi-transparent blue color (50% opacity) inside the circle
 * - Sets fully transparent color outside the circle
 * - Works with the default vertex shader from [KraftShader]
 *
 * Technical background:
 * - Alpha values in OpenGL range from 0.0 (fully transparent) to 1.0 (fully opaque)
 * - The shader demonstrates proper alpha channel handling in gl_FragColor
 * - When rendered against a colored background, the transparency effect becomes visible
 */
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

/**
 * Demo screen that demonstrates transparency handling in KraftShade.
 *
 * This screen showcases how to use [KraftShadeView] with [TransparencyShader]
 * to render semi-transparent content against a colored background.
 *
 * Features demonstrated:
 * - Using [KraftShadeState] with [KraftShadeView]
 * - Rendering transparent and semi-transparent content
 * - Visualizing alpha blending with a colored background
 * - Creating a circular shape using shader code
 *
 * Implementation details:
 * - Uses a red background to make transparency effects visible
 * - Applies [TransparencyShader] to create a semi-transparent blue circle
 * - Uses [LaunchedEffect] to set up the rendering pipeline
 * - Maintains a 1:1 aspect ratio for the circular shader
 *
 * Visual effect:
 * - A semi-transparent blue circle appears in the center of a red background
 * - The area outside the circle is fully transparent, showing the red background
 * - The area inside the circle shows a blend of blue and red due to 50% transparency
 */
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
