package com.cardinalblue.kraftshade.shader.buffer

import android.opengl.GLES20
import com.cardinalblue.kraftshade.model.GlSize

interface GlBuffer : AutoCloseable, GlBufferProvider {
    val isScreenCoordinate: Boolean
    val size: GlSize
    fun beforeDraw()
    fun afterDraw()
    fun delete()

    fun draw(draw: GlBuffer.() -> Unit) {
        beforeDraw()
        GLES20.glViewport(0, 0, size.width, size.height)
        draw()
        afterDraw()
    }

    override fun provideBuffer(): GlBuffer = this
}

/**
 * Implementations (including their subclasses):
 * - GlBuffer
 *     - TextureBuffer
 *     - WindowSurfaceBuffer
 *     - PixelBuffer
 * - BufferReference
 */
fun interface GlBufferProvider {
    fun provideBuffer(): GlBuffer
}