package com.cardinalblue.kraftshade.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.cardinalblue.kraftshade.widget.KraftTextureView
import com.cardinalblue.kraftshade.widget.KraftTextureViewTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class KraftShadeBaseState<V : KraftTextureView> internal constructor(
    protected val scope: CoroutineScope
) {
    protected var view: V? = null
    protected val mutex = Mutex(true)

    internal fun setView(view: V) {
        this.view = view
        mutex.unlock()
    }

    fun runGlTask(task: KraftTextureViewTask): Job {
        return launchWithLock { view ->
            view.runGlTask(task).join()
        }
    }

    fun launchWithLock(block: suspend (view: V) -> Unit): Job {
        return scope.launch {
            mutex.withLock {
                block(view!!)
            }
        }
    }

    fun terminate() {
        val view = view ?: return
        view.terminate()
    }
}

@Composable
fun <V : KraftTextureView> rememberKraftShadeBaseState(): KraftShadeBaseState<V> {
    val scope = rememberCoroutineScope()
    return remember { KraftShadeBaseState(scope = scope) }
}
