package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer
import com.cardinalblue.kraftshade.util.KraftLogger

/**
 * This is a pool of shared TextureBuffers that is used to store the intermediate results of
 * [KraftShader]s or child [Pipeline].
 */
internal class TextureBufferPool(
    bufferSize: GlSize
) {
    /**
     * Change the size using [changeSize] which is a suspend function.
     */
    var bufferSize: GlSize = bufferSize
        private set

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
                    map[bufferReference] = availableBuffer
                    logger.d { "reuse a buffer for [${bufferReference.nameForDebug}] ($availableSize / $poolSize)" }
                    return availableBuffer
                }

            TextureBuffer(bufferSize).also {
                map[bufferReference] = it
                logger.d { "creating new buffer for ${bufferReference.nameForDebug} ($availableSize / $poolSize)" }
            }
        }
    }

    fun recycle(
        stepNameForDebug: String,
        vararg bufferReferences: BufferReference,
    ) {
        if (bufferReferences.isEmpty()) return
        var numberRecycled = 0
        val recycledDebugNames = mutableListOf<String?>()
        bufferReferences.forEach { ref ->
            val buffer = map.remove(ref)
            if (buffer == null) {
                logger.w("${ref.nameForDebug} is not in the pool")
                return@forEach
            }
            numberRecycled++
            availableBuffers.add(buffer)
            recycledDebugNames.add(ref.nameForDebug)
        }
        logger.d { "recycled $numberRecycled buffers ($availableSize / $poolSize) after step [$stepNameForDebug] [${recycledDebugNames.joinToString(",")}]" }
    }

    fun recycleAll(stepNameForDebug: String) {
        logger.d("recycle all buffers")
        recycle(stepNameForDebug, *map.keys.toTypedArray())
    }

    // Do not remove suspend modifier here. This is only to ensure the thread is right.
    suspend fun delete() {
        logger.d("delete all buffers")
        map.forEach { (_, buffer) -> buffer.delete() }
        map.clear()
        availableBuffers.forEach { it.delete() }
        availableBuffers.clear()
    }

    companion object {
        private val logger = KraftLogger("TextureBufferPool")
    }
}
