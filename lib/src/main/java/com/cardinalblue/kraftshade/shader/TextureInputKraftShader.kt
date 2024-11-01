package com.cardinalblue.kraftshade.shader

import android.opengl.GLES20
import androidx.annotation.CallSuper
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.Texture

abstract class TextureInputKraftShader : KraftShader() {
    private val inputTexture = KraftShaderTextureInput(
        0, "inputImageTexture", required = false)
    var inputTextureId: Int by inputTexture.textureIdDelegate

    open fun draw(
        inputTexture: Texture,
        size: GlSize,
        isScreenCoordinate: Boolean,
    ) {
        this.inputTextureId = inputTexture.textureId
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
