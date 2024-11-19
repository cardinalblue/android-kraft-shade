package com.cardinalblue.kraftshade.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cardinalblue.kraftshade.widget.KraftTextureView
import com.cardinalblue.kraftshade.widget.KraftTextureViewTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Composable
fun KraftShadeView(
    modifier: Modifier = Modifier,
    state: KraftShadeState,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            KraftTextureView(context).also {
                state.setView(it)
            }
        }
    )
}

class KraftShadeState internal constructor(
    private val scope: CoroutineScope
) {
    private var view: KraftTextureView? = null
    private val mutex = Mutex(true)

    internal fun setView(view: KraftTextureView) {
        this.view = view
        mutex.unlock()
    }

    fun runGlTask(task: KraftTextureViewTask): Job {
        return scope.launch {
            mutex.withLock {
                view!!
                    .runGlTask(task)
                    .join()
            }
        }
    }
}

@Composable
fun rememberKraftShadeState(): KraftShadeState {
    val scope = rememberCoroutineScope()
    return remember { KraftShadeState(scope = scope) }
}