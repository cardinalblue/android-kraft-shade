package com.cardinalblue.kraftshade.demo.ui.screen.view

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cardinalblue.kraftshade.dsl.CommonInputs
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.pipeline.input.bounceBetween
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.WindowSurfaceBuffer
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader
import com.cardinalblue.kraftshade.widget.AnimatedKraftTextureView
import kotlinx.coroutines.runBlocking

@Composable
fun AnimatedKraftTextureViewTestWindow() {
    var env: GlEnv? by remember { mutableStateOf(null) }
    var buffer: WindowSurfaceBuffer? by remember { mutableStateOf(null) }
    var previousBuffer: WindowSurfaceBuffer? by remember { mutableStateOf(null) }
    var aspectRatio by remember { mutableFloatStateOf(1f) }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(aspectRatio),
        factory = { context ->
            AnimatedKraftTextureView(context).apply {
                env = glEnv
                setEffect { windowSurface ->
                    previousBuffer = buffer
                    buffer = windowSurface
                    val input = context.assets.open("sample/cat.jpg").use {
                        BitmapFactory.decodeStream(it)
                    }.let(::LoadedTexture)

                    aspectRatio = input.size.aspectRatio

                    setupUsingSerialTextureInputPipeline(input, windowSurface)
//                    setupUsingAsPipeline(input, windowSurface)
                }
            }
        }
    )

    DisposableEffect(key1 = env) {
        onDispose {
            runBlocking {
                env?.use {
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
}


private suspend fun GlEnvDslScope.setupUsingSerialTextureInputPipeline(
    input: Texture,
    windowSurface: WindowSurfaceBuffer,
): Pipeline {
    return serialTextureInputPipeline {
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

private suspend fun GlEnvDslScope.setupUsingAsPipeline(
    input: Texture,
    windowSurface: WindowSurfaceBuffer,
): Pipeline {
    return SaturationKraftShader().asPipeline { shader ->
        shader.setInputTexture(input)
        shader.withInput(
            CommonInputs
                .time()
                .bounceBetween(0f, 1f)
        ) { saturationInput, _ ->
            shader.saturation = saturationInput.get()
        }
        setTargetBuffer(windowSurface)
    }
}