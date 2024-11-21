package com.cardinalblue.kraftshade.widget

import android.content.Context
import android.util.AttributeSet
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.pipeline.Effect
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.buffer.WindowSurfaceBuffer
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

    protected var effect: Effect? = null
    protected var job: Job? = null

    private val renderFlow = MutableSharedFlow<Unit>()

    fun setEffect(
        afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = {},
        effectProvider: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Effect
    ) {
        runGlTask { windowSurface ->
            val effect = effectProvider.invoke(this, windowSurface)
            this@KraftEffectTextureView.effect = effect
            afterSet(this, windowSurface)
        }
    }

    fun requestRender() {
        attachScope?.launch {
            renderFlow.emit(Unit)
        }
    }

    private fun asyncRender() {
        val effect = effect ?: return
        job?.cancel()
        job = runGlTask { windowSurface ->
            logger.tryAndLog {
                render(effect, windowSurface)
            }
        }
    }

    protected suspend fun render(
        effect: Effect,
        windowSurface: WindowSurfaceBuffer
    ) {
        if (effect is Pipeline) {
            effect.run()
        } else if (effect is KraftShader) {
            effect.drawTo(windowSurface)
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
