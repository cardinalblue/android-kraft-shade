package com.cardinalblue.kraftshade.shader

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.model.GlMat4
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider
import com.cardinalblue.kraftshade.shader.builtin.bypass.BypassableTwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

abstract class TwoTextureInputKraftShader(
    samplerUniformName: String = "inputImageTexture",
    sizeUniformName: String = "textureSize",
    secondTextureSampleName: String = "inputImageTexture2",
    secondTextureSizeUniformName: String = "textureSize2",
) : TextureInputKraftShader(samplerUniformName, sizeUniformName) {
    /**
     * TODO change required back to true after removing [BypassableTwoTextureInputKraftShader].
     */
    private val secondInput = KraftShaderTextureInput(
        textureIndex = 1,
        samplerUniformName = secondTextureSampleName,
        sizeUniformName = secondTextureSizeUniformName,
        required = false
    )

    protected var _secondInputTexture: Texture by secondInput.textureDelegate

    var texture2TransformMatrix: GlMat4 by GlUniformDelegate("texture2TransformMatrix", required = false)

    init {
        texture2TransformMatrix = GlMat4().apply {
            setIdentity()
        }
    }

    fun getSecondInputTexture(): Texture {
        return _secondInputTexture
    }

    /**
     * Sets the second input texture for this shader.
     * 
     * **Important**: This method is expected to be called only once in the shader's lifecycle.
     * Replacing the texture dynamically could make the previous texture resource untrackable,
     * and you might forget to call [Texture.delete] on it when it's no longer needed,
     * potentially causing memory leaks.
     * 
     * **For dynamic texture replacement**, use [setSecondInputTexture] with [TextureProvider] instead.
     * For example, use [sampledBitmapTextureProvider] for dynamically loading [android.graphics.Bitmap]s.
     * 
     * @param texture The texture to set as the second input. This texture will be automatically tracked
     *                by the shader for proper cleanup when the shader is destroyed.
     * 
     * @see setSecondInputTexture
     * @see com.cardinalblue.kraftshade.shader.buffer.sampledBitmapTextureProvider
     */
    open fun setSecondInputTexture(texture: Texture) {
        this._secondInputTexture = texture
    }

    /**
     * Sets the second input texture using a [TextureProvider].
     * 
     * This is the **recommended approach for dynamic texture replacement**, as the [TextureProvider]
     * manages the texture lifecycle and can be safely replaced multiple times without causing
     * memory leaks. The provider will handle resource cleanup automatically.
     * 
     * @param texture The texture provider that will supply the second texture. Use functions like
     *                [sampledBitmapTextureProvider] for dynamic content loading.
     * 
     * @see sampledBitmapTextureProvider
     */
    fun setSecondInputTexture(texture: TextureProvider) {
        setSecondInputTexture(texture.provideTexture())
    }

    override fun loadVertexShader(): String = TWO_TEXTURE_INPUT_VERTEX_SHADER

    override fun drawWithInput(inputTexture: Texture, size: GlSize, isScreenCoordinate: Boolean) {
        error("call the other drawWithInput method with two input textures")
    }

    fun drawWithInput(texture1: Texture, texture2: Texture, size: GlSize, isScreenCoordinate: Boolean) {
        setSecondInputTexture(texture2)
        super.drawWithInput(texture1, size, isScreenCoordinate)
    }

    override fun beforeActualDraw(isScreenCoordinate: Boolean) {
        super.beforeActualDraw(isScreenCoordinate)
        secondInput.activate()
    }

    fun updateTexture2SamplingTransformMatrix(block: GlMat4.() -> Unit) {
        texture2TransformMatrix = texture2TransformMatrix.apply {
            block()
        }
    }

    companion object {
        @Language("GLSL")
        const val TWO_TEXTURE_INPUT_VERTEX_SHADER = """
attribute vec4 position;
attribute vec4 inputTextureCoordinate;
varying vec2 textureCoordinate;
varying vec2 textureCoordinate2;

uniform mat4 texture2TransformMatrix;

uniform highp vec2 resolution;

void main()
{
    gl_Position = position;
    textureCoordinate = inputTextureCoordinate.xy;
    textureCoordinate2 = (texture2TransformMatrix * vec4(inputTextureCoordinate.xy, 0.0, 1.0)).xy;
}
"""

    const val TWO_TEXTURE_INPUT_VERTEX_SHADER_30 = """
#version 300 es
in vec4 position;
in vec4 inputTextureCoordinate;
out vec2 textureCoordinate;
out vec2 textureCoordinate2;

uniform highp mat4 texture2TransformMatrix;

uniform highp vec2 resolution;

void main()
{
    gl_Position = position;
    textureCoordinate = inputTextureCoordinate.xy;
    textureCoordinate2 = (texture2TransformMatrix * vec4(inputTextureCoordinate.xy, 0.0, 1.0)).xy;
}
"""
}
}
