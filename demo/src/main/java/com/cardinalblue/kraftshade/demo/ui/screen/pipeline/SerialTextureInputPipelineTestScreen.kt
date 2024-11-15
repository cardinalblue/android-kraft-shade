package com.cardinalblue.kraftshade.demo.ui.screen.pipeline

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.pipeline.SerialTextureInputPipeline
import com.cardinalblue.kraftshade.pipeline.input.TimeInput
import com.cardinalblue.kraftshade.pipeline.input.bounceBetween
import com.cardinalblue.kraftshade.pipeline.input.map
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.buffer.WindowSurfaceBuffer
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader
import com.cardinalblue.kraftshade.widget.KraftTextureView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SerialTextureInputPipelineTestScreen() {
    val glEnv = remember { GlEnv() }
    var pipeline: SerialTextureInputPipeline? by remember { mutableStateOf(null) }
    var buffer: WindowSurfaceBuffer? by remember { mutableStateOf(null) }
    var previousBuffer: WindowSurfaceBuffer? by remember { mutableStateOf(null) }
    var aspectRatio by remember { mutableFloatStateOf(1f) }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(aspectRatio),
        factory = { context ->
            KraftTextureView(context).apply {
                runGlTask { _, windowSurface ->
                    previousBuffer = buffer
                    buffer = windowSurface
                    val input = context.assets.open("sample/cat.jpg").use {
                        BitmapFactory.decodeStream(it)
                    }.let(::LoadedTexture)

                    aspectRatio = input.size.aspectRatio

                    val time = TimeInput()
                    time.start()
                    val saturationInput = time.bounceBetween(0f, 1f)
                    val saturationKraftShader = SaturationKraftShader()
                    val shaders = mutableListOf<TextureInputKraftShader>(
                        saturationKraftShader,
                    )
                    pipeline = SerialTextureInputPipeline(this, shaders).also { pipeline ->
                        pipeline.setInputTexture(input)
                        pipeline.setTargetBuffer(windowSurface)
                        pipeline.connectInput(saturationInput, saturationKraftShader) { saturationInput, shader ->
                            shader.saturation = saturationInput.get()
                        }
                    }
                }
            }
        }
    ) {
        it.runGlTask { _, windowSurface ->
            pipeline?.setTargetBuffer(windowSurface)
        }
    }

    DisposableEffect(key1 = glEnv) {
        onDispose {
            runBlocking {
                glEnv.use {
                    pipeline?.destroy()
                    terminate()
                }
            }
        }
    }

    DisposableEffect(key1 = buffer) {
        onDispose {
            if (previousBuffer == null) return@onDispose
            runBlocking {
                glEnv.use {
                    previousBuffer?.delete()
                    previousBuffer = null
                }
            }
        }
    }

    LaunchedEffect(key1 = pipeline) {
        val pipeline = pipeline ?: return@LaunchedEffect
        withContext(Dispatchers.Default) {
            while (true) {
                try {
                    pipeline.run()
                } catch (e: Exception) {
                    break
                }
                delay(10.milliseconds)
            }
        }
    }
}
