@file:OptIn(DangerousKraftShadeApi::class)
package com.cardinalblue.kraftshade.widget

import android.content.Context
import android.util.AttributeSet
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.EffectExecution
import com.cardinalblue.kraftshade.pipeline.EffectExecutionProvider
import com.cardinalblue.kraftshade.shader.buffer.WindowSurfaceBuffer
import com.cardinalblue.kraftshade.util.DangerousKraftShadeApi
import com.cardinalblue.kraftshade.util.KraftLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.seconds

open class KraftEffectTextureView : KraftTextureView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val logger = KraftLogger("KraftEffectTextureView")
    private var attachScope: CoroutineScope? = null

    @DangerousKraftShadeApi
    var effectExecution: EffectExecution? = null
        private set

    /**
     * When the view size changed, rendering won't be triggered if this is set to false.
     */
    var renderOnSizeChange: Boolean = true

    protected var job: Job? = null

    private val renderFlow = MutableSharedFlow<Unit>()

    /**
     * This field is a workaround. It seems GPUImage has such workaround too.
     * Note that after the size of the surface has changed, we need to render it twice to make the
     * size correct. Not sure if there is a better way to resolve the problem of deformation if we
     * only render once.
     */
    private var renderTwiceForNextFrame = false

    private val sizeChangeListener = object : WindowSurfaceBuffer.Listener {
        override fun onWindowSurfaceBufferReady() {}

        override fun onWindowSurfaceBufferSizeChanged(size: GlSize) {
            runGlTask { _ ->
                effectExecution?.onBufferSizeChanged(size)
                renderTwiceForNextFrame = true
                if (renderOnSizeChange) {
                    requestRender()
                }
            }
        }
    }

    var ratio = 0.0f
        set(value) {
            field = value
            requestLayout()
        }

    init {
        addWindowSurfaceBufferListener(sizeChangeListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (ratio != 0.0f) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = MeasureSpec.getSize(heightMeasureSpec)

            val newHeight: Int
            val newWidth: Int
            if (width / ratio < height) {
                newWidth = width
                newHeight = Math.round(width / ratio)
            } else {
                newHeight = height
                newWidth = Math.round(height * ratio)
            }

            val newWidthSpec = MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY)
            val newHeightSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY)
            super.onMeasure(newWidthSpec, newHeightSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setEffect(
        afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = {},
        effectExecutionProvider: EffectExecutionProvider
    ) {
        runGlTask { windowSurface ->
            val effect = with(effectExecutionProvider) {
                provide(windowSurface)
            }
            this@KraftEffectTextureView.effectExecution = effect
            afterSet(this, windowSurface)
        }
    }

    fun requestRender() {
        attachScope?.launch(Dispatchers.Default) {
            renderFlow.emit(Unit)
        }
    }

    /**
     * See the kdoc of [renderTwiceForNextFrame] for why we need to render twice.
     */
    private fun asyncRender() {
        val effect = effectExecution ?: return
        job?.cancel()
        job = runGlTask { _ ->
            logger.tryAndLog {
                effect.run()
                if (renderTwiceForNextFrame) {
                    renderTwiceForNextFrame = false
                    effect.run()
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachScope = CoroutineScope(Job())
            .also { scope ->
                scope.launch {
                    renderFlow
                        .sample((1.seconds / 60))
                        .collectLatest {
                            asyncRender()
                        }
                }
            }
    }

    override fun onDetachedFromWindow() {
        attachScope?.cancel()
        attachScope = null
        super.onDetachedFromWindow()
    }
}
