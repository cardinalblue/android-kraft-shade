package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLSurface
import android.view.Surface
import android.view.TextureView
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.util.KraftLogger

/**
 * A WindowSurfaceBuffer can be created from following three ways:
 * 1. From a Surface with size
 * 2. From a SurfaceTexture with size
 * 3. Create the buffer without any surface or surfaceTexture first, and when a SurfaceTexture is
 *    ready, use surfaceTextureListener to set the surfaceTexture and size from external source.
 */
class WindowSurfaceBuffer(
    private val glEnv: GlEnv,
    surface: SurfaceWithSize? = null,
    surfaceTexture: SurfaceTextureWithSize? = null,
    private var listener: Listener? = null,
) : GlBuffer {
    private val logger = KraftLogger("WindowSurfaceBuffer")

    // from listener
    private var _size: GlSize? = surface?.size ?: surfaceTexture?.size
    private var surfaceTexture: SurfaceTexture? = surfaceTexture?.surfaceTexture
    private var surface: Surface? = surface?.surface
    val isSurfaceReady: Boolean get() = surfaceTexture != null || surface != null

    val surfaceTextureListener by lazy {
        object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                logger.i("Surface texture available: ${width}x${height}")
                this@WindowSurfaceBuffer.surfaceTexture = surface
                this@WindowSurfaceBuffer._size = GlSize(width, height)
                listener?.onWindowSurfaceBufferReady()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                logger.i("Surface texture size changed: ${width}x${height}")
                this@WindowSurfaceBuffer.surfaceTexture = surface
                this@WindowSurfaceBuffer._size = GlSize(width, height)
                listener?.onWindowSurfaceBufferSizeChanged(GlSize(width, height))
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                logger.i("Surface texture destroyed")
                this@WindowSurfaceBuffer.surfaceTexture = null
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // Do nothing
            }

        }
    }

    override val isScreenCoordinate: Boolean = true

    override val size: GlSize get() {
        check(isSurfaceReady) { "surface is not ready" }
        return _size ?: error("\"surface size is 0\"")
    }

    private val windowSurface: EGLSurface by lazy {
        this.surface?.let {
            return@lazy glEnv.createWindowSurface(it)
        }

        this.surfaceTexture?.let {
            return@lazy glEnv.createWindowSurface(it)
        }

        throw IllegalStateException("surface is not ready. it should not be used yet!")
    }

    fun swapBuffers() {
        glEnv.swapBuffers(windowSurface)
    }

    override suspend fun beforeDraw() {
        glEnv.makeCurrent(windowSurface)
    }

    override suspend fun afterDraw() {
        swapBuffers()
        logger.d { "drawn ($size)" }
    }

    override suspend fun delete() {
        // in this case, windowSurface is not created yet, so we don't have to destroy it
        if (surfaceTexture == null) return

        EGL14.eglMakeCurrent(
            glEnv.eglDisplay, EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT
        )
        EGL14.eglDestroySurface(glEnv.eglDisplay, windowSurface)
    }

    override suspend fun close() {
        delete()
    }

    interface Listener {
        fun onWindowSurfaceBufferReady()
        fun onWindowSurfaceBufferSizeChanged(size: GlSize)
    }
}

data class SurfaceWithSize(
    val surface: Surface,
    val size: GlSize,
)

data class SurfaceTextureWithSize(
    val surfaceTexture: SurfaceTexture,
    val size: GlSize,
)
