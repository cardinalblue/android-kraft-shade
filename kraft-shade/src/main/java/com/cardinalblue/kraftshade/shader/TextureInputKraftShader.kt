package com.cardinalblue.kraftshade.shader

import android.opengl.GLES30
import androidx.annotation.CallSuper
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.ExternalOESTexture
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider

abstract class TextureInputKraftShader(
    private val samplerUniformName: String = "inputImageTexture",
    sizeUniformName: String = "textureSize",
) : KraftShader() {
    protected val input = KraftShaderTextureInput(
        textureIndex = 0,
        samplerUniformName = samplerUniformName,
        sizeUniformName = sizeUniformName,
        required = false
    )

    protected var _inputTexture: Texture by input.textureDelegate

    override fun interceptFragmentShader(fragmentShader: String): String {
        return if (_inputTexture is ExternalOESTexture) {
            addOESExtensionToFragmentShader(fragmentShader, samplerUniformName)
        } else {
            super.interceptFragmentShader(fragmentShader)
        }
    }

    protected fun addOESExtensionToFragmentShader(fragmentShader: String, samplerName: String): String {
        // Add OES extension and replace sampler2D with samplerExternalOES only for the specific uniform
        return if (!fragmentShader.contains("#extension GL_OES_EGL_image_external")) {
            "#extension GL_OES_EGL_image_external : require\n" +
                    fragmentShader.replace("sampler2D $samplerName", "samplerExternalOES $samplerName")
        } else {
            fragmentShader.replace("sampler2D $samplerName", "samplerExternalOES $samplerName")
        }
    }


    fun getInputTexture(): Texture {
        return _inputTexture
    }

    /**
     * Sets the input texture for this shader.
     * 
     * **Important**: This method is expected to be called only once in the shader's lifecycle.
     * Replacing the texture dynamically could make the previous texture resource untrackable,
     * and you might forget to call [Texture.delete] on it when it's no longer needed,
     * potentially causing memory leaks.
     * 
     * **For dynamic texture replacement**, use [setInputTexture] with [TextureProvider] instead.
     * For example, use [sampledBitmapTextureProvider] for dynamically loading [android.graphics.Bitmap]s.
     * 
     * @param texture The texture to set as input. This texture will be automatically tracked
     *                by the shader for proper cleanup when the shader is destroyed.
     * 
     * @see setInputTexture
     * @see com.cardinalblue.kraftshade.shader.buffer.sampledBitmapTextureProvider
     */
    open fun setInputTexture(texture: Texture) {
        _inputTexture = texture
    }

    /**
     * Sets the input texture using a [TextureProvider].
     * 
     * This is the **recommended approach for dynamic texture replacement**, as the [TextureProvider]
     * manages the texture lifecycle and can be safely replaced multiple times without causing
     * memory leaks. The provider will handle resource cleanup automatically.
     * 
     * @param texture The texture provider that will supply the texture. Use functions like
     *                [sampledBitmapTextureProvider] for dynamic content loading.
     * 
     * @see sampledBitmapTextureProvider
     */
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
