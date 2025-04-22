package com.cardinalblue.kraftshade

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import com.cardinalblue.kraftshade.model.GlSize
import java.nio.IntBuffer

/**
 * Utility class for OpenGL ES 3.0 operations.
 * Extends the functionality provided by [OpenGlUtils] with OpenGL ES 3.0 specific features.
 */
object OpenGlUtils30 {
    /**
     * Checks to see if a GLES error has been raised.
     */
    fun checkGlError(op: String) {
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            throw RuntimeException(msg)
        }
    }

    /**
     * Loads a texture from a bitmap using OpenGL ES 3.0.
     * 
     * @param img The bitmap to load as a texture
     * @param usedTexId The texture ID to use, or [OpenGlUtils.NO_TEXTURE_ID] to generate a new one
     * @param recycle Whether to recycle the bitmap after loading
     * @return The texture ID
     */
    @JvmOverloads
    fun loadTexture(img: Bitmap, usedTexId: Int, recycle: Boolean = true): Int {
        val textures = IntArray(1)
        if (usedTexId == OpenGlUtils.NO_TEXTURE_ID) {
            GLES30.glGenTextures(1, textures, 0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )

            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, img, 0)
        } else {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId)
            GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, img)
            textures[0] = usedTexId
        }
        if (recycle) {
            img.recycle()
        }
        return textures[0]
    }

    /**
     * Loads a texture from an IntBuffer using OpenGL ES 3.0.
     * 
     * @param data The IntBuffer containing the texture data
     * @param width The width of the texture
     * @param height The height of the texture
     * @param usedTexId The texture ID to use, or [OpenGlUtils.NO_TEXTURE_ID] to generate a new one
     * @return The texture ID
     */
    fun loadTexture(data: IntBuffer?, width: Int, height: Int, usedTexId: Int): Int {
        val textures = IntArray(1)
        if (usedTexId == OpenGlUtils.NO_TEXTURE_ID) {
            GLES30.glGenTextures(1, textures, 0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height,
                0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, data
            )
        } else {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId)
            GLES30.glTexSubImage2D(
                GLES30.GL_TEXTURE_2D, 0, 0, 0, width,
                height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, data
            )
            textures[0] = usedTexId
        }
        return textures[0]
    }

    /**
     * Creates a bitmap from the current OpenGL ES 3.0 context.
     * 
     * @param size The size of the bitmap to create
     * @return The created bitmap
     */
    fun createBitmapFromContext(size: GlSize): Bitmap {
        val pixels = IntArray(size.width * size.height)
        val buffer = IntBuffer.wrap(pixels)
        GLES30.glReadPixels(0, 0, size.width, size.height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer)
        return Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888).apply {
            copyPixelsFromBuffer(buffer)
        }
    }

    /**
     * Gets a bitmap from a texture ID using OpenGL ES 3.0.
     * 
     * @param textureId The texture ID to get the bitmap from
     * @param width The width of the texture
     * @param height The height of the texture
     * @return The created bitmap
     */
    fun getBitmapFromTextureId(
        textureId: Int,
        width: Int,
        height: Int,
    ): Bitmap {
        val frameBuffer = IntArray(1) { -1 }
        try {
            return withFrameBufferRestored30 {
                withViewPortRestored30 {
                    GLES30.glViewport(0, 0, width, height)
                    // Create framebuffer
                    GLES30.glGenFramebuffers(1, frameBuffer, 0)
                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0])

                    // Attach texture to framebuffer
                    GLES30.glFramebufferTexture2D(
                        GLES30.GL_FRAMEBUFFER,
                        GLES30.GL_COLOR_ATTACHMENT0,
                        GLES30.GL_TEXTURE_2D,
                        textureId,
                        0
                    )

                    // Check framebuffer status
                    IncompleteFrameBufferAccess30.checkAndThrow()

                    // Read pixels
                    createBitmapFromContext(GlSize(width, height))
                }
            }
        } finally {
            if (frameBuffer[0] != -1) {
                GLES30.glDeleteFramebuffers(1, frameBuffer, 0)
            }
        }
    }
}

/**
 * Executes the provided action with the framebuffer state restored after execution.
 * 
 * @param action The action to execute
 * @return The result of the action
 */
inline fun <T> withFrameBufferRestored30(action: () -> T): T {
    val fboBound = IntArray(size = 1)
    GLES30.glGetIntegerv(GLES30.GL_FRAMEBUFFER_BINDING, fboBound, 0)
    try {
        return action()
    } finally {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboBound[0])
    }
}

/**
 * Executes the provided action with the viewport state restored after execution.
 * 
 * @param action The action to execute
 * @return The result of the action
 */
inline fun <T> withViewPortRestored30(action: () -> T): T {
    val viewPort = IntArray(size = 4)
    GLES30.glGetIntegerv(GLES30.GL_VIEWPORT, viewPort, 0)
    try {
        return action()
    } finally {
        GLES30.glViewport(viewPort[0], viewPort[1], viewPort[2], viewPort[3])
    }
}

/**
 * Exception thrown when a framebuffer is incomplete.
 */
class IncompleteFrameBufferAccess30(status: Int) : RuntimeException(
    "Framebuffer not complete, status: $status"
) {
    companion object {
        /**
         * Checks if the framebuffer is complete and throws an exception if it is not.
         */
        fun checkAndThrow() {
            val status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
            if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                throw IncompleteFrameBufferAccess30(status)
            }
        }
    }
}
