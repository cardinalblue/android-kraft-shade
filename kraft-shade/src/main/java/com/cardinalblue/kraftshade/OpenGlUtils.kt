package com.cardinalblue.kraftshade

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import com.cardinalblue.kraftshade.model.GlSize
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

object OpenGlUtils {
    const val NO_TEXTURE_ID: Int = -1

    val CUBE = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f,
    )

    val VERTICALLY_FLIPPED_CUBE = floatArrayOf(
        -1.0f, 1.0f,
        1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, -1.0f,
    )

    /**
     * For frame buffer objects or PixelBuffer
     */
    val glCubeBuffer: FloatBuffer = CUBE.asFloatBuffer()
        get() {
            field.position(0)
            return field
        }

    /**
     * For screen coordinate (WindowSurface)
     */
    val glVerticallyFlippedCubeBuffer: FloatBuffer = VERTICALLY_FLIPPED_CUBE.asFloatBuffer()
        get() {
            field.position(0)
            return field
        }

    val TEXTURE_NO_ROTATION: FloatArray = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
    )

    val TEXTURE_VERT_FLIP: FloatArray = floatArrayOf(
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
    )

    val glTextureBuffer = TEXTURE_VERT_FLIP.asFloatBuffer()
        get() {
            field.position(0)
            return field
        }

    fun FloatArray.asFloatBuffer(): FloatBuffer {
        return ByteBuffer
            .allocateDirect(size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(this)
            .apply { position(0) }
    }

    @JvmOverloads
    fun loadTexture(img: Bitmap, usedTexId: Int, recycle: Boolean = true): Int {
        val textures = IntArray(1)
        if (usedTexId == NO_TEXTURE_ID) {
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

    fun loadTexture(data: IntBuffer?, width: Int, height: Int, usedTexId: Int): Int {
        val textures = IntArray(1)
        if (usedTexId == NO_TEXTURE_ID) {
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
     * Checks to see if a GLES error has been raised.
     */
    fun checkGlError(op: String) {
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            throw RuntimeException(msg)
        }
    }

    fun createBitmapFromContext(size: GlSize): Bitmap {
        val pixels = IntArray(size.width * size.height)
        val buffer = IntBuffer.wrap(pixels)
        GLES30.glReadPixels(0, 0, size.width, size.height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer)
        return Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888).apply {
            copyPixelsFromBuffer(buffer)
        }
    }

    fun getBitmapFromTextureId(
        textureId: Int,
        width: Int,
        height: Int,
    ): Bitmap {
        val frameBuffer = IntArray(1) { -1 }
        try {
            return withFrameBufferRestored {
                withViewPortRestored {
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
                    IncompleteFrameBufferAccess.checkAndThrow()

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

inline fun <T> withFrameBufferRestored(action: () -> T): T {
    val fboBound = IntArray(size = 1)
    GLES30.glGetIntegerv(GLES30.GL_FRAMEBUFFER_BINDING, fboBound, 0)
    try {
        return action()
    } finally {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboBound[0])
    }
}

inline fun <T> withViewPortRestored(action: () -> T): T {
    val viewPort = IntArray(size = 4)
    GLES30.glGetIntegerv(GLES30.GL_VIEWPORT, viewPort, 0)
    try {
        return action()
    } finally {
        GLES30.glViewport(viewPort[0], viewPort[1], viewPort[2], viewPort[3])
    }
}

class IncompleteFrameBufferAccess(status: Int) : RuntimeException(
    "Framebuffer not complete, status: $status"
) {
    companion object {
        fun checkAndThrow() {
            val status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
            if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                throw IncompleteFrameBufferAccess(status)
            }
        }
    }
}
