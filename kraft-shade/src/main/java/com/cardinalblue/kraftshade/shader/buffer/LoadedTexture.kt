package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.cardinalblue.kraftshade.dsl.BasePipelineSetupScope
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.util.UnboundedKraftResource

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

/**
 * You have to handle the release of this texture manually if you use this API. check
 * [BasePipelineSetupScope.asTexture] for an alternative that automatically releases
 * the texture when the pipeline is destroyed.
 */
@UnboundedKraftResource
fun Bitmap.asTextureUnbounded() = LoadedTexture(this)
