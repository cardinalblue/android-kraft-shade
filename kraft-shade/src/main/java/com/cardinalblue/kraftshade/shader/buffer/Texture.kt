package com.cardinalblue.kraftshade.shader.buffer

import android.opengl.GLES20
import com.cardinalblue.kraftshade.OpenGlUtils

open class Texture : AutoCloseable, TextureProvider {
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

    open fun delete() {
        if (!isValid()) return
        GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
        textureId = OpenGlUtils.NO_TEXTURE_ID
    }

    override fun close() {
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
