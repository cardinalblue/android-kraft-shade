package com.cardinalblue.kraftshade.shader

import android.opengl.GLES30
import androidx.annotation.CallSuper
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider

abstract class TextureInputKraftShader(
    samplerUniformName: String = "inputImageTexture",
    sizeUniformName: String = "textureSize",
) : KraftShader() {
    protected val input = KraftShaderTextureInput(
        textureIndex = 0,
        samplerUniformName = samplerUniformName,
        sizeUniformName = sizeUniformName,
        required = false
    )

    protected var _inputTexture: Texture by input.textureDelegate

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
    override fun beforeActualDraw(isScreenCoordinate: Boolean) {
        super.beforeActualDraw(isScreenCoordinate)
        input.activate()
    }

    override fun afterActualDraw() {
        GLES30.glDisableVertexAttribArray(glAttribTextureCoordinate)
    }
}
