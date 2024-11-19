package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.env.ProtectedGlEnv
import com.cardinalblue.kraftshade.model.GlSize
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLSurface

/**
 * Create PixelBuffer on the thread that renders it.
 */
class PixelBuffer internal constructor(
    width: Int,
    height: Int,
    private val protectedGlEnv: ProtectedGlEnv,
) : GlBuffer {
    override val isScreenCoordinate: Boolean = false
    override val size: GlSize = GlSize(width, height)

    private val pbufferSurface: EGLSurface = protectedGlEnv.createPbufferSurface(size)

    fun makeCurrent() {
        protectedGlEnv.makeCurrent(pbufferSurface)
    }

    override fun delete() {
        with(protectedGlEnv.egl10) {
            eglMakeCurrent(
                protectedGlEnv.eglDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT
            )
            eglDestroySurface(protectedGlEnv.eglDisplay, pbufferSurface)
        }
    }

    override fun close() {
        delete()
    }

    override fun beforeDraw() {
        makeCurrent()
    }

    override fun afterDraw() {
        // do nothing
    }

    fun getBitmap(): Bitmap {
        makeCurrent()
        return OpenGlUtils.createBitmapFromBuffer(size)
    }

    fun render(drawBlock: GlBuffer.() -> Unit): Bitmap {
        draw(drawBlock)
        return getBitmap()
    }
}
