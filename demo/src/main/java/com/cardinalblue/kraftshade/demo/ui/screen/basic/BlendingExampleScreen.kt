package com.cardinalblue.kraftshade.demo.ui.screen.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.demo.shader.DrawCircleKraftShader
import com.cardinalblue.kraftshade.model.GlColor

/**
 * Demo screen that demonstrates alpha blending in KraftShade.
 *
 * This screen showcases how to use [KraftShadeEffectView] with a custom shader
 * that demonstrates alpha blending by drawing a red circle on a semi-transparent
 * white background.
 *
 * Features demonstrated:
 * - Basic KraftShade setup with [KraftShadeEffectState]
 * - Using a custom shader ([DrawCircleKraftShader])
 * - Alpha blending with semi-transparent colors
 * - Color manipulation using [GlColor.copyColor]
 *
 * Implementation details:
 * - Uses [runGlTask] to execute OpenGL operations
 * - Demonstrates setting alpha values for background colors
 * - Shows how transparency affects rendering in OpenGL
 */
@Composable
fun BlendingExampleScreen() {
    val state = rememberKraftShadeEffectState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KraftShadeEffectView(
            modifier = Modifier.aspectRatio(1f),
            state = state
        )
    }

    LaunchedEffect(Unit) {
        state.runGlTask { windowSurface ->
            val shader = DrawCircleKraftShader(
                color = GlColor.Red,
                backgroundColor = GlColor.White.copyColor(a = 0.5f),
            )
            shader.drawTo(windowSurface)
        }
    }
}