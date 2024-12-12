package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider

/**
 * Represents a reference to a texture buffer in the TextureBufferPool.
 * This class acts as a handle that the pipeline system uses to track
 * and manage texture buffer lifecycle.
 */
class BufferReference internal constructor(
    private val pipeline: Pipeline,
    val nameForDebug: String? = null,
) : GlBufferProvider, TextureProvider {
    override fun provideBuffer(): GlBuffer {
        return pipeline.bufferPool[this]
    }

    override fun provideTexture(): Texture {
        return pipeline.getTextureFromBufferPool(this)
    }
}

/**
 * A helper class to create multiple BufferReference objects at once.
 * If you have more than 10 references to create do it in batches of 10.
 */
class BufferReferenceCreator internal constructor(
    private val pipeline: Pipeline,
    private vararg val namesForDebug: String
) {
    operator fun component1(): BufferReference = BufferReference(
        pipeline,
        namesForDebug.getOrNull(0)
    )

    operator fun component2(): BufferReference = BufferReference(
        pipeline,
        namesForDebug.getOrNull(1)
    )
    operator fun component3(): BufferReference = BufferReference(
        pipeline,
        namesForDebug.getOrNull(2)
    )
    operator fun component4(): BufferReference = BufferReference(
        pipeline,
        namesForDebug.getOrNull(3)
    )
    operator fun component5(): BufferReference = BufferReference(
        pipeline,
        namesForDebug.getOrNull(4)
    )
    operator fun component6(): BufferReference = BufferReference(
        pipeline,
        namesForDebug.getOrNull(5)
    )
    operator fun component7(): BufferReference = BufferReference(
        pipeline,
        namesForDebug.getOrNull(6)
    )
    operator fun component8(): BufferReference = BufferReference(
        pipeline,
        namesForDebug.getOrNull(7)
    )
    operator fun component9(): BufferReference = BufferReference(
        pipeline,
        namesForDebug.getOrNull(8)
    )
    operator fun component10(): BufferReference = BufferReference(
        pipeline,
        namesForDebug.getOrNull(9)
    )
}
