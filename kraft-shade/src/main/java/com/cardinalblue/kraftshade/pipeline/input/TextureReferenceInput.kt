package com.cardinalblue.kraftshade.pipeline.input

import com.cardinalblue.kraftshade.pipeline.BufferReference
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider

open class TextureInput(
    private val textureProvider: TextureProvider,
) : Input<Texture> {
    override fun get(): Texture {
        return textureProvider.provideTexture()
    }
}

class TextureReferenceInput(
    internal val bufferReference: BufferReference
) : TextureInput(bufferReference)

fun TextureProvider.asTextureInput(): TextureInput {
    return if (this is BufferReference) {
        TextureReferenceInput(this)
    } else {
        TextureInput(this)
    }
}
