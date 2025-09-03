package com.cardinalblue.kraftshade.demo.ui.screen.media3

import android.content.Context
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.ui.PlayerView
import com.cardinalblue.kraftshade.demo.ui.components.NetworkAwareScreen
import com.cardinalblue.kraftshade.media3.KraftShadePipelineEffect
import com.cardinalblue.kraftshade.shader.builtin.ContrastKraftShader
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader
import java.io.File
import kotlin.math.abs

@OptIn(UnstableApi::class)
@Composable
fun TransformerWithKraftShaderPipeline() {
    NetworkAwareScreen(
        additionalDescription = "We need network to stream the sample video for this demo"
    ) {
        // You can either use MediaMetadataRetriever to get the aspect ratio or add listener to listen
        // for the video frame sizes. However, Player.Listener won't be working when you use
        // setVideoEffects on ExoPlayer. This is an existing issue that might not be resolved soon.
        // See https://github.com/androidx/media/issues/2505
        // The black bounds are due to callback not being called.
        var videoAspectRatio by remember { mutableFloatStateOf(16f / 9) }

        val context = LocalContext.current
        var exportedUri by remember { mutableStateOf<String?>(null) }
        val exoPlayer = remember {
            ExoPlayer.Builder(context)
                .build()
                .apply { playWhenReady = true }
        }
        LaunchedEffect(exportedUri) {
            if (exportedUri == null) return@LaunchedEffect
            exoPlayer.setMediaItems(
                listOf(
                    MediaItem.Builder()
                        .setUri(exportedUri!!)
                        .build()
                )
            )
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(16.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(videoAspectRatio),
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                    }
                }
            )

            DisposableEffect(Unit) {
                onDispose {
                    exoPlayer.release()
                }
            }

            var from by remember { mutableIntStateOf(0) }
            var to by remember { mutableIntStateOf(20) }
            TextField(
                value = from.toString(),
                onValueChange = { from = it.toIntOrNull() ?: 0 },
                label = { Text("From (s)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            TextField(
                value = to.toString(),
                onValueChange = { to = it.toIntOrNull() ?: 20 },
                label = { Text("To (s)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )


            var isExporting by remember { mutableStateOf(false) }
            Button(
                enabled = !isExporting,
                onClick = {
                    exoPlayer.pause()
                    isExporting = true

                    val output = File(context.cacheDir, "exported_${System.currentTimeMillis()}.mp4")
                    export(
                        context, output, from, to,
                        object : Transformer.Listener {
                            override fun onCompleted(
                                composition: Composition,
                                exportResult: ExportResult
                            ) {
                                super.onCompleted(composition, exportResult)
                                exportedUri = "file://${output.absolutePath}"
                                isExporting = false
                                Toast.makeText(context, "Exported!", Toast.LENGTH_SHORT).show()
                            }

                            override fun onError(
                                composition: Composition,
                                exportResult: ExportResult,
                                exportException: ExportException
                            ) {
                                super.onError(composition, exportResult, exportException)
                                isExporting = false
                            }
                        })
                }) {
                Text("Export")
            }
        }
    }
}

@OptIn(UnstableApi::class)
private fun export(context: Context, output: File, from: Int, to: Int, listener: Transformer.Listener) {
    val transformer = Transformer.Builder(context)
        .setVideoMimeType(MimeTypes.VIDEO_H264)
        .setAudioMimeType(MimeTypes.AUDIO_AAC)
        .build()
    val exportItem = MediaItem.Builder()
        .setUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4".toUri())
        .setClippingConfiguration(
            MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(from * 1000L)
                .setEndPositionMs(to * 1000L)
                .build()
        )
        .build()
    val editedMediaItem = EditedMediaItem.Builder(exportItem)
        .setEffects(
            Effects(
                emptyList(),
                listOf(
                    KraftShadePipelineEffect(context) { buffer, time, videoTexture ->
                        pipeline(buffer) {
                            serialSteps(videoTexture, buffer) {
                                step(SaturationKraftShader()) { shader ->
                                    shader.saturation = 2f
                                }
                                step(ContrastKraftShader()) { shader ->
                                    val cycle = time.get() % 4f - 2f
                                    val contrast = abs(cycle) / 2f * 3f
                                    shader.contrast = contrast
                                }
                            }
                        }
                    }
                )
            )
        )
        .build()
    transformer.addListener(listener)
    transformer.start(editedMediaItem, output.absolutePath)
}