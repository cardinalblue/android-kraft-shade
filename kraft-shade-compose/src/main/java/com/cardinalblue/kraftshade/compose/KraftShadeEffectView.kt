package com.cardinalblue.kraftshade.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.pipeline.EffectExecution
import com.cardinalblue.kraftshade.shader.buffer.WindowSurfaceBuffer
import com.cardinalblue.kraftshade.widget.KraftEffectTextureView
import kotlinx.coroutines.CoroutineScope

@Composable
fun KraftShadeEffectView(
    modifier: Modifier = Modifier,
    state: KraftShadeEffectState = rememberKraftShadeEffectState(),
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            KraftEffectTextureView(context).also {
                state.setView(it)
            }
        }
    )

    DisposableEffect(key1 = Unit) {
        onDispose {
            state.terminate()
        }
    }
}

open class KraftShadeEffectState(scope: CoroutineScope) : KraftShadeBaseState<KraftEffectTextureView>(scope) {
    fun setEffect(
        afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = { requestRender() },
        effectExecutionProvider: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> EffectExecution
    ) {
        launchWithLock { view ->
            view.setEffect(afterSet, effectExecutionProvider)
        }
    }

    fun requestRender() {
        launchWithLock { view ->
            view.requestRender()
        }
    }
}

@Composable
fun rememberKraftShadeEffectState(): KraftShadeEffectState {
    val scope = rememberCoroutineScope()
    return remember { KraftShadeEffectState(scope = scope) }
}
