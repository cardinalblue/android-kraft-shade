package com.cardinalblue.kraftshade.dsl

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader

class PipelineShaderBuilderScope {
    private val shaders: MutableList<TextureInputKraftShader> = mutableListOf()

    suspend fun addShader(block: suspend () -> TextureInputKraftShader) {
        shaders.add(block())
    }

    fun build(): List<TextureInputKraftShader> {
        return shaders
    }
}