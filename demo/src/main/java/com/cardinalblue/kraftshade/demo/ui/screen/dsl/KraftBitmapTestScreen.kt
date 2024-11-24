package com.cardinalblue.kraftshade.demo.ui.screen.dsl

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.dsl.kraftBitmapFrom
import com.cardinalblue.kraftshade.shader.builtin.BrightnessKraftShader
import com.cardinalblue.kraftshade.shader.builtin.ContrastKraftShader

@Composable
fun KraftBitmapTestScreen() {
    var bitmap: ImageBitmap? by remember { mutableStateOf(null) }

    bitmap?.let { bitmap ->
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(bitmap.width.toFloat() / bitmap.height),
            bitmap = bitmap,
            contentDescription = null
        )
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        val inputBitmap = context.loadBitmapFromAsset("sample/cat.jpg")
        bitmap = kraftBitmapFrom(inputBitmap) { // compare to GPUImage
            // compare to GPUImageFilterGroup
            serialPipeline {
                addShader { ContrastKraftShader(4f) }
                addShader { BrightnessKraftShader(-0.5f) }
            }
        }.asImageBitmap()
    }
}
