package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.shader.buffer.Texture

interface SingleInputTextureEffect : Effect {
    fun setInputTextureId(textureId: Int)

    fun setInputTexture(texture: Texture) {
        setInputTextureId(texture.textureId)
    }
}
