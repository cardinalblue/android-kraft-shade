package com.cardinalblue.kraftshade.widget

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.shader.buffer.WindowSurfaceBuffer
import com.cardinalblue.kraftshade.util.KraftLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias KraftTextureViewTask = suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit

/**
 * Usually, you only need [KraftEffectTextureView] or [AnimatedKraftTextureView] instead of this
 * class. This is the base class for all View implementations in KraftShade.
 */
open class KraftTextureView : TextureView, WindowSurfaceBuffer.Listener {
    private val logger = KraftLogger("KraftTextureView")
    var glEnv: GlEnv? = null
        private set
    private var windowSurface: WindowSurfaceBuffer? = null
    private var coroutineScope = CoroutineScope(Job())

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        isOpaque = false
    }

    private val initLock = Mutex()
    private val taskAfterAttached: MutableList<KraftTextureViewTask> = mutableListOf()

    fun runGlTask(task: KraftTextureViewTask): Job {
        return coroutineScope.launch {
            initLock.withLock {
                val glEnv = glEnv
                val windowSurface = windowSurface
                if (glEnv == null || windowSurface == null || !windowSurface.isSurfaceReady) {
                    taskAfterAttached.add(task)
                } else {
                    logger.tryAndLog {
                        glEnv.use {
                            task(windowSurface)
                        }
                    }
                }
            }
        }
    }

    private suspend fun suspendInit() {
        initLock.withLock {
            glEnv = GlEnv().apply {
                use {
                    val surface = WindowSurfaceBuffer(
                        glEnv = env,
                        listener = this@KraftTextureView,
                    ).also { windowSurface ->
                        surfaceTextureListener = windowSurface.surfaceTextureListener
                    }
                    windowSurface = surface
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Job())
        coroutineScope.launch {
            logger.tryAndLog {
                suspendInit()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        terminate()
    }

    fun terminate() {
        coroutineScope.cancel()
        runBlocking {
            logger.tryAndLog {
                initLock.withLock {
                    taskAfterAttached.clear()

                    glEnv?.use {
                        windowSurface?.delete()
                        windowSurface = null
                        terminateEnv()
                    }

                    glEnv = null
                }
            }
        }
    }

    override fun onWindowSurfaceBufferReady() {
        val glEnv = glEnv ?: return
        val surface = windowSurface ?: return
        coroutineScope.launch {
            initLock.withLock {
                if (taskAfterAttached.isEmpty()) return@withLock
                logger.tryAndLog {
                    glEnv.use {
                        logger.d("executing ${taskAfterAttached.size} tasks after buffer ready")
                        taskAfterAttached.forEach { it.invoke(this@use, surface) }
                        taskAfterAttached.clear()
                    }
                }
            }
        }
    }
}
