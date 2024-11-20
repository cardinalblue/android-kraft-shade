package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.view.TextureView
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.util.KraftLogger
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLSurface

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

    override fun beforeDraw() {
        glEnv.makeCurrent(windowSurface)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun afterDraw() {
        swapBuffers()
    }

    override fun delete() {
        // in this case, windowSurface is not created yet, so we don't have to destroy it
        if (surfaceTexture == null) return

        with(glEnv.egl10) {
            eglMakeCurrent(
                glEnv.eglDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT
            )
            eglDestroySurface(glEnv.eglDisplay, windowSurface)
        }
    }

    override fun close() {
        delete()
    }

    interface Listener {
        fun onWindowSurfaceBufferReady()
    }
}
