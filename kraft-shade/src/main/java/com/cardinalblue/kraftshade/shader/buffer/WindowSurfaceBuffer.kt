package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLSurface
import android.view.TextureView
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.util.KraftLogger

class WindowSurfaceBuffer(
    private val glEnv: GlEnv,
    private var listener: Listener? = null,
) : GlBuffer {
    private val logger = KraftLogger("WindowSurfaceBuffer")

    // from listener
    private var width: Int = 0
    private var height: Int = 0
    private var surfaceTexture: SurfaceTexture? = null
    val isSurfaceReady: Boolean get() = surfaceTexture != null

    val surfaceTextureListener by lazy {
        object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                logger.i("Surface texture available: ${width}x${height}")
                this@WindowSurfaceBuffer.surfaceTexture = surface
                this@WindowSurfaceBuffer.width = width
                this@WindowSurfaceBuffer.height = height
                listener?.onWindowSurfaceBufferReady()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                logger.i("Surface texture size changed: ${width}x${height}")
                this@WindowSurfaceBuffer.surfaceTexture = surface
                this@WindowSurfaceBuffer.width = width
                this@WindowSurfaceBuffer.height = height
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
        check(width > 0 && height > 0) { "surface size is 0" }
        return GlSize(width, height)
    }

    private val windowSurface: EGLSurface by lazy {
        val surfaceTexture = requireNotNull(surfaceTexture) {
            "surface is not ready. it should not be used yet!"
        }
        glEnv.createWindowSurface(surfaceTexture)
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
