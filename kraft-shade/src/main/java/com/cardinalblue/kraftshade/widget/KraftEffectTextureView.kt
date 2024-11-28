@file:OptIn(DangerousKraftShadeApi::class)
package com.cardinalblue.kraftshade.widget

import android.content.Context
import android.opengl.GLES20
import android.util.AttributeSet
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.EffectExecution
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

    private val sizeChangeListener = object : WindowSurfaceBuffer.Listener {
        override fun onWindowSurfaceBufferReady() {}

        override fun onWindowSurfaceBufferSizeChanged(size: GlSize) {
            runGlTask { windowSurface ->
                windowSurface.beforeDraw()
                GLES20.glClearColor(0f, 0f, 0f, 1f)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                windowSurface.afterDraw()
                effectExecution?.onBufferSizeChanged(size)
                if (renderOnSizeChange) {
                    requestRender()
                }
            }
        }
    }

    init {
        addWindowSurfaceBufferListener(sizeChangeListener)
    }

    fun setEffect(
        afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = {},
        effectExecutionProvider: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> EffectExecution
    ) {
        runGlTask { windowSurface ->
            val effect = effectExecutionProvider.invoke(this, windowSurface)
            this@KraftEffectTextureView.effectExecution = effect
            afterSet(this, windowSurface)
        }
    }

    fun requestRender() {
        attachScope?.launch(Dispatchers.Default) {
            renderFlow.emit(Unit)
        }
    }

    private fun asyncRender() {
        val effect = effectExecution ?: return
        job?.cancel()
        job = runGlTask { windowSurface ->
            logger.tryAndLog {
                effect.run()
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
