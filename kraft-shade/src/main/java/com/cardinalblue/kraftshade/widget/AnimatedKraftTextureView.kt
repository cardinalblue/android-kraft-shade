@file:OptIn(DangerousKraftShadeApi::class)
package com.cardinalblue.kraftshade.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import androidx.annotation.MainThread
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.pipeline.AnimatedEffectExecutionProvider
import com.cardinalblue.kraftshade.pipeline.EffectExecution
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.pipeline.input.TimeInput
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.buffer.WindowSurfaceBuffer
import com.cardinalblue.kraftshade.util.DangerousKraftShadeApi
import com.cardinalblue.kraftshade.util.KraftLogger
import kotlinx.coroutines.Job

/**
 * A specialized [KraftTextureView] that provides animation capabilities using Android's Choreographer
 * for frame-synchronized rendering.
 *
 * This view requires an [EffectExecution] to be set using [setEffect] before any rendering can occur. The effect
 * defines how the content will be processed and rendered for each frame. The rendering pipeline is
 * triggered in the following sequence:
 *
 * 1. Set an effect using [setEffect]
 * 2. Call [play] to start the animation
 * 3. For each frame:
 *    - Choreographer triggers a new frame
 *    - The effect is applied to process the frame
 *    - The result is rendered to the view's surface
 *
 * For dynamic effects that need to change based on time, user interaction, or other states:
 * - Use the input system within your [Pipeline] construction
 * - Note that although KraftShader is also [EffectExecution], but the input system only works with [Pipeline], so if you only have a [KraftShader], but you still want to use input system, you should wrap it in any kind of Pipeline.
 * - Update the state of the inputs during animation instead of directly touching the components in the effect
 * - The changes will be automatically applied in the next frame based on how pipeline and input work
 *
 * The animation can be controlled using [play] and [stop] methods.
 */
class AnimatedKraftTextureView : KraftEffectTextureView {
    /**
     * Just a state for external to use
     */
    var playing = false
        private set

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val callback: Callback = Callback()
    private val choreographer: Choreographer = Choreographer.getInstance()
    private val logger = KraftLogger("AnimatedKraftTextureView")

    val timeInput: TimeInput = TimeInput()

    fun setEffectWithTimeInput(
        afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer, timeInput: TimeInput) -> Unit = { _, _ -> },
        effectExecutionProvider: AnimatedEffectExecutionProvider
    ) {
        setEffect(
            effectExecutionProvider = { windowSurface ->
                with(effectExecutionProvider) {
                    provide(windowSurface, timeInput)
                }
            },
            afterSet = { afterSet(this, it, timeInput) },
        )
    }

    fun setEffectAndPlay(effectExecutionProvider: AnimatedEffectExecutionProvider) {
        setEffectWithTimeInput(
            afterSet = { _, _ ->
                play()
            },
            effectExecutionProvider = effectExecutionProvider
        )
    }

    fun setEffectAndPause(effectExecutionProvider: AnimatedEffectExecutionProvider) {
        setEffectWithTimeInput(
            afterSet = { _, _ ->
                stop()
            },
            effectExecutionProvider = effectExecutionProvider
        )
    }

    /**
     * This will trigger the render using Choreographer (usually 60fps). To adjust the effects, you
     * should use the input system to make changes to the shaders or pipelines.
     */
    @MainThread
    fun play() {
        checkNotNull(effectExecution) { "effect is not set, call setEffect before calling this method" }
        // If already playing, do nothing to avoid double-posting callbacks
        if (playing) return
        logger.i("play")
        timeInput.start()
        choreographer.postFrameCallback(callback)
        playing = true
    }

    @MainThread
    fun stop() {
        if (!playing) return
        logger.d("stop")
        timeInput.pause()
        callback.job?.cancel() // Cancel any ongoing render job
        choreographer.removeFrameCallback(callback)
        playing = false
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private inner class Callback : FrameCallback {
        var job: Job? = null
            private set

        override fun doFrame(frameTimeNanos: Long) {
            if (!playing) return // Early return if we're not supposed to be playing

            // Schedule next frame first
            choreographer.postFrameCallback(this)
            logger.v("postFrameCallback - doFrame()")

            val effect = requireNotNull(effectExecution)
            val currentJob = job
            if (currentJob?.isActive == true) {
                logger.v("Previous frame still rendering, skipping this frame")
                return
            }

            job = runGlTask { _ ->
                logger.tryAndLog {
                    effect.run()
                }
            }
        }
    }
}