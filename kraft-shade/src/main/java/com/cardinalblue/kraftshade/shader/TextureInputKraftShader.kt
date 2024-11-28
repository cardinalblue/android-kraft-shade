package com.cardinalblue.kraftshade.shader

import android.opengl.GLES20
import androidx.annotation.CallSuper
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider

abstract class TextureInputKraftShader : KraftShader() {
    private val _inputTexture = KraftShaderTextureInput(
        0, "inputImageTexture", required = false)
    private var _inputTextureId: Int by _inputTexture.textureIdDelegate

    open fun setInputTexture(textureId: Int) {
        _inputTextureId = textureId
    }

    internal fun getInputTextureId() = _inputTextureId

    fun setInputTexture(texture: TextureProvider) {
        setInputTexture(texture.provideTexture().textureId)
    }

    open fun drawWithInput(
        inputTexture: Texture,
        size: GlSize,
        isScreenCoordinate: Boolean,
    ) {
        setInputTexture(inputTexture.textureId)
        draw(size, isScreenCoordinate)
    }

    @CallSuper
    override fun beforeActualDraw() {
        check(_inputTextureId != OpenGlUtils.NO_TEXTURE_ID) {
            "input texture is not set for ${this::class.simpleName}"
        }
        super.beforeActualDraw()
        _inputTexture.activate()
    }

    override fun afterActualDraw() {
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate)
    }
}
