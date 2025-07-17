package com.cardinalblue.kraftshade.demo.ui.screen.color

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.demo.util.aspectRatio
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.EmbossKraftShader
import com.cardinalblue.kraftshade.shader.builtin.LookUpTableKraftShader

/**
 * Demo screen that demonstrates the Look-Up Table (LUT) shader in KraftShade.
 *
 * This screen showcases how to use [KraftShadeEffectView] with [LookUpTableKraftShader]
 * to apply color grading to an image using a lookup table image.
 *
 * Features demonstrated:
 * - Using [KraftShadeEffectState] with [KraftShadeEffectView]
 * - Loading and displaying an image from assets
 * - Loading a LUT image from assets
 * - Applying [LookUpTableKraftShader] for color grading
 * - Working with multiple input textures in a shader
 *
 * Implementation details:
 * - Uses [runGlTask] to execute OpenGL operations
 * - Demonstrates setting multiple input textures on a shader
 * - Shows how to apply a black and white LUT effect
 * - Maintains proper aspect ratio of the source image
 *
 * Technical background:
 * - A Look-Up Table (LUT) is a predefined transformation of colors used for color grading
 * - The LUT image contains a mapping of original colors to their transformed versions
 * - This technique is commonly used in photo and video editing for consistent color treatments
 */
@Composable
fun LookUpTableShaderTestScreen() {
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    val state = rememberKraftShadeEffectState()
    KraftShadeEffectView(
        modifier = Modifier.aspectRatio(aspectRatio),
        state = state,
    )

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        state.runGlTask { windowSurface ->
            val shader = LookUpTableKraftShader()

            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.aspectRatio
            val lutBitmap = context.loadBitmapFromAsset("sample/lookup_bw1.jpg")
            shader.setInputTexture(bitmap.asTexture())
            shader.setSecondInputTexture(lutBitmap.asTexture())
            shader.drawTo(windowSurface)
        }
    }
}
