package com.cardinalblue.kraftshade.shader

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.model.GlMat4
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

abstract class ThreeTextureInputKraftShader(
    secondTextureSampleName: String = "inputImageTexture2",
    thirdTextureSampleName: String = "inputImageTexture3",
) : TwoTextureInputKraftShader(
    secondTextureSampleName = secondTextureSampleName
) {
    private val thirdTextureInput = KraftShaderTextureInput(
        2, thirdTextureSampleName)

    private var thirdInputTextureId: Int by thirdTextureInput.textureIdDelegate

    var texture3TransformMatrix: GlMat4 by GlUniformDelegate("texture3TransformMatrix", required = false)

    init {
        texture3TransformMatrix = GlMat4().apply {
            setIdentity()
        }
    }

    open fun setThirdInputTexture(textureId: Int) {
        this.thirdInputTextureId = textureId
    }

    fun setThirdInputTexture(texture: Texture) {
        setThirdInputTexture(texture.textureId)
    }

    fun setThirdInputTexture(texture: TextureProvider) {
        setThirdInputTexture(texture.provideTexture().textureId)
    }

    override fun loadVertexShader(): String = THREE_TEXTURE_INPUT_VERTEX_SHADER

    override fun drawWithInput(inputTexture: Texture, size: GlSize, isScreenCoordinate: Boolean) {
        error("call the other drawWithInput method with three input textures")
    }

    fun drawWithInput(texture1: Texture, texture2: Texture, texture3: Texture, size: GlSize, isScreenCoordinate: Boolean) {
        setThirdInputTexture(texture3)
        super.drawWithInput(texture1, texture2, size, isScreenCoordinate)
    }

    override fun beforeActualDraw() {
        super.beforeActualDraw()
        thirdTextureInput.activate()
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
