package com.cardinalblue.kraftshade.demo.ui.screen.media3

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.cardinalblue.kraftshade.demo.ui.components.NetworkAwareScreen
import com.cardinalblue.kraftshade.demo.ui.components.ParameterSlider
import com.cardinalblue.kraftshade.media3.KraftShadePipelineEffect
import com.cardinalblue.kraftshade.media3.KraftShaderEffect
import com.cardinalblue.kraftshade.pipeline.input.sampledInput
import com.cardinalblue.kraftshade.shader.builtin.ContrastKraftShader
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerWithKraftShadePipeline() {
    NetworkAwareScreen(
        additionalDescription = "We need network to stream the sample video for this demo"
    ) {
        // You can either use MediaMetadataRetriever to get the aspect ratio or add listener to listen
        // for the video frame sizes. However, Player.Listener won't be working when you use
        // setVideoEffects on ExoPlayer. This is an existing issue that might not be resolved soon.
        // See https://github.com/androidx/media/issues/2505
        // The black bounds are due to callback not being called.
        var videoAspectRatio by remember { mutableFloatStateOf(16f / 9) }
        var saturation by remember { mutableFloatStateOf(1f) }
        var contrast by remember { mutableFloatStateOf(1f) }

        val context = LocalContext.current
        val exoPlayer by remember {
            val player = ExoPlayer.Builder(context)
                .build()
                .apply { playWhenReady = true }

            mutableStateOf(player)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(videoAspectRatio),
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                    }
                },
            )

            ParameterSlider(
                modifier = Modifier
                    .padding(16.dp),
                label = "Saturation",
                value = saturation,
                valueRange = 0f..2f,
                onValueChange = {
                    saturation = it
                }
            )

            ParameterSlider(
                modifier = Modifier
                    .padding(16.dp),
                label = "Contrast",
                value = contrast,
                valueRange = 0f..4f,
                onValueChange = {
                    contrast = it
                }
            )
        }

        DisposableEffect(Unit) {
            exoPlayer.setMediaItems(listOf(
                MediaItem.Builder()
                    .setUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4".toUri())
                    .build()
            ))

            val pipelineEffect = KraftShadePipelineEffect(context) { buffer, _, videoTexture ->
                // You can also use presentationTimeInput to animate the parameters if needed.
                val saturationInput = sampledInput { saturation }
                pipeline(buffer) {
                    serialSteps(videoTexture, buffer) {
                        step(SaturationKraftShader()) { shader ->
                            shader.saturation = saturation
                        }
                        step(ContrastKraftShader()) { shader ->
                            shader.contrast = contrast
                        }
                    }
                }

            }
            exoPlayer.setVideoEffects(listOf(pipelineEffect))
            exoPlayer.prepare()

            onDispose {
                exoPlayer.release()
            }
        }
    }
}