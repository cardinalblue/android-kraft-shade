package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.cardinalblue.kraftshade.model.GlSize

class LoadedTexture() : Texture() {
    private var _size: GlSize = GlSize(0, 0)
    override val size: GlSize get() = _size

    constructor(bitmap: Bitmap) : this() {
        load(bitmap)
    }

    fun load(bitmap: Bitmap) {
        _size = GlSize(bitmap.width, bitmap.height)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
    }
}

fun Bitmap.asTexture() = LoadedTexture(this)
