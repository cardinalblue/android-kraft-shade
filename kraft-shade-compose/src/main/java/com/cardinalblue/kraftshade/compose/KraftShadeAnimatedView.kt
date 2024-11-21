package com.cardinalblue.kraftshade.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.pipeline.Effect
import com.cardinalblue.kraftshade.shader.buffer.WindowSurfaceBuffer
import com.cardinalblue.kraftshade.widget.AnimatedKraftTextureView
import kotlinx.coroutines.CoroutineScope

@Composable
fun KraftShadeAnimatedView(
    modifier: Modifier = Modifier,
    state: KraftShadeState = rememberKraftShadeState(),
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AnimatedKraftTextureView(context).also {
                state.setView(it)
            }
        }
    )
}

open class KraftShadeAnimatedState(scope: CoroutineScope) : KraftShadeBaseState<AnimatedKraftTextureView>(scope) {
    fun setEffect(
        afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = {},
        effectProvider: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Effect
    ) {
        launchWithLock { view ->
            view.setEffect(afterSet, effectProvider)
        }
    }

    fun requestRender() {
        launchWithLock { view ->
            view.requestRender()
        }
    }

    fun play() {
        launchWithLock { view ->
            view.play()
        }
    }

    fun stop() {
        launchWithLock { view ->
            view.stop()
        }
    }
}

@Composable
fun rememberKraftShadeAnimatedState(): KraftShadeAnimatedState {
    val scope = rememberCoroutineScope()
    return remember { KraftShadeAnimatedState(scope = scope) }
}
