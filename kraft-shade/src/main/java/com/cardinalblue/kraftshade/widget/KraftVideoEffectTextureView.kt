package com.cardinalblue.kraftshade.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.view.Choreographer
import android.view.Surface
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
import com.cardinalblue.kraftshade.dsl.SerialTextureInputPipelineScope
import com.cardinalblue.kraftshade.shader.buffer.ExternalOESTexture
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider
import com.cardinalblue.kraftshade.shader.buffer.WindowSurfaceBuffer
import com.cardinalblue.kraftshade.shader.builtin.DoNothingKraftShader
import com.cardinalblue.kraftshade.util.KraftLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class KraftVideoEffectTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : KraftEffectTextureView(context, attrs, defStyleAttr) {

    private val logger = KraftLogger("KraftVideoEffectTextureView")

    private var mediaPlayer: MediaPlayer? = null
    private var isPrepareCalled = false
    private var currentVideoPosition: Int = 0
    private var wasPlayingWhenPaused: Boolean = false
    private var videoUri: Uri? = null
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0

    private var videoTexture: ExternalOESTexture? = null
    private var videoSurfaceTexture: SurfaceTexture? = null

    private val choreographer: Choreographer = Choreographer.getInstance()
    private val callback: Callback = Callback()

    private var isTextureReady = false

    fun setEffectWithPipeline(
        afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = { requestRender() },
        videoRotation: () -> Float = { 0f },
        effectExecution: suspend GraphPipelineSetupScope.(inputTexture: TextureProvider, targetBuffer: GlBufferProvider,) -> Unit
    ) {
        val videoTexture = videoTexture ?: return
        super.setEffect(afterSet) { targetBuffer ->
            pipeline(targetBuffer) {
                serialSteps(videoTexture, targetBuffer) {
                    setupVideoTextureUpdate(videoRotation())
                    graphStep { inputTexture -> effectExecution(inputTexture, targetBuffer) }
                }
            }
        }
    }

    suspend fun createVideoTexture() {
        val job = runGlTask {
            val texture = ExternalOESTexture()
            this@KraftVideoEffectTextureView.videoTexture = texture
            this@KraftVideoEffectTextureView.isTextureReady = true
            logger.i("videoTexture created with ID: ${texture.textureId}")
        }
        job.join()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachScope?.launch {
            createVideoTexture()
        }
        choreographer.postFrameCallback(callback)
    }

    fun startPlayback(uri: Uri) {
        if (isPrepareCalled) {
            stopAndRelease()
        }

        videoUri = uri

        // Wait for texture to be ready before setting up MediaPlayer
        if (!isTextureReady) {
            attachScope?.launch {
                // Wait for texture creation to complete
                while (!isTextureReady) {
                    delay(10)
                }
                setupMediaPlayer(uri)
            }
        } else {
            attachScope?.launch {
                setupMediaPlayer(uri)
            }
        }
    }

    private fun setupMediaPlayer(uri: Uri) {
        mediaPlayer = MediaPlayer().apply {
            try {
                isPrepareCalled = true
                setDataSource(context, uri)

                val surfaceTexture = SurfaceTexture(videoTexture!!.textureId)
                this@KraftVideoEffectTextureView.videoSurfaceTexture = surfaceTexture
                val surface = Surface(surfaceTexture)
                setSurface(surface)
                logger.i("media set surface with texture ID: ${videoTexture?.textureId}")

                setOnPreparedListener { mp ->
                    logger.d("Media prepared, starting playback")

                    // Get video dimensions
                    this@KraftVideoEffectTextureView.videoWidth = mp.videoWidth
                    this@KraftVideoEffectTextureView.videoHeight = mp.videoHeight

                    // Set aspect ratio for proper sizing
                    ratio = videoWidth.toFloat() / videoHeight.toFloat()
                    logger.i("media prepared, video size: $videoWidth x $videoHeight")

                    mp.start()
                    mp.isLooping = true
                }

                setOnErrorListener { _, what, extra ->
                    logger.e("MediaPlayer error - what: $what, extra: $extra")
                    false
                }

                setOnCompletionListener { mp ->
                    logger.d("Media playback completed")
                    mp.seekTo(0)
                    mp.start()
                }

                prepareAsync()

            } catch (e: Exception) {
                logger.e("Error setting up MediaPlayer", e)
                stopAndRelease()
            }
        }
    }

    fun pausePlayback() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                wasPlayingWhenPaused = true
                currentVideoPosition = mp.currentPosition
                mp.pause()
                logger.d("Video paused at position: $currentVideoPosition")
            }
        }
    }

    fun resumePlayback() {
        mediaPlayer?.let { mp ->
            if (wasPlayingWhenPaused && currentVideoPosition > 0) {
                mp.seekTo(currentVideoPosition)
                mp.start()
                wasPlayingWhenPaused = false
                logger.d("Video resumed from position: $currentVideoPosition")
            }
        }
    }

    fun stopAndRelease() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
                logger.d("MediaPlayer released")
            } catch (e: Exception) {
                logger.e("Error releasing MediaPlayer", e)
            }
        }
        mediaPlayer = null
        isPrepareCalled = false
        currentVideoPosition = 0
        wasPlayingWhenPaused = false
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    private suspend fun SerialTextureInputPipelineScope.setupVideoTextureUpdate(videoRotation: Float) {
        step(DoNothingKraftShader()) {
            videoSurfaceTexture?.updateTexImage()
            it.withTransform {
                rotate2D(videoRotation, pivotX = 0.5f, pivotY = 0.5f)
                verticalFlip()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        videoSurfaceTexture?.release()
        choreographer.removeFrameCallback(callback)
    }

    private inner class Callback : Choreographer.FrameCallback {

        override fun doFrame(frameTimeNanos: Long) {
            // Schedule next frame first
            choreographer.postFrameCallback(this)

            requestRender()
        }
    }
}