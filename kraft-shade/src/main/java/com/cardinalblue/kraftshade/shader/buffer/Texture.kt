package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import android.opengl.GLES20
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.util.SuspendAutoCloseable

open class Texture : SuspendAutoCloseable, TextureProvider {
    var textureId: Int
        private set

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
    private val loadedTexture = LoadedTexture()
    private var bitmapHash: Int = 0

    override fun provideTexture(): Texture {
        val bitmap = provider()
        if (bitmap != null) {
            if (bitmap.hashCode() != bitmapHash) {
                loadedTexture.load(bitmap)
                bitmapHash = bitmap.hashCode()
            }
        }
        return loadedTexture
    }
}

fun externalBitmapTextureProvider(provider: () -> Bitmap?): TextureProvider {
    return ExternalBitmapTextureProvider(provider)
}
