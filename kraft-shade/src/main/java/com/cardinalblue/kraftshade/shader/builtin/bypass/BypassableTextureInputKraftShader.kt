package com.cardinalblue.kraftshade.shader.builtin.bypass

import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.Texture
import org.intellij.lang.annotations.Language

/**
 * TODO: temporary solution
 * A better solution will be bypassing by marking a [BufferReference] mapped to another
 * [BufferReference]. Look for 'Dynamic Shader Bypass Mechanism' in README.md.
 */
class BypassableTextureInputKraftShader<T : TextureInputKraftShader>(
    val wrappedShader: T,
    bypass: Boolean = false
) : TextureInputKraftShader() {
    var bypass: Boolean = bypass

    override val debugName: String get() = "${super.debugName}(${wrappedShader.debugName})"

    override fun setInputTexture(texture: Texture) {
        super.setInputTexture(texture)
        wrappedShader.setInputTexture(texture)
    }

    override fun draw(bufferSize: GlSize, isScreenCoordinate: Boolean) {
        if (bypass) {
            super.draw(bufferSize, isScreenCoordinate)
        } else {
            wrappedShader.draw(bufferSize, isScreenCoordinate)
        }
    }

    override fun loadFragmentShader(): String = BYPASS_FRAGMENT_SHADER

    override fun toString(): String {
        return "${this::class.simpleName}(${wrappedShader::class.simpleName})"
    }
}

@Language("GLSL")
internal const val BYPASS_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;
uniform sampler2D inputImageTexture;

void main() {
    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
}
"""