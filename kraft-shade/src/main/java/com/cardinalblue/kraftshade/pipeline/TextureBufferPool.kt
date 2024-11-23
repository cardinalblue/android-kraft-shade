package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer
import com.cardinalblue.kraftshade.util.KraftLogger

/**
 * This is a pool of shared TextureBuffers that is used to store the intermediate results of
 * [KraftShader]s or child [Pipeline].
 */
internal class TextureBufferPool(
    val bufferSize: GlSize
) {
    private val map = mutableMapOf<BufferReference, TextureBuffer>()
    private val availableBuffers: MutableList<TextureBuffer> = mutableListOf()

    val poolSize: Int get() = availableBuffers.size + map.size
    val availableSize: Int get() = availableBuffers.size

    operator fun get(bufferReference: BufferReference): TextureBuffer {
        return map[bufferReference] ?: run {
            availableBuffers
                .removeFirstOrNull()
                ?.let { availableBuffer ->
                    logger.d { "reuse a buffer for ${bufferReference.nameForDebug} ($availableSize / $poolSize)" }
                    map[bufferReference] = availableBuffer
                    return availableBuffer
                }

            logger.d { "creating new buffer for ${bufferReference.nameForDebug} ($availableSize / $poolSize)" }
            TextureBuffer(bufferSize).also {
                map[bufferReference] = it
            }
        }
    }

    fun recycle(vararg bufferReferences: BufferReference) {
        bufferReferences.forEach { ref ->
            val buffer = map.remove(ref)
            if (buffer == null) {
                logger.w("${ref.nameForDebug} is not in the pool")
                return
            }
            availableBuffers.add(buffer)
        }
    }

    fun delete() {
        availableBuffers.forEach { it.delete() }
        availableBuffers.clear()
    }

    companion object {
        private val logger = KraftLogger("TextureBufferPool")
    }
}
