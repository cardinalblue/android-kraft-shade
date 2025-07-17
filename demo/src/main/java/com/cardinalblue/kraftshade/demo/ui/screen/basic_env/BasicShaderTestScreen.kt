package com.cardinalblue.kraftshade.demo.ui.screen.basic_env

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

@Composable
fun BasicShaderScreen() {
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
                backgroundColor = GlColor.Green,
            ).apply {
                debug = true
            }

            shader.drawTo(windowSurface)
        }
    }
}