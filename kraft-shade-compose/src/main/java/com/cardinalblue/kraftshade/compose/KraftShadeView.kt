package com.cardinalblue.kraftshade.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cardinalblue.kraftshade.widget.KraftTextureView
import com.cardinalblue.kraftshade.widget.KraftTextureViewTask
import kotlinx.coroutines.Job

@Composable
fun KraftShadeView(
    modifier: Modifier = Modifier,
    onStateCreated: (KraftShadeState) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            KraftTextureView(context).also {
                onStateCreated(KraftShadeState(it))
            }
        }
    )
}

class KraftShadeState(
    private val view: KraftTextureView
) {
    fun runGlTask(task: KraftTextureViewTask): Job {
        return view.runGlTask(task)
    }
}
