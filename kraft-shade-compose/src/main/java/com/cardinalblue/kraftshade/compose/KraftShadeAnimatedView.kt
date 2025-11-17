package com.cardinalblue.kraftshade.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cardinalblue.kraftshade.pipeline.AnimatedEffectExecutionProvider
import com.cardinalblue.kraftshade.pipeline.input.TimeInput
import com.cardinalblue.kraftshade.widget.AnimatedKraftTextureView
import kotlinx.coroutines.CoroutineScope

@Composable
fun KraftShadeAnimatedView(
    modifier: Modifier = Modifier,
    state: KraftShadeAnimatedState = rememberKraftShadeAnimatedState(),
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AnimatedKraftTextureView(context).also {
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

open class KraftShadeAnimatedState(scope: CoroutineScope) : KraftShadeBaseState<AnimatedKraftTextureView>(scope) {
    val playing: Boolean get() {
        return view?.playing ?: false
    }

    val isEffectExecutionReady: Boolean get() {
        return view?.isEffectExecutionReady ?: false
    }

    suspend fun getTimeInput(): TimeInput {
        return withLock { it.timeInput }
    }

    fun setEffectAndPause(effectExecutionProvider: AnimatedEffectExecutionProvider) {
        launchWithLock { view ->
            view.setEffectAndPause(effectExecutionProvider)
        }
    }

    fun setEffectAndPlay(effectExecutionProvider: AnimatedEffectExecutionProvider) {
        launchWithLock { view ->
            view.setEffectAndPlay(effectExecutionProvider)
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
