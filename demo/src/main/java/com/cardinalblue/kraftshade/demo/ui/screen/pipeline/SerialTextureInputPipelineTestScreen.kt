package com.cardinalblue.kraftshade.demo.ui.screen.pipeline

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cardinalblue.kraftshade.dsl.CommonInputs
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.pipeline.SerialTextureInputPipeline
import com.cardinalblue.kraftshade.pipeline.input.bounceBetween
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
    var env: GlEnv? by remember { mutableStateOf(null) }
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
                env = this.glEnv
                runGlTask { windowSurface ->
                    previousBuffer = buffer
                    buffer = windowSurface
                    val input = context.assets.open("sample/cat.jpg").use {
                        BitmapFactory.decodeStream(it)
                    }.let(::LoadedTexture)

                    aspectRatio = input.size.aspectRatio

                    pipeline = serialTextureInputPipeline {
                        setInputTexture(input)
                        setTargetBuffer(windowSurface)

                        +SaturationKraftShader()
                            .withInput(
                                CommonInputs
                                    .time()
                                    .bounceBetween(0f, 1f)
                            ) { saturationInput, shader ->
                                shader.saturation = saturationInput.get()
                            }
                    }
                }
            }
        }
    ) {
        it.runGlTask { windowSurface ->
            pipeline?.setTargetBuffer(windowSurface)
        }
    }

    DisposableEffect(key1 = env) {
        onDispose {
            runBlocking {
                env?.use {
                    pipeline?.destroy()
                    terminateEnv()
                }
            }
        }
    }

    DisposableEffect(key1 = buffer) {
        onDispose {
            if (previousBuffer == null) return@onDispose
            runBlocking {
                env?.use {
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
