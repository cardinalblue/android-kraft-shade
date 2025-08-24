package com.cardinalblue.kraftshade.shader

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.model.GlMat4
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

abstract class ThreeTextureInputKraftShader(
    samplerUniformName: String = "inputImageTexture",
    sizeUniformName: String = "textureSize",
    secondTextureSampleName: String = "inputImageTexture2",
    secondTextureSizeUniformName: String = "textureSize2",
    thirdTextureSampleName: String = "inputImageTexture3",
    thirdTextureSizeUniformName: String = "textureSize3",
) : TwoTextureInputKraftShader(
    samplerUniformName = samplerUniformName,
    sizeUniformName = sizeUniformName,
    secondTextureSampleName = secondTextureSampleName,
    secondTextureSizeUniformName = secondTextureSizeUniformName,
) {
    private val thirdInput = KraftShaderTextureInput(
        textureIndex = 2,
        samplerUniformName = thirdTextureSampleName,
        sizeUniformName = thirdTextureSizeUniformName,
    )

    protected var _thirdInputTexture: Texture by thirdInput.textureDelegate

    var texture3TransformMatrix: GlMat4 by GlUniformDelegate("texture3TransformMatrix", required = false)

    init {
        texture3TransformMatrix = GlMat4().apply {
            setIdentity()
        }
    }

    fun getThirdInputTexture(): Texture {
        return _thirdInputTexture
    }

    /**
     * Sets the third input texture for this shader.
     * 
     * **Important**: This method is expected to be called only once in the shader's lifecycle.
     * Replacing the texture dynamically could make the previous texture resource untrackable,
     * and you might forget to call [Texture.delete] on it when it's no longer needed,
     * potentially causing memory leaks.
     * 
     * **For dynamic texture replacement**, use [setThirdInputTexture] with [TextureProvider] instead.
     * For example, use [sampledBitmapTextureProvider] for dynamically loading [android.graphics.Bitmap]s.
     * 
     * @param texture The texture to set as the third input. This texture will be automatically tracked
     *                by the shader for proper cleanup when the shader is destroyed.
     * 
     * @see setThirdInputTexture
     * @see com.cardinalblue.kraftshade.shader.buffer.sampledBitmapTextureProvider
     */
    open fun setThirdInputTexture(texture: Texture) {
        this._thirdInputTexture = texture
    }

    /**
     * Sets the third input texture using a [TextureProvider].
     * 
     * This is the **recommended approach for dynamic texture replacement**, as the [TextureProvider]
     * manages the texture lifecycle and can be safely replaced multiple times without causing
     * memory leaks. The provider will handle resource cleanup automatically.
     * 
     * @param texture The texture provider that will supply the third texture. Use functions like
     *                [sampledBitmapTextureProvider] for dynamic content loading.
     * 
     * @see sampledBitmapTextureProvider
     */
    fun setThirdInputTexture(texture: TextureProvider) {
        setThirdInputTexture(texture.provideTexture())
    }

    override fun loadVertexShader(): String = THREE_TEXTURE_INPUT_VERTEX_SHADER

    override fun drawWithInput(inputTexture: Texture, size: GlSize, isScreenCoordinate: Boolean) {
        error("call the other drawWithInput method with three input textures")
    }

    fun drawWithInput(texture1: Texture, texture2: Texture, texture3: Texture, size: GlSize, isScreenCoordinate: Boolean) {
        setThirdInputTexture(texture3)
        super.drawWithInput(texture1, texture2, size, isScreenCoordinate)
    }

    override fun beforeActualDraw(isScreenCoordinate: Boolean) {
        super.beforeActualDraw(isScreenCoordinate)
        thirdInput.activate()
    }

    fun updateTexture3TransformMatrix(block: GlMat4.() -> Unit) {
        texture3TransformMatrix = texture3TransformMatrix.apply {
            block()
        }
    }

    companion object {
        @Language("GLSL")
        const val THREE_TEXTURE_INPUT_VERTEX_SHADER = """
attribute vec4 position;
attribute vec4 inputTextureCoordinate;
varying vec2 textureCoordinate;
varying vec2 textureCoordinate2;
varying vec2 textureCoordinate3;

uniform mat4 texture2TransformMatrix;
uniform mat4 texture3TransformMatrix;

uniform highp vec2 resolution;

void main()
{
    gl_Position = position;
    textureCoordinate = inputTextureCoordinate.xy;
    textureCoordinate2 = (texture2TransformMatrix * vec4(inputTextureCoordinate.xy, 0.0, 1.0)).xy;
    textureCoordinate3 = (texture3TransformMatrix * vec4(inputTextureCoordinate.xy, 0.0, 1.0)).xy;
}
"""
    }
}
