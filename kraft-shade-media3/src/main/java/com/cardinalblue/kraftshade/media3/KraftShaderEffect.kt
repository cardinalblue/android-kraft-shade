package com.cardinalblue.kraftshade.media3

import android.content.Context
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BaseGlShaderProgram
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.IdPassingTexture
import com.cardinalblue.kraftshade.util.KraftLogger
import kotlinx.coroutines.runBlocking

@UnstableApi
class KraftShaderEffect(kraftShader: TextureInputKraftShader) : GlEffect {
    private val program by lazy { KraftShaderProgram(kraftShader) }
    override fun toGlShaderProgram(
        context: Context,
        useHdr: Boolean
    ): GlShaderProgram = program
}

@UnstableApi
class KraftShaderProgram(
    private val shader: TextureInputKraftShader
) : BaseGlShaderProgram(true, 1) {
    private val logger = KraftLogger("Media3KraftShaderProgram")
    private var configuredWidth = 0
    private var configuredHeight = 0

    private val texture = IdPassingTexture()

    override fun configure(
        inputWidth: Int,
        inputHeight: Int
    ): Size {
        configuredWidth = inputWidth
        configuredHeight = inputHeight
        return Size(inputWidth, inputHeight)
    }

    override fun drawFrame(inputTexId: Int, presentationTimeUs: Long) {
        texture.setId(inputTexId)
        shader.setInputTexture(texture)
        shader.draw(
            GlSize(configuredWidth, configuredHeight),
            isScreenCoordinate = false
        )
    }

    override fun release() {
        super.release()
        runBlocking {
            shader.destroy(false)
        }
        logger.d("released")
    }
}
