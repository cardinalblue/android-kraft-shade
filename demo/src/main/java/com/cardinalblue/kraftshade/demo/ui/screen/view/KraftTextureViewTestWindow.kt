package com.cardinalblue.kraftshade.demo.ui.screen.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cardinalblue.kraftshade.shader.builtin.DrawCircleKraftShader
import com.cardinalblue.kraftshade.widget.KraftTextureView

@Composable
fun KraftTextureViewTestWindow() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            KraftTextureView(context).apply {
                runGlTask { env, windowSurface ->
                    val shader = DrawCircleKraftShader()
                    shader.drawTo(windowSurface)
                }
            }
        }
    ) {
        // Do nothing
    }
}
