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

    private var callback: Callback? = null
    private var effect: Effect? = null

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

        pause()
        callback = Callback().also { callback ->
            callback.scheduleNext = true
            Choreographer
                .getInstance()
                .postFrameCallback(callback)
        }
        playing = true
    }

    @MainThread
    fun pause() {
        callback?.let { callback ->
            callback.scheduleNext = false
            Choreographer
                .getInstance()
                .removeFrameCallback(callback)
        }
        callback = null
        playing = false
    }

    private inner class Callback : FrameCallback {
        var scheduleNext = false

        private var job: Job? = null
        private var dirty = false

        override fun doFrame(frameTimeNanos: Long) {
            val effect = requireNotNull(effect)
            val isRunning = job?.isActive ?: false
            if (isRunning) {
                dirty = true
                return
            }

            job = runGlTask { windowSurface ->
                dirty = false
                do {
                    render(effect, windowSurface)
                    if (dirty) {
                        Log.d("AnimatedKraftTextureView", "still dirty. this imply the effect can't be rendered in one frame.")
                    }
                } while(dirty)

                if (!scheduleNext) return@runGlTask

                withContext(Dispatchers.Main) {
                    Choreographer
                        .getInstance()
                        .postFrameCallback(this@Callback)
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