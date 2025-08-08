package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import android.opengl.EGL14
import android.opengl.EGLSurface
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.util.KraftLogger

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

    override suspend fun delete() {
        logger.i("Deleting pixel buffer")
        EGL14.eglMakeCurrent(
            glEnv.eglDisplay, EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT
        )
        EGL14.eglDestroySurface(glEnv.eglDisplay, pbufferSurface)
    }

    override suspend fun close(deleteRecursively: Boolean) {
        delete()
    }

    override suspend fun beforeDraw() {
        makeCurrent()
    }

    override suspend fun afterDraw() {
        // do nothing
    }

    suspend fun getBitmap(): Bitmap {
        return glEnv.execute {
            makeCurrent()
            OpenGlUtils.createBitmapFromContext(size)
        }
    }

    suspend fun render(drawBlock: GlBuffer.() -> Unit): Bitmap {
        draw(drawBlock)
        return getBitmap()
    }
}
