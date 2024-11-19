package com.cardinalblue.kraftshade.pipeline

import kotlinx.coroutines.runBlocking
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.env.ProtectedGlEnv
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer

class SerialTextureInputPipeline(
    glEnv: GlEnv,
    effects: List<SingleInputTextureEffect> = emptyList(),
) : Pipeline(glEnv), SingleInputTextureEffect {
    private var buffer1: TextureBuffer? = null
    private var buffer2: TextureBuffer? = null

    private val effects = mutableListOf<SingleInputTextureEffect>().apply {
        addAll(effects)
    }

    fun addEffect(effect: SingleInputTextureEffect) {
        effects.add(effect)
    }

    fun removeEffect(effect: SingleInputTextureEffect) {
        val removed = effects.remove(effect)
        check(removed) { "effect not found" }
    }

    override fun setInputTextureId(textureId: Int) {
        effects.first().setInputTextureId(textureId)
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

        effects.forEachIndexed { index, effect ->
            val bufferToDrawTo: GlBuffer = if (index == effects.lastIndex) {
                targetBuffer
            } else if (index % 2 == 0) { // 0, 2, 4, 6...
                buffer1
            } else { // 1, 3, 5, 7...
                buffer2
            }

            if (index != 0) {
                val texture = if (index % 2 == 0) buffer2 else buffer1
                effect.setInputTexture(texture)
            }

            effect.drawTo(bufferToDrawTo)
        }
    }

    /**
     * Only call this method when input texture is set. You can just set the input texture once if
     * it doesn't change at all, and then call this function to render the pipeline
     */
    override suspend fun drawTo(buffer: GlBuffer) {
        setTargetBuffer(buffer)
        run()
    }

    override suspend fun GlEnv.destroy(protectedGlEnv: ProtectedGlEnv) {
        effects.forEach {
            it.destroy()
        }
        buffer1?.delete()
        buffer2?.delete()
    }
}
