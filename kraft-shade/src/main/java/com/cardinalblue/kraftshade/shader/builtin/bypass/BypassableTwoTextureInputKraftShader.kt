package com.cardinalblue.kraftshade.shader.builtin.bypass

import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.Texture

/**
 * TODO: temporary solution
 * A better solution will be bypassing by marking a [BufferReference] mapped to another
 * [BufferReference]. Look for 'Dynamic Shader Bypass Mechanism' in README.md.
 */
class BypassableTwoTextureInputKraftShader<T : TwoTextureInputKraftShader>(
    val wrappedShader: T,
    var bypass: Boolean = false,
    var passTexture1: Boolean = true,
) : TwoTextureInputKraftShader() {

    private var inputTexture1: Texture? = null
    private var inputTexture2: Texture? = null

    override val debugName: String get() = "${super.debugName}(${wrappedShader.debugName})"

    override fun setInputTexture(texture: Texture) {
        wrappedShader.setInputTexture(texture)
        inputTexture1 = texture
    }

    override fun setSecondInputTexture(texture: Texture) {
        wrappedShader.setSecondInputTexture(texture)
        inputTexture2 = texture
    }

    override fun draw(bufferSize: GlSize, isScreenCoordinate: Boolean) {
        if (!bypass) {
            wrappedShader.draw(bufferSize, isScreenCoordinate)
        } else {
            val inputTexture = if (passTexture1) inputTexture1 else inputTexture2
            super.setInputTexture(inputTexture ?: Texture.Invalid)
            super.draw(bufferSize, isScreenCoordinate)
        }
    }

    override fun loadFragmentShader(): String = BYPASS_FRAGMENT_SHADER

    override fun toString(): String {
        return "${this::class.simpleName}(${wrappedShader::class.simpleName})"
    }
}
