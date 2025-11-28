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
class KraftShaderEffect<T : TextureInputKraftShader>(
    kraftShader: T,
    setup: T.() -> Unit = {},
    update: T.(Long) -> Unit = { _ -> }
) : GlEffect {
    private val program by lazy { KraftShaderProgram(kraftShader, setup, update) }
    override fun toGlShaderProgram(
        context: Context,
        useHdr: Boolean
    ): GlShaderProgram = program
}

/**
 * @param setup This is an one-time setup callback for setup operations that need to run on the
 *  correct thread that is in the right GL context. It's called when the shader is used for rendering
 *  the first frame.
 */
@UnstableApi
class KraftShaderProgram<T : TextureInputKraftShader>(
    private val shader: T,
    private val setup: T.() -> Unit = {},
    private val update: T.(Long) -> Unit = { _ -> }
) : BaseGlShaderProgram(true, 1) {
    private val logger = KraftLogger("Media3KraftShaderProgram")
    private var configuredWidth = 0
    private var configuredHeight = 0
    private var isSetup = false

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
        if (!isSetup) {
            shader.setup()
            isSetup = true
        }

        texture.setId(inputTexId)
        shader.setInputTexture(texture)
        shader.update(presentationTimeUs)
        shader.draw(
            GlSize(configuredWidth, configuredHeight),
            isScreenCoordinate = false
        )
    }

    override fun release() {
        super.release()
        runBlocking {
            shader.destroy(true)
        }
        logger.d("released")
    }
}
