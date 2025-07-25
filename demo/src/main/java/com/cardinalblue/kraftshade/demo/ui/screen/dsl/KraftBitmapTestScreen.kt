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
import com.cardinalblue.kraftshade.dsl.kraftBitmap
import com.cardinalblue.kraftshade.shader.builtin.BrightnessKraftShader
import com.cardinalblue.kraftshade.shader.builtin.ContrastKraftShader

/**
 * Demo screen that demonstrates the KraftBitmap DSL for image processing.
 *
 * This screen showcases how to use the [kraftBitmap] DSL to apply multiple shaders
 * to an image in a serial pipeline and display the result in a Compose [Image].
 *
 * Features demonstrated:
 * - Using the [kraftBitmap] DSL to process images
 * - Creating a serial pipeline of shaders
 * - Applying multiple effects (contrast and brightness) in sequence
 * - Converting the result to an [ImageBitmap] for display in Compose
 *
 * Implementation details:
 * - Loads an image from assets
 * - Uses [serialPipeline] to chain multiple shaders
 * - Applies [ContrastKraftShader] with increased contrast (4f)
 * - Applies [BrightnessKraftShader] with decreased brightness (-0.5f)
 * - Converts the processed bitmap to an ImageBitmap for display
 */
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
        bitmap = kraftBitmap(context, inputBitmap) { // compare to GPUImage
            // compare to GPUImageFilterGroup
            serialPipeline {
                step(ContrastKraftShader(4f))
                step(BrightnessKraftShader(-0.5f))
            }
        }.asImageBitmap()
    }
}
