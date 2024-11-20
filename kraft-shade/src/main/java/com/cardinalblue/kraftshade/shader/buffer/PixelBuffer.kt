package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.util.KraftLogger
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLSurface

/**
 * Create PixelBuffer on the thread that renders it.
 */
class PixelBuffer internal constructor(
    width: Int,
    height: Int,
    private val glEnv: GlEnv,
) : GlBuffer {
    override val isScreenCoordinate: Boolean = false
    override val size: GlSize = GlSize(width, height)

    private val logger = KraftLogger("PixelBuffer")

    init {
        logger.i("Creating pixel buffer with size: $size")
    }

    private val pbufferSurface: EGLSurface = glEnv.createPbufferSurface(size)

    fun makeCurrent() {
        glEnv.makeCurrent(pbufferSurface)
    }

    override fun delete() {
        logger.i("Deleting pixel buffer")
        with(glEnv.egl10) {
            eglMakeCurrent(
                glEnv.eglDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT
            )
            eglDestroySurface(glEnv.eglDisplay, pbufferSurface)
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
