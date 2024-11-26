package com.cardinalblue.kraftshade.demo.ui.screen.basic_env

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.cardinalblue.kraftshade.env.GlEnv

@Composable
fun BasicGlEnvScreen(
    modifier: Modifier = Modifier,
    imageGeneration: suspend GlEnv.() -> Bitmap,
) {
    var image by remember { mutableStateOf<ImageBitmap?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        image?.let { image ->
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )
        }
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        val glEnv = GlEnv(context)
        val bitmap = glEnv.execute {
            imageGeneration
                .invoke(env)
                .also { env.terminate() }
        }
        image = bitmap.asImageBitmap()
    }
}
