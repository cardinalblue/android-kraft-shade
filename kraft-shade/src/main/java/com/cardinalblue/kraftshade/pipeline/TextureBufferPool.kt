package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer
import com.cardinalblue.kraftshade.util.KraftLogger

/**
 * This is a pool of shared TextureBuffers that is used to store the intermediate results of
 * [KraftShader]s or child [Pipeline].
 */
internal class TextureBufferPool(
    private var bufferSize: GlSize
) {
    private val map = mutableMapOf<BufferReference, TextureBuffer>()
    private val availableBuffers: MutableList<TextureBuffer> = mutableListOf()

    val poolSize: Int get() = availableBuffers.size + map.size
    val availableSize: Int get() = availableBuffers.size

    suspend fun changeSize(size: GlSize) {
        delete()
        bufferSize = size
    }

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

    // Do not remove suspend modifier here. This is only to ensure the thread is right.
    suspend fun delete() {
        map.forEach { (_, buffer) -> buffer.delete() }
        map.clear()
        availableBuffers.forEach { it.delete() }
        availableBuffers.clear()
    }

    companion object {
        private val logger = KraftLogger("TextureBufferPool")
    }
}
