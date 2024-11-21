package com.cardinalblue.kraftshade.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cardinalblue.kraftshade.widget.KraftTextureView
import kotlinx.coroutines.CoroutineScope

@Composable
fun KraftShadeView(
    modifier: Modifier = Modifier,
    state: KraftShadeState = rememberKraftShadeState(),
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

class KraftShadeState(scope: CoroutineScope) : KraftShadeBaseState<KraftTextureView>(scope)

@Composable
fun rememberKraftShadeState(): KraftShadeState {
    val scope = rememberCoroutineScope()
    return remember { KraftShadeState(scope = scope) }
}
