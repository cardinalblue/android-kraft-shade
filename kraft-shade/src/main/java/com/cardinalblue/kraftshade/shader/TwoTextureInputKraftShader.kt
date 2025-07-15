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

    open fun setSecondInputTexture(texture: Texture) {
        this._secondInputTexture = texture
    }

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
    }
}
