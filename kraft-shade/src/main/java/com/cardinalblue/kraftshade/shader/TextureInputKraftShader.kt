package com.cardinalblue.kraftshade.shader

import android.opengl.GLES20
import androidx.annotation.CallSuper
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.SingleInputTextureEffect
import com.cardinalblue.kraftshade.shader.buffer.Texture

abstract class TextureInputKraftShader : KraftShader(), SingleInputTextureEffect {
    private val inputTexture = KraftShaderTextureInput(
        0, "inputImageTexture", required = false)
    private var _inputTextureId: Int by inputTexture.textureIdDelegate

    override fun setInputTextureId(textureId: Int) {
        _inputTextureId = textureId
    }

    open fun draw(
        inputTexture: Texture,
        size: GlSize,
        isScreenCoordinate: Boolean,
    ) {
        this._inputTextureId = inputTexture.textureId
        draw(size, isScreenCoordinate)
    }

    @CallSuper
    override fun beforeActualDraw() {
        super.beforeActualDraw()
        inputTexture.activate()
    }

    override fun afterActualDraw() {
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate)
    }
}
