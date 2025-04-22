package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import android.opengl.GLES30
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
        GLES30.glGenFramebuffers(1, buffers, 0)
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, size.width, size.height, 0,
            GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null
        )
        bufferId = buffers[0]

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, bufferId)

        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D, textureId, 0
        )

        // be sure to do this otherwise if the later rendering target is WindowSurfaceBuffer then
        // it's gonna fail
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    constructor(width: Int, height: Int) : this(GlSize(width, height))

    override suspend fun beforeDraw() {
        logger.v("Drawing to texture buffer")
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, bufferId)
    }

    override suspend fun afterDraw() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    override fun getBitmap(): Bitmap {
        return withFrameBufferRestored {
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, bufferId)
            com.cardinalblue.kraftshade.OpenGlUtils.createBitmapFromContext(size)
        }
    }

    override suspend fun delete() {
        logger.i("Deleting texture buffer")
        super.delete()
        GLES30.glDeleteFramebuffers(1, intArrayOf(bufferId), 0)
        bufferId = -1
    }
}
