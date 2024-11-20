package com.cardinalblue.kraftshade.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import androidx.annotation.MainThread
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.pipeline.Effect
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.buffer.WindowSurfaceBuffer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class AnimatedKraftTextureView : KraftTextureView {
    /**
     * Just a state for external to use
     */
    var playing = false
        private set

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val callback: Callback = Callback()
    private var effect: Effect? = null
    private val choreographer: Choreographer = Choreographer.getInstance()

    fun setEffect(
        playAfterSet: Boolean = true,
        effectProvider: suspend GlEnv.(windowSurface: WindowSurfaceBuffer) -> Effect,
    ) {
        runGlTask { windowSurface ->
            val effect = effectProvider.invoke(this, windowSurface)
            this@AnimatedKraftTextureView.effect = effect
            if (playAfterSet) {
                withContext(Dispatchers.Main) {
                    play()
                }
            }
        }
    }

    /**
     * This will trigger the render using Choreographer (usually 60fps). To adjust the effects, you
     * should use the input system to make changes to the shaders or pipelines.
     */
    @MainThread
    fun play() {
        checkNotNull(effect) { "effect is not set, call setEffect before calling this method" }
        
        // If already playing, do nothing to avoid double-posting callbacks
        if (playing) return

        pause() // Clean up any existing callback just in case
        choreographer.postFrameCallback(callback)
        playing = true
    }

    @MainThread
    fun pause() {
        choreographer.removeFrameCallback(callback)
        callback.job?.cancel() // Cancel any ongoing render job
        playing = false
    }

    private inner class Callback : FrameCallback {
        var job: Job? = null
            private set

        override fun doFrame(frameTimeNanos: Long) {
            if (!playing) return // Early return if we're not supposed to be playing

            // Schedule next frame first
            choreographer.postFrameCallback(this)

            val effect = requireNotNull(effect)
            val currentJob = job
            if (currentJob?.isActive == true) {
                Log.d("AnimatedKraftTextureView", "Previous frame still rendering, skipping this frame")
                return
            }

            job = runGlTask { windowSurface ->
                try {
                    render(effect, windowSurface)
                } catch (e: CancellationException) {
                    throw e // Let cancellation propagate
                } catch (e: Exception) {
                    Log.e("AnimatedKraftTextureView", "Error rendering frame", e)
                    // Don't stop animation on render error
                }
            }
        }

        private suspend fun render(
            effect: Effect,
            windowSurface: WindowSurfaceBuffer
        ) {
            if (effect is Pipeline) {
                effect.run()
            } else if (effect is KraftShader) {
                effect.drawTo(windowSurface)
            }
        }
    }
}