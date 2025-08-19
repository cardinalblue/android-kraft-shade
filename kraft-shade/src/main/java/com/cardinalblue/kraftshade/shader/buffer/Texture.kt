package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES30
import com.cardinalblue.kraftshade.IncompleteFrameBufferAccess
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.util.SuspendAutoCloseable
import com.cardinalblue.kraftshade.withFrameBufferRestored
import com.cardinalblue.kraftshade.withViewPortRestored
import java.nio.IntBuffer

abstract class Texture private constructor(create: Boolean = true) : SuspendAutoCloseable,
    TextureProvider {
    var textureId: Int
        private set

    abstract val size: GlSize

    init {
        if (create) {
            val textures = intArrayOf(0)
            GLES30.glGenTextures(1, textures, 0)
            textureId = textures[0]
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
            GLES30.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES30.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
        } else {
            textureId = OpenGlUtils.NO_TEXTURE_ID
        }
    }

    constructor() : this(true)

    fun isValid() = textureId != OpenGlUtils.NO_TEXTURE_ID

    open suspend fun delete() {
        if (!isValid()) return
        GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
        textureId = OpenGlUtils.NO_TEXTURE_ID
    }

    override suspend fun close(deleteRecursively: Boolean) {
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
                    GLES30.glViewport(0, 0, size.width, size.height)
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
                    val pixels = IntArray(size.area)
                    val buffer = IntBuffer.wrap(pixels)
                    GLES30.glReadPixels(
                        0,
                        0,
                        size.width,
                        size.height,
                        GLES30.GL_RGBA,
                        GLES30.GL_UNSIGNED_BYTE,
                        buffer
                    )
                    buffer
                }
            }
        } finally {
            if (frameBuffer[0] != -1) {
                GLES30.glDeleteFramebuffers(1, frameBuffer, 0)
            }
        }
    }

    companion object {
        val Invalid = object : Texture(false) {
            override val size: GlSize get() = GlSize(0, 0)

            override suspend fun delete() {
                // Do nothing
            }

            override fun getBitmap(): Bitmap {
                throw IllegalStateException("Invalid texture")
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
    /**
     * The name of the texture. It is required for the texture to be used in shader serialization.
     * If you don't need to serialize the shader, you can set it to null.
     */
    name: String? = null,
    private val provider: () -> Bitmap?,
) : TextureProvider {
    /**
     * need to be by lazy, because the texture is created in the GL thread.
     */
    private val loadedTexture by lazy { LoadedTexture(name) }
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

fun sampledBitmapTextureProvider(
    /**
     * The name of the texture. It is required for the texture to be used in shader serialization.
     * If you don't need to serialize the shader, you can set it to null.
     */
    name: String? = null,
    provider: () -> Bitmap?
): TextureProvider {
    return ExternalBitmapTextureProvider(name, provider)
}
