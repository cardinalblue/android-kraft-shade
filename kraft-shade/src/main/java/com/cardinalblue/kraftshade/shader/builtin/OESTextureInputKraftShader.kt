package com.cardinalblue.kraftshade.shader.builtin

import android.opengl.GLES30
import androidx.annotation.CallSuper
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.KraftShaderTextureInput
import com.cardinalblue.kraftshade.shader.buffer.Texture
import org.intellij.lang.annotations.Language

class OESTextureInputKraftShader(
    private val samplerUniformName: String = "inputImageTexture",
    sizeUniformName: String = "textureSize",
) : KraftShader() {
    private val input = KraftShaderTextureInput(
        textureIndex = 0,
        samplerUniformName = samplerUniformName,
        sizeUniformName = sizeUniformName,
        required = false
    )

    private var _inputTexture: Texture by input.textureDelegate

    override fun loadFragmentShader(): String = OES_TEXTURE_INPUT_FRAGMENT_SHADER

    fun setInputTexture(texture: Texture) {
        _inputTexture = texture
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

@Language("GLSL")
private const val OES_TEXTURE_INPUT_FRAGMENT_SHADER = """
#extension GL_OES_EGL_image_external : require
varying highp vec2 textureCoordinate;

uniform samplerExternalOES inputImageTexture;

void main()
{
    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
}
"""