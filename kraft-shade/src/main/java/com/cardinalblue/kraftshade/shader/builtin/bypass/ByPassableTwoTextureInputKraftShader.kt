package com.cardinalblue.kraftshade.shader.builtin.bypass

import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader

/**
 * TODO: temporary solution
 * A better solution will be bypassing by marking a [BufferReference] mapped to another
 * [BufferReference]. Look for 'Dynamic Shader Bypass Mechanism' in README.md.
 */
class ByPassableTwoTextureInputKraftShader<T : TwoTextureInputKraftShader>(
    val wrappedShader: T,
    var bypass: Boolean = false,
    var passTexture1: Boolean = true,
) : TwoTextureInputKraftShader() {

    private var inputTexture1Id = OpenGlUtils.NO_TEXTURE_ID
    private var inputTexture2Id = OpenGlUtils.NO_TEXTURE_ID

    override fun setInputTexture(textureId: Int) {
        wrappedShader.setInputTexture(textureId)
        inputTexture1Id = textureId
    }

    override fun setSecondInputTexture(textureId: Int) {
        wrappedShader.setSecondInputTexture(textureId)
        inputTexture2Id = textureId
    }

    override fun draw(bufferSize: GlSize, isScreenCoordinate: Boolean) {
        if (!bypass) {
            wrappedShader.draw(bufferSize, isScreenCoordinate)
        } else {
            val inputTextureId = if (passTexture1) inputTexture1Id else inputTexture2Id
            super.setInputTexture(inputTextureId)
            super.draw(bufferSize, isScreenCoordinate)
        }
    }

    override fun loadFragmentShader(): String = BYPASS_FRAGMENT_SHADER
}
