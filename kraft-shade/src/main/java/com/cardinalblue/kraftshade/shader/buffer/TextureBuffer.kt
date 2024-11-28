package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import android.opengl.GLES20
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.util.KraftLogger
import com.cardinalblue.kraftshade.withFrameBufferRestored

class TextureBuffer(
    override val size: GlSize
) : Texture(), GlBuffer {
    private val logger = KraftLogger("TextureBuffer")

    private var bufferId: Int = 0
    override val isScreenCoordinate: Boolean = false

    init {
        logger.i("Creating texture buffer: ${size.width}x${size.height}")
        val buffers = intArrayOf(-1)
        GLES20.glGenFramebuffers(1, buffers, 0)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, size.width, size.height, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )
        bufferId = buffers[0]

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, bufferId)

        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, textureId, 0
        )

        // be sure to do this otherwise if the later rendering target is WindowSurfaceBuffer then
        // it's gonna fail
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    constructor(width: Int, height: Int) : this(GlSize(width, height))

    override suspend fun beforeDraw() {
        logger.v("Drawing to texture buffer")
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, bufferId)
    }

    override suspend fun afterDraw() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    override fun getBitmap(): Bitmap {
        return withFrameBufferRestored {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, bufferId)
            com.cardinalblue.kraftshade.OpenGlUtils.createBitmapFromBuffer(size)
        }
    }

    override suspend fun delete() {
        logger.i("Deleting texture buffer")
        super.delete()
        GLES20.glDeleteFramebuffers(1, intArrayOf(bufferId), 0)
        bufferId = -1
    }
}
