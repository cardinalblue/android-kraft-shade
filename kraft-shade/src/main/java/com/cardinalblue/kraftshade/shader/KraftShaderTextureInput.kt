package com.cardinalblue.kraftshade.shader

import android.opengl.GLES20
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import kotlin.properties.ReadWriteProperty

/**
 * @param textureIndex starting from 0, but if your are extending TextureInputKraftShader, the first
 *  texture will be inputImageTexture, so for the additional textures, the index should start from 1.
 */
class KraftShaderTextureInput(
    val textureIndex: Int,
    samplerName: String,
    required: Boolean = true,
) {
    fun activate(shader: KraftShader) {
        GLES20.glActiveTexture(mappedTextureIndex())
        // we won't be able to get the real property, so just pass fake ones here.
        // They shouldn't be needed anyway.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdDelegate.getValue(shader, this::textureIdDelegate))
        textureSamplerDelegate.setValue(shader, this::textureSamplerDelegate, textureIndex)
    }

    val textureIdDelegate: ReadWriteProperty<KraftShader, Int> = TextureIdDelegate()

    private val textureSamplerDelegate = GlUniformDelegate<Int>(name = samplerName, required = required)

    private fun mappedTextureIndex(): Int {
        return when(textureIndex) {
            0 -> GLES20.GL_TEXTURE0
            1 -> GLES20.GL_TEXTURE1
            2 -> GLES20.GL_TEXTURE2
            3 -> GLES20.GL_TEXTURE3
            4 -> GLES20.GL_TEXTURE4
            5 -> GLES20.GL_TEXTURE5
            6 -> GLES20.GL_TEXTURE6
            7 -> GLES20.GL_TEXTURE7
            8 -> GLES20.GL_TEXTURE8
            9 -> GLES20.GL_TEXTURE9
            10 -> GLES20.GL_TEXTURE10
            11 -> GLES20.GL_TEXTURE11
            12 -> GLES20.GL_TEXTURE12
            13 -> GLES20.GL_TEXTURE13
            14 -> GLES20.GL_TEXTURE14
            15 -> GLES20.GL_TEXTURE15
            16 -> GLES20.GL_TEXTURE16
            17 -> GLES20.GL_TEXTURE17
            18 -> GLES20.GL_TEXTURE18
            19 -> GLES20.GL_TEXTURE19
            20 -> GLES20.GL_TEXTURE20
            21 -> GLES20.GL_TEXTURE21
            22 -> GLES20.GL_TEXTURE22
            23 -> GLES20.GL_TEXTURE23
            24 -> GLES20.GL_TEXTURE24
            25 -> GLES20.GL_TEXTURE25
            26 -> GLES20.GL_TEXTURE26
            27 -> GLES20.GL_TEXTURE27
            28 -> GLES20.GL_TEXTURE28
            29 -> GLES20.GL_TEXTURE29
            30 -> GLES20.GL_TEXTURE30
            31 -> GLES20.GL_TEXTURE31
            else -> error("textureIndex should be within 0 to 31")
        }
    }
}

private class TextureIdDelegate : ReadWriteProperty<KraftShader, Int> {
    private var textureId: Int = OpenGlUtils.NO_TEXTURE_ID

    override fun getValue(thisRef: KraftShader, property: kotlin.reflect.KProperty<*>): Int {
        return textureId
    }

    override fun setValue(thisRef: KraftShader, property: kotlin.reflect.KProperty<*>, value: Int) {
        require(value != OpenGlUtils.NO_TEXTURE_ID) {
            "Invalid input texture is not loaded"
        }
        textureId = value
    }
}
