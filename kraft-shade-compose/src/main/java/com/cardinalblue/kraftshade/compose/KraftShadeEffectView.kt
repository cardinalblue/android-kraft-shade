@file:OptIn(DangerousKraftShadeApi::class)
package com.cardinalblue.kraftshade.compose

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.pipeline.EffectExecutionProvider
import com.cardinalblue.kraftshade.shader.buffer.WindowSurfaceBuffer
import com.cardinalblue.kraftshade.util.DangerousKraftShadeApi
import com.cardinalblue.kraftshade.widget.KraftEffectTextureView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

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

/**
 * @param skipRender Disable the rendering temporarily. [requestRender] will be ignored when this is
 *  set.
 */
open class KraftShadeEffectState(
    scope: CoroutineScope,
    var skipRender: Boolean = false,
) : KraftShadeBaseState<KraftEffectTextureView>(scope) {
    var effectExecutionProvider: EffectExecutionProvider? = null

    fun setEffect(
        afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = { requestRender() },
        effectExecutionProvider: EffectExecutionProvider,
    ) {
        launchWithLock { view ->
            view.setEffect(afterSet, effectExecutionProvider)
        }
        this.effectExecutionProvider = effectExecutionProvider
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setEffectWithBeforeAfter(
        afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = { requestRender() },
        effectExecutionProvider: EffectExecutionProvider,
        doNothingEffectExecutionProvider: EffectExecutionProvider
    ) {
        launchWithLock { view ->
            view.setOnTouchListener { v, event ->
                if (event != null && event.action == MotionEvent.ACTION_DOWN) {
                    view.setEffect(afterSet, doNothingEffectExecutionProvider)
                } else if (event != null && event.action == MotionEvent.ACTION_MOVE) {
                    skipRender = true
                } else if (event != null && event.action == MotionEvent.ACTION_UP) {
                    skipRender = false
                    view.setEffect(afterSet, effectExecutionProvider)
                } else {
                    view.setEffect(afterSet, effectExecutionProvider)
                }
                true
            }
            view.setEffect(afterSet, effectExecutionProvider)
        }
    }

    fun setRenderOnSizeChange(enabled: Boolean) {
        launchWithLock { view ->
            view.renderOnSizeChange = enabled
        }
    }

    /**
     * You have to make sure the effect is already set before calling this method.
     * Usually, you would do it with the [afterSet] param in [setEffect] method.
     */
    @DangerousKraftShadeApi
    suspend fun renderBlocking(): Unit = withContext(Dispatchers.Default) {
        mutex.withLock {
            val view = requireNotNull(view)
            view.glEnv!!.execute {
                logger.tryAndLog {
                    view.effectExecution!!.run()
                }
            }
        }
    }

    fun requestRender() {
        if (skipRender) {
            logger.d("render is skipped")
            return
        }
        launchWithLock { view ->
            view.requestRender()
        }
    }
}

/**
 * @param skipRendering Disable the rendering temporarily. [requestRender] will be ignored when set.
 */
@Composable
fun rememberKraftShadeEffectState(
    skipRendering: Boolean = false,
    renderOnSizeChange: Boolean = true,
): KraftShadeEffectState {
    val scope = rememberCoroutineScope()
    return remember {
        KraftShadeEffectState(scope = scope, skipRender = skipRendering).apply {
            if (!renderOnSizeChange) {
                this.setRenderOnSizeChange(renderOnSizeChange)
            }
        }
    }
}
