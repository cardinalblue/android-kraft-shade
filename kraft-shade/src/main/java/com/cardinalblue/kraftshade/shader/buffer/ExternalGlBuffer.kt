package com.cardinalblue.kraftshade.shader.buffer

import android.opengl.GLES30
import com.cardinalblue.kraftshade.model.GlSize

/**
 * You can use this class if we somehow need to use an external frame buffer. Some obvious examples
 * are from MediaCodec and ExoPlayer. If the buffer is not created by you, and it's managed in some
 * 3rd party library, but if you get to execute OpenGL commands on the right thread and right time,
 * you can use this class to capture the frame buffer id and size, and use it as a GlBuffer.
 *
 * Use [setCurrentFrameBuffer] to capture the current frame buffer id
 */
class ExternalFrameBuffer(
    override val isScreenCoordinate: Boolean
) : GlBuffer {
    var frameBufferId: Int = -1
    override var size: GlSize = GlSize.ZERO

    fun setCurrentFrameBuffer() {
        val buffers = IntArray(1)
        GLES30.glGetIntegerv(GLES30.GL_FRAMEBUFFER_BINDING, buffers, 0)
        frameBufferId = buffers[0]
    }

    fun isValid(): Boolean {
        return frameBufferId != -1 && size != GlSize.ZERO
    }

    override suspend fun beforeDraw() {
        check(frameBufferId != -1) { "frame buffer id is not set" }
        check(size != GlSize.ZERO) { "size is not set" }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferId)
    }

    override suspend fun afterDraw() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    override suspend fun delete() {
        // do nothing, since we don't own the frame buffer
    }

    override suspend fun close(deleteRecursively: Boolean) {
       // do nothing, since we don't own the frame buffer
    }
}