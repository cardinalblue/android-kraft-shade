package com.cardinalblue.kraftshade.pipeline

import kotlinx.coroutines.runBlocking
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.env.ProtectedGlEnv
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer

class SerialTextureInputPipeline(
    glEnv: GlEnv,
    private val shaders: List<TextureInputKraftShader>,
) : Pipeline(glEnv) {
    private var buffer1: TextureBuffer? = null
    private var buffer2: TextureBuffer? = null

    fun setInputTexture(texture: Texture) {
        shaders.first().inputTextureId = texture.textureId
    }

    override fun setTargetBuffer(buffer: GlBuffer)  {
        val oldBufferSize = targetBuffer?.size
        val newBufferSize = buffer.size
        // keep the size of the old buffer first is important
        super.setTargetBuffer(buffer)
        // buffer1 and buffer2 are used internally, so if the size if the same, we don't have to
        // recreate them.
        if (oldBufferSize == newBufferSize) return
        runBlocking {
            runDeferred {
                buffer1?.delete()
                buffer2?.delete()
                buffer1 = TextureBuffer(buffer.size)
                buffer2 = TextureBuffer(buffer.size)
            }
        }
    }

    /**
     * Implement ping pong buffer
     */
    override suspend fun GlEnv.execute(protectedGlEnv: ProtectedGlEnv) {
        val buffer1 = requireNotNull(buffer1) { "call setTargetBuffer before executing the pipeline" }
        val buffer2 = requireNotNull(buffer2) { "call setTargetBuffer before executing the pipeline" }
        val targetBuffer = requireNotNull(targetBuffer) { "call setTargetBuffer before executing the pipeline" }

        shaders.forEachIndexed { index, shader ->
            val bufferToDrawTo: GlBuffer = if (index == shaders.lastIndex) {
                targetBuffer
            } else if (index % 2 == 0) { // 0, 2, 4, 6...
                buffer1
            } else { // 1, 3, 5, 7...
                buffer2
            }

            if (index != 0) {
                shader.inputTextureId = if (index % 2 == 0) buffer2.textureId else buffer1.textureId
            }

            shader.drawTo(bufferToDrawTo)
        }
    }

    override suspend fun GlEnv.destroy(protectedGlEnv: ProtectedGlEnv) {
        shaders.forEach {
            it.destroy()
        }
        buffer1?.delete()
        buffer2?.delete()
    }
}
