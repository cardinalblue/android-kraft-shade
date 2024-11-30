package com.cardinalblue.kraftshade.shader.buffer

import android.opengl.GLES20
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.util.SuspendAutoCloseable

interface GlBuffer : SuspendAutoCloseable, GlBufferProvider {
    val isScreenCoordinate: Boolean
    val size: GlSize
    suspend fun beforeDraw()
    suspend fun afterDraw()
    suspend fun delete()

    suspend fun draw(draw: suspend GlBuffer.() -> Unit) {
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