package com.cardinalblue.kraftshade.shader.buffer

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import com.cardinalblue.kraftshade.model.GlSize

/**
 *  Texture that uses the external OES texture target.
 *  For example, it can be used with camera preview frames or MediaPlayer
 */
class ExternalOESTexture(
    override val autoDelete: Boolean = true
): Texture(true) {

    override val glTextureTarget: Int = GLES11Ext.GL_TEXTURE_EXTERNAL_OES

    // ExternalOESTexture won't use this property
    override val size: GlSize
        get() = GlSize(0, 0)

    override fun getBitmap(): Bitmap {
        throw IllegalStateException("getBitmap() is not supported for ExternalOESTexture")
    }
}