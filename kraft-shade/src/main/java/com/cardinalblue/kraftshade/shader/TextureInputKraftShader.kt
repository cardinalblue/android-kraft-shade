package com.cardinalblue.kraftshade.shader

import android.opengl.GLES20
import androidx.annotation.CallSuper
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider

abstract class TextureInputKraftShader : KraftShader() {
    private val input = KraftShaderTextureInput(
        0, "inputImageTexture", required = false)
    private var _inputTexture: Texture by input.textureDelegate

    open fun setInputTexture(texture: Texture) {
        _inputTexture = texture
    }

    fun setInputTexture(texture: TextureProvider) {
        setInputTexture(texture.provideTexture())
    }

    open fun drawWithInput(
        inputTexture: Texture,
        size: GlSize,
        isScreenCoordinate: Boolean,
    ) {
        setInputTexture(inputTexture)
        draw(size, isScreenCoordinate)
    }

    @CallSuper
    override fun beforeActualDraw() {
        super.beforeActualDraw()
        input.activate()
    }

    override fun afterActualDraw() {
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate)
    }
}
