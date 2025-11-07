package com.cardinalblue.kraftshade.media3

import android.content.Context
import android.opengl.EGL14
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BaseGlShaderProgram
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.EffectExecution
import com.cardinalblue.kraftshade.pipeline.input.sampledInput
import com.cardinalblue.kraftshade.shader.buffer.ExternalFrameBuffer
import com.cardinalblue.kraftshade.shader.buffer.IdPassingTexture
import com.cardinalblue.kraftshade.util.KraftLogger
import kotlinx.coroutines.runBlocking

@UnstableApi
class KraftShadePipelineEffect(
    context: Context,
    provider: VideoEffectExecutionProvider
) : GlEffect {
    private val program by lazy { KraftShadePipelineShaderProgram(context, provider) }
    override fun toGlShaderProgram(
        context: Context,
        useHdr: Boolean
    ): GlShaderProgram = program
}

@UnstableApi
class KraftShadePipelineShaderProgram(
    private val context: Context,
    private val provider: VideoEffectExecutionProvider,
) : BaseGlShaderProgram(true, 1) {
    private lateinit var glEnv: GlEnv
    private val logger = KraftLogger("Media3PipelineEffect")
    private val texture = IdPassingTexture()
    private val externalFrameBuffer = ExternalFrameBuffer(false)
    private var effectExecution: EffectExecution? = null

    private var presentationTime: Float = 0f
    private val presentationTimeInput = sampledInput { presentationTime }

    private fun init() {
        if (effectExecution != null) return
        glEnv = GlEnv(context, disableDispatcher = true, eglContext = EGL14.eglGetCurrentContext())
        externalFrameBuffer.setCurrentFrameBuffer()
        check(externalFrameBuffer.isValid()) {
            "External frame buffer is not valid. Make sure configure() is called before drawFrame()."
        }
        runBlocking {
            effectExecution = with(provider) {
                glEnv.execute {
                    provide(
                        glBuffer = externalFrameBuffer,
                        presentationTimeInput = presentationTimeInput,
                        videoTexture = texture
                    )
                }
            }
        }
    }

    override fun configure(
        inputWidth: Int,
        inputHeight: Int
    ): Size {
        val newGlSize = GlSize(inputWidth, inputHeight)
        val effectExecution = effectExecution
        if (effectExecution != null) {
            runBlocking {
                effectExecution.onBufferSizeChanged(newGlSize)
            }
        }
        externalFrameBuffer.size = newGlSize
        return Size(inputWidth, inputHeight)
    }

    override fun drawFrame(inputTexId: Int, presentationTimeUs: Long) {
        init()
        presentationTime = presentationTimeUs / 1_000_000f
        val effectExecution = requireNotNull(effectExecution) {
            "effectExecution is not initialized"
        }
        texture.setId(inputTexId)
        runBlocking {
            effectExecution.run()
        }
    }

    /**
     * Usually we don't need to terminate GlEnv on our own because the context is external. However,
     * if the pipeline is used in a complex Composition setup including multiple sequences, some of
     * the pipeline can be terminated while others are still running. In that case, we should do the
     * resource cleanup on our own to avoid unnecessary memory usage.
     */
    override fun release() {
        super.release()
        runBlocking {
            glEnv.terminate()
        }
        logger.d("released")
    }
}
