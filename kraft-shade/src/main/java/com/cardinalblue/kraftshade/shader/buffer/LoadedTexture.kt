package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import com.cardinalblue.kraftshade.model.GlSize

class LoadedTexture(
    /**
     * The name of the texture. It is required for the texture to be used in shader serialization.
     * If you don't need to serialize the shader, you can set it to null.
     */
    val name: String? = null
) : Texture() {
    private var _size: GlSize = GlSize(0, 0)
    override val size: GlSize get() = _size

    constructor(
        bitmap: Bitmap,
        /**
         * The name of the texture. It is required for the texture to be used in shader serialization.
         * If you don't need to serialize the shader, you can set it to null.
         */
        name: String? = null
    ) : this(name) {
        load(bitmap)
    }

    fun load(bitmap: Bitmap) {
        _size = GlSize(bitmap.width, bitmap.height)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
    }
}

fun Bitmap.asTexture() = LoadedTexture(this)
