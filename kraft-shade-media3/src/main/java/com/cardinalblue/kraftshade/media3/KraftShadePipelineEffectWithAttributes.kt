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
import com.cardinalblue.kraftshade.shader.VertexAttributeData
import com.cardinalblue.kraftshade.shader.VertexAttributeContext
import com.cardinalblue.kraftshade.shader.buffer.ExternalFrameBuffer
import com.cardinalblue.kraftshade.shader.buffer.IdPassingTexture
import com.cardinalblue.kraftshade.util.KraftLogger
import kotlinx.coroutines.runBlocking

/**
 * A Media3 GlEffect that supports custom vertex shader attributes for video processing.
 * This allows for more complex vertex transformations during video export with Transformer.
 *
 * This class works in conjunction with MultiAttributeKraftShader to provide vertex attribute
 * support within the KraftShade pipeline system.
 *
 * Usage example:
 * ```kotlin
 * val vertexAttributeProvider = { presentationTimeUs: Long ->
 *     // Create vertex attributes based on time for animation
 *     val time = presentationTimeUs / 1_000_000f
 *     listOf(
 *         VertexAttributeData(
 *             name = "customAttribute",
 *             size = 2,
 *             buffer = createAnimatedBuffer(time)
 *         )
 *     )
 * }
 *
 * val effect = KraftShadePipelineEffectWithAttributes(
 *     context = context,
 *     provider = { buffer, timeInput, videoTexture ->
 *         pipeline(buffer) {
 *             // Use MultiAttributeKraftShader in your pipeline
 *             step(multiAttributeShader, buffer) { shader ->
 *                 shader.setInputTexture(videoTexture)
 *                 // Vertex attributes are handled by the provider
 *             }
 *         }
 *     },
 *     vertexAttributeProvider = vertexAttributeProvider
 * )
 * ```
 *
 * @param context The Android context
 * @param provider The standard VideoEffectExecutionProvider that creates the pipeline
 * @param vertexAttributeProvider A function that provides vertex attributes based on presentation time
 */
@UnstableApi
class KraftShadePipelineEffectWithAttributes(
    context: Context,
    private val provider: VideoEffectExecutionProvider,
    private val vertexAttributeProvider: (presentationTimeUs: Long) -> List<VertexAttributeData>
) : GlEffect {
    private val program by lazy {
        KraftShadePipelineShaderProgramWithAttributes(context, provider, vertexAttributeProvider)
    }

    override fun toGlShaderProgram(
        context: Context,
        useHdr: Boolean
    ): GlShaderProgram {
        return program
    }
}

/**
 * A custom shader program that supports multiple vertex attributes for Media3 video processing.
 * This extends BaseGlShaderProgram to handle custom vertex data beyond the standard position
 * and texture coordinates.
 *
 * The vertex attributes are provided per frame through the vertexAttributeProvider function,
 * allowing for dynamic vertex data that can change over time (e.g., for animations).
 *
 * This class integrates with the KraftShade pipeline system, allowing MultiAttributeKraftShader
 * instances within the pipeline to access the provided vertex attributes.
 */
@UnstableApi
class KraftShadePipelineShaderProgramWithAttributes(
    private val context: Context,
    private val provider: VideoEffectExecutionProvider,
    private val vertexAttributeProvider: (presentationTimeUs: Long) -> List<VertexAttributeData>
) : BaseGlShaderProgram(true, 1) {
    private lateinit var glEnv: GlEnv
    private val logger = KraftLogger("Media3PipelineEffectWithAttributes")
    private val texture = IdPassingTexture()
    private val externalFrameBuffer = ExternalFrameBuffer(false)
    private var effectExecution: EffectExecution? = null

    private var presentationTime: Float = 0f
    private val presentationTimeInput = sampledInput { presentationTime }

    // Store current vertex attributes for access by shaders in the pipeline
    private var currentVertexAttributes: List<VertexAttributeData> = emptyList()

    private fun init() {
        if (effectExecution != null) return
        glEnv = GlEnv(context, disableDispatcher = false, eglContext = EGL14.eglGetCurrentContext())
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

        // Get vertex attributes for this frame
        currentVertexAttributes = vertexAttributeProvider(presentationTimeUs)

        // Store attributes in a way that MultiAttributeKraftShader instances can access them
        // This could be done through a thread-local or context object
        VertexAttributeContext.setCurrentAttributes(currentVertexAttributes)

        try {
            runBlocking {
                effectExecution.run()
            }
        } finally {
            // Clear the context after drawing
            VertexAttributeContext.clearCurrentAttributes()
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
