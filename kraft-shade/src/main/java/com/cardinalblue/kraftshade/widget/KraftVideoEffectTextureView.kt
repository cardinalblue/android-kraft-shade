package com.cardinalblue.kraftshade.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
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
import kotlinx.coroutines.runBlocking

class KraftVideoEffectTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : KraftEffectTextureView(context, attrs, defStyleAttr) {

    private val logger = KraftLogger("KraftVideoEffectTextureView")

    private var mediaPlayer: MediaPlayer? = null
    private var isPrepareCalled = false
    private var wasPlayingWhenPaused: Boolean = false
    private var videoUri: Uri? = null
    private var videoRotation: Float = 0f
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0

    private var videoTexture: ExternalOESTexture? = null
    private var videoSurfaceTexture: SurfaceTexture? = null
    private var isWaitingForFirstFrame = false

    private val choreographer: Choreographer = Choreographer.getInstance()
    private val callback: Callback = Callback()

    private var isTextureReady = false

    fun setVideoUri(uri: Uri, autoPlay: Boolean) {
        // Stop current playback but don't release MediaPlayer for reuse
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.stop()
            }
        }
        
        videoRotation = getVideoRotation(uri)
        videoUri = uri

        // Wait for texture to be ready before setting up MediaPlayer
        if (!isTextureReady) {
            attachScope?.launch {
                // Wait for texture creation to complete
                while (!isTextureReady) {
                    delay(10)
                }
                setupMediaPlayer(uri, autoPlay)
            }
        } else {
            attachScope?.launch {
                setupMediaPlayer(uri, autoPlay)
            }
        }
    }

    fun setEffectWithPipeline(
        afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = { requestRender() },
        effectExecution: suspend GraphPipelineSetupScope.(inputTexture: TextureProvider, targetBuffer: GlBufferProvider,) -> Unit
    ) {
        val videoTexture = videoTexture ?: return
        super.setEffect(afterSet) { targetBuffer ->
            // Do not recycle texture automatically, it might be reused across different videos
            pipeline(targetBuffer, automaticTextureRecycle = false) {
                serialSteps(videoTexture, targetBuffer) {
                    setupVideoTextureUpdate(videoRotation)
                    graphStep { inputTexture -> effectExecution(inputTexture, targetBuffer) }
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachScope?.launch {
            createVideoTexture()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        videoSurfaceTexture?.release()
        runBlocking { videoTexture?.delete() }
        choreographer.removeFrameCallback(callback)
    }

    /**
     *  lifecycle method to pause the video playback,
     *  might automatically play the video when onResume() is called if it was playing before.
     */
    fun onPause() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                wasPlayingWhenPaused = true
                mp.pause()
                choreographer.removeFrameCallback(callback)
            }
        }
    }

    /**
     *  lifecycle method to resume the video playback,
     */
    fun onResume() {
        mediaPlayer?.let { mp ->
            if (wasPlayingWhenPaused && !mp.isPlaying) {
                mp.start()
                choreographer.postFrameCallback(callback)
                wasPlayingWhenPaused = false
            }
        }
    }

    /**
     *  pause event to pause the video playback.
     */
    fun pausePlayback() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                choreographer.removeFrameCallback(callback)
            }
        }
    }

    /**
     *  resume event to resume the video playback.
     */
    fun resumePlayback() {
        mediaPlayer?.let { mp ->
            if (!mp.isPlaying) {
                mp.start()
                choreographer.postFrameCallback(callback)
            }
        }
    }

    fun stopAndRelease() {
        releaseMediaPlayer()
        isPrepareCalled = false
        wasPlayingWhenPaused = false
    }

    private fun releaseMediaPlayer() {
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
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    private fun setupMediaPlayer(uri: Uri, autoPlay: Boolean) {
        // Reuse existing MediaPlayer if possible, or create a new one
        val mp = mediaPlayer ?: MediaPlayer().also { mediaPlayer = it }
        
        try {
            mp.reset()
            
            isPrepareCalled = true
            mp.setDataSource(context, uri)

            // Only create new SurfaceTexture if we don't have one or if texture changed
            if (videoSurfaceTexture == null) {
                val surfaceTexture = SurfaceTexture(videoTexture!!.textureId)
                this.videoSurfaceTexture = surfaceTexture
                val surface = Surface(surfaceTexture)
                mp.setSurface(surface)
                logger.i("media set surface with texture ID: ${videoTexture?.textureId}")
            } else {
                // Reuse existing surface
                val surface = Surface(videoSurfaceTexture)
                mp.setSurface(surface)
                logger.i("media reusing existing surface")
            }

            // Set up frame available listener for first frame detection
            videoSurfaceTexture?.setOnFrameAvailableListener { surfaceTexture ->
                if (isWaitingForFirstFrame) {
                    logger.i("First frame available, requesting render")
                    isWaitingForFirstFrame = false
                    // Remove the listener to avoid unnecessary calls
                    surfaceTexture.setOnFrameAvailableListener(null)
                    requestRender()
                }
            }

            mp.setOnPreparedListener { player ->
                // Get video dimensions
                this.videoWidth = player.videoWidth
                this.videoHeight = player.videoHeight

                // Set aspect ratio for proper sizing
                ratio = videoWidth.toFloat() / videoHeight.toFloat()
                logger.i("media prepared, video size: $videoWidth x $videoHeight")

                if (autoPlay) {
                    player.start()
                    choreographer.postFrameCallback(callback)
                } else {
                    // To show the first frame, we need to start and immediately pause
                    isWaitingForFirstFrame = true
                    player.seekTo(0)
                }
                player.isLooping = true
            }

            mp.setOnErrorListener { _, what, extra ->
                logger.e("MediaPlayer error - what: $what, extra: $extra")
                false
            }

            mp.prepareAsync()

        } catch (e: Exception) {
            logger.e("Error setting up MediaPlayer", e)
            stopAndRelease()
        }
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

    private fun createVideoTexture() {
        runGlTask {
            val texture = ExternalOESTexture()
            videoTexture = texture
            isTextureReady = true
            logger.i("videoTexture created with ID: ${texture.textureId}")
        }
    }

    private fun getVideoRotation(uri: Uri): Float {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            rotation?.toFloatOrNull() ?: 0f
        } catch (e: Exception) {
            logger.e("Error retrieving video rotation", e)
            0f
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
                // Ignore release errors
            }
        }
    }

    private inner class Callback : Choreographer.FrameCallback {

        override fun doFrame(frameTimeNanos: Long) {
            // Schedule next frame first
            choreographer.postFrameCallback(this)

            requestRender()
        }
    }
}