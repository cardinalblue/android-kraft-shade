package com.cardinalblue.kraftshade.shader

import android.opengl.GLES30
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import kotlin.properties.ReadWriteProperty

/**
 * @param textureIndex starting from 0, but if your are extending TextureInputKraftShader, the first
 * texture will be inputImageTexture, so for the additional textures, the index should start from 1.
 */
class KraftShaderTextureInput(
    val textureIndex: Int,
    samplerUniformName: String = "inputImageTexture${textureIndex + 1}",
    sizeUniformName: String = "textureSize${textureIndex + 1}",
    required: Boolean = true,
) {
    fun activate(shader: KraftShader) {
        GLES30.glActiveTexture(mappedTextureIndex())
        // we won't be able to get the real property, so just pass fake ones here.
        // They shouldn't be needed anyway.
        val texture = textureDelegate.getValue(shader, this::textureDelegate)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture.textureId)
        textureSamplerDelegate.setValue(shader, this::textureSamplerDelegate, textureIndex)
        textureSizeDelegate.setValue(shader, this::textureSizeDelegate, texture.size)
    }

    val textureDelegate: ReadWriteProperty<KraftShader, Texture> = TextureDelegate()

    private val textureSamplerDelegate = GlUniformDelegate<Int>(name = samplerUniformName, required = required)
    private val textureSizeDelegate = GlUniformDelegate<GlSize>(name = sizeUniformName, required = false)

    private fun mappedTextureIndex(): Int {
        return when(textureIndex) {
            0 -> GLES30.GL_TEXTURE0
            1 -> GLES30.GL_TEXTURE1
            2 -> GLES30.GL_TEXTURE2
            3 -> GLES30.GL_TEXTURE3
            4 -> GLES30.GL_TEXTURE4
            5 -> GLES30.GL_TEXTURE5
            6 -> GLES30.GL_TEXTURE6
            7 -> GLES30.GL_TEXTURE7
            8 -> GLES30.GL_TEXTURE8
            9 -> GLES30.GL_TEXTURE9
            10 -> GLES30.GL_TEXTURE10
            11 -> GLES30.GL_TEXTURE11
            12 -> GLES30.GL_TEXTURE12
            13 -> GLES30.GL_TEXTURE13
            14 -> GLES30.GL_TEXTURE14
            15 -> GLES30.GL_TEXTURE15
            16 -> GLES30.GL_TEXTURE16
            17 -> GLES30.GL_TEXTURE17
            18 -> GLES30.GL_TEXTURE18
            19 -> GLES30.GL_TEXTURE19
            20 -> GLES30.GL_TEXTURE20
            21 -> GLES30.GL_TEXTURE21
            22 -> GLES30.GL_TEXTURE22
            23 -> GLES30.GL_TEXTURE23
            24 -> GLES30.GL_TEXTURE24
            25 -> GLES30.GL_TEXTURE25
            26 -> GLES30.GL_TEXTURE26
            27 -> GLES30.GL_TEXTURE27
            28 -> GLES30.GL_TEXTURE28
            29 -> GLES30.GL_TEXTURE29
            30 -> GLES30.GL_TEXTURE30
            31 -> GLES30.GL_TEXTURE31
            else -> error("textureIndex should be within 0 to 31")
        }
    }
}

private class TextureDelegate(
    private val required: Boolean = true,
) : ReadWriteProperty<KraftShader, Texture> {
    private var texture: Texture = Texture.Invalid

    override fun getValue(thisRef: KraftShader, property: kotlin.reflect.KProperty<*>): Texture {
        return texture
    }

    override fun setValue(thisRef: KraftShader, property: kotlin.reflect.KProperty<*>, value: Texture) {
        require(!required || value.isValid()) {
            "invalid input texture"
        }
        texture = value
    }
}
