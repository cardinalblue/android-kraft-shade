package com.cardinalblue.kraftshade.shader

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.model.GlMat4
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

abstract class TwoTextureInputKraftShader : TextureInputKraftShader() {
    private val secondTextureInput = KraftShaderTextureInput(
        1, "inputImageTexture2", required = true)

    var secondInputTextureId: Int by secondTextureInput.textureIdDelegate

    var texture2SamplingTransformMatrix: GlMat4 by GlUniformDelegate("textureTransformMatrix", required = false)

    init {
        texture2SamplingTransformMatrix = GlMat4().apply {
            setIdentity()
        }
    }

    override fun loadVertexShader(): String = TWO_TEXTURE_INPUT_VERTEX_SHADER

    override fun draw(inputTexture: Texture, size: GlSize, isScreenCoordinate: Boolean) {
        error("call the other draw method with two input textures")
    }

    fun draw(texture1: Texture, texture2: Texture, size: GlSize, isScreenCoordinate: Boolean) {
        secondInputTextureId = texture2.textureId
        super.draw(texture1, size, isScreenCoordinate)
    }

    override fun beforeActualDraw() {
        super.beforeActualDraw()
        secondTextureInput.activate()
    }

    fun updateTexture2SamplingTransformMatrix(block: GlMat4.() -> Unit) {
        texture2SamplingTransformMatrix = texture2SamplingTransformMatrix.apply {
            block()
        }
    }

    companion object {
        @Language("GLSL")
        const val TWO_TEXTURE_INPUT_VERTEX_SHADER = """
    attribute vec4 position;
    attribute vec4 inputTextureCoordinate;
    varying vec2 textureCoordinate;
    varying vec2 texture2Coordinate;

    uniform mat4 textureTransformMatrix;

    uniform highp vec2 resolution;

    void main()
    {
        gl_Position = position;
        textureCoordinate = inputTextureCoordinate.xy;
        texture2Coordinate = (textureTransformMatrix * vec4(inputTextureCoordinate.xy, 0.0, 1.0)).xy;
    }
"""
    }
}
