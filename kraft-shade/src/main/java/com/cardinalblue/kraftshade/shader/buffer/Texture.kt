package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import android.opengl.GLES20
import com.cardinalblue.kraftshade.IncompleteFrameBufferAccess
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.util.SuspendAutoCloseable
import com.cardinalblue.kraftshade.withFrameBufferRestored
import com.cardinalblue.kraftshade.withViewPortRestored
import java.nio.IntBuffer

abstract class Texture : SuspendAutoCloseable, TextureProvider {
    var textureId: Int
        private set

    abstract val size: GlSize

    init {
        val textures = intArrayOf(0)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
    }

    fun isValid() = textureId != OpenGlUtils.NO_TEXTURE_ID

    open suspend fun delete() {
        if (!isValid()) return
        GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
        textureId = OpenGlUtils.NO_TEXTURE_ID
    }

    override suspend fun close() {
        delete()
    }

    override fun provideTexture(): Texture = this

    /**
     * If you are working with [TextureBuffer], the size is known by [TextureBuffer.size]. You can use
     * [TextureBuffer.getBitmap] without parameter instead.
     */
    open fun getBitmap(): Bitmap {
        val buffer = getIntBufferFromTexture(textureId, size)
        // Convert to bitmap
        val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    protected fun getIntBufferFromTexture(
        textureId: Int,
        size: GlSize,
    ): IntBuffer {
        val frameBuffer = IntArray(1) { -1 }
        try {
            return withFrameBufferRestored {
                withViewPortRestored {
                    GLES20.glViewport(0, 0, size.width, size.height)
                    // Create framebuffer
                    GLES20.glGenFramebuffers(1, frameBuffer, 0)
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])

                    // Attach texture to framebuffer
                    GLES20.glFramebufferTexture2D(
                        GLES20.GL_FRAMEBUFFER,
                        GLES20.GL_COLOR_ATTACHMENT0,
                        GLES20.GL_TEXTURE_2D,
                        textureId,
                        0
                    )

                    // Check framebuffer status
                    IncompleteFrameBufferAccess.checkAndThrow()

                    // Read pixels
                    val pixels = IntArray(size.area)
                    val buffer = IntBuffer.wrap(pixels)
                    GLES20.glReadPixels(0, 0, size.width, size.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)
                    buffer
                }
            }
        } finally {
            if (frameBuffer[0] != -1) {
                GLES20.glDeleteFramebuffers(1, frameBuffer, 0)
            }
        }
    }
}

/**
 * Implementations (including their subclasses):
 * - Texture
 *     - LoadedTexture
 *     - TextureBuffer
 * - BufferReference
 */
fun interface TextureProvider {
    fun provideTexture(): Texture
}

class ExternalBitmapTextureProvider(
    private val provider: () -> Bitmap?,
) : TextureProvider {
    /**
     * need to be by lazy, because the texture is created in the GL thread.
     */
    private val loadedTexture by lazy { LoadedTexture() }
    private var bitmapHash: Int = 0

    override fun provideTexture(): Texture {
        val bitmap = provider()
        if (bitmap != null) {
            if (bitmap.hashCode() != bitmapHash) {
                loadedTexture.load(bitmap)
                bitmapHash = bitmap.hashCode()
            }
        } else {
            bitmapHash = 0
        }
        return loadedTexture
    }
}

fun sampledBitmapTextureProvider(provider: () -> Bitmap?): TextureProvider {
    return ExternalBitmapTextureProvider(provider)
}
