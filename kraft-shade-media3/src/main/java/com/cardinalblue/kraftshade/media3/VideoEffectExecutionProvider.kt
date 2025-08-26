package com.cardinalblue.kraftshade.media3

import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.pipeline.EffectExecution
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.buffer.Texture

fun interface VideoEffectExecutionProvider {
    suspend fun GlEnvDslScope.provide(
        glBuffer: GlBuffer,
        presentationTimeInput: Input<Float>,
        videoTexture: Texture,
    ): EffectExecution
}