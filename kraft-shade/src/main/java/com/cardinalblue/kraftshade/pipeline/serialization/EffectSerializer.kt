package com.cardinalblue.kraftshade.pipeline.serialization

import android.content.Context
import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.BufferReference
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.pipeline.RunShaderStep
import com.cardinalblue.kraftshade.pipeline.TextureBufferPool
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.ThreeTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider
import com.cardinalblue.kraftshade.util.KraftLogger
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlin.collections.forEach

class EffectSerializer(private val context: Context, private val size: GlSize) {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    suspend fun serialize(
        block: suspend GraphPipelineSetupScope.() -> Unit,
    ): String {
        val glEnv = GlEnv(context)
        val pipeline = Pipeline(
            glEnv = glEnv,
            bufferPool = TextureBufferPool(size),
            automaticRecycle = false,
        )

        val target = pipeline.bufferPool[BufferReference(pipeline)]

        // collect pipeline steps
        glEnv.execute {
            val scope = GraphPipelineSetupScope(glEnv, pipeline, target)
            scope.block()
        }

        val nodes = pipeline.steps
            .filterIsInstance<RunShaderStep<KraftShader>>()
            .map { step ->
                val shader = step.shader
                val setupAction = step.setupAction
                with(pipeline.pipelineRunningScope) { setupAction(shader) }

                PipelineShaderNode(
                    shaderClassName = shader::class.qualifiedName!!,
                    shaderProperties = shader.properties.toMap(), // clone properties to avoid modification
                    inputs = buildList {
                        fun addTexture(texture: Texture) {
                            if (texture is LoadedTexture) {
                                val textureName = requireNotNull(texture.name) {
                                    "Texture name cannot be null for LoadedTexture"
                                }
                                add(textureName)
                            } else {
                                if (texture != Texture.Invalid) add(texture.toString()) // use object's toString() for output reference
                            }
                        }

                        if (shader is TextureInputKraftShader) {
                            addTexture(shader.getInputTexture())
                        }
                        if (shader is TwoTextureInputKraftShader) {
                            addTexture(shader.getSecondInputTexture())
                        }
                        if (shader is ThreeTextureInputKraftShader) {
                            addTexture(shader.getThirdInputTexture())
                        }
                    },
                    output = step.targetBuffer.provideBuffer()
                        .toString() // use object's toString() for output reference
                )
            }

        pipeline.destroy()
        glEnv.terminate()

        return gson.toJson(nodes)
    }
}

class SerializedEffect(
    private val json: String,
    private val getTextureProvider: (String) -> TextureProvider?,
) {
    constructor(json: String, textures: Map<String, TextureProvider>) : this(
        json,
        getTextureProvider = { name -> textures[name] }
    )

    private val logger = KraftLogger("SerializedEffect")
    private val buffers = mutableMapOf<String, BufferReference>()

    suspend fun applyTo(pipeline: Pipeline, targetBuffer: GlBufferProvider) {
        val records = Gson().fromJson(json, Array<PipelineShaderNode>::class.java)

        val steps = records.mapIndexed { index, record ->
            val shader = record.createShader()

            val firstInput = record.inputs.getOrNull(0)?.let { getTexture(it) }
            val secondInput = record.inputs.getOrNull(1)?.let { getTexture(it) }
            val thirdInput = record.inputs.getOrNull(2)?.let { getTexture(it) }

            RunShaderStep(
                stepIndex = index,
                runContext = pipeline.runContext,
                shader = shader,
                targetBuffer = if (index != records.lastIndex) {
                    buffers.getOrPut(record.output) { BufferReference(pipeline) }
                } else {
                    targetBuffer
                }
            ) { shader ->
                when (shader) {
                    is ThreeTextureInputKraftShader -> {
                        firstInput?.let { shader.setInputTexture(it) }
                            ?: logger.w(
                                "First input texture is null for shader ${shader::class.simpleName}"
                            )
                        secondInput?.let { shader.setSecondInputTexture(it) }
                            ?: logger.w(
                                "Second input texture is null for shader ${shader::class.simpleName}"
                            )
                        thirdInput?.let { shader.setThirdInputTexture(it) }
                            ?: logger.w(
                                "Third input texture is null for shader ${shader::class.simpleName}"
                            )
                    }

                    is TwoTextureInputKraftShader -> {
                        firstInput?.let { shader.setInputTexture(it) }
                            ?: logger.w(
                                "First input texture is null for shader ${shader::class.simpleName}"
                            )
                        secondInput?.let { shader.setSecondInputTexture(it) }
                            ?: logger.w(
                                "Second input texture is null for shader ${shader::class.simpleName}"
                            )
                    }

                    is TextureInputKraftShader -> {
                        firstInput?.let { shader.setInputTexture(it) }
                            ?: logger.w(
                                "Input texture is null for shader ${shader::class.simpleName}"
                            )
                    }
                }
            }
        }

        steps.forEach { step -> pipeline.addStep(step) }
    }

    private fun getTexture(name: String): TextureProvider? {
        return buffers[name] ?: getTextureProvider(name)
    }
}

private data class PipelineShaderNode(
    val shaderClassName: String,
    val shaderProperties: Map<String, Any>,
    val inputs: List<String>,
    val output: String,
) {
    fun createShader(): KraftShader {
        val shaderClass = Class.forName(shaderClassName)
        val constructor = shaderClass.getConstructor()
        return (constructor.newInstance() as KraftShader).apply {
            shaderProperties.mapValues { (_, value) ->
                when (value) {
                    is List<*> -> value.map { (it as Double).toFloat() }.toFloatArray()
                    is Int -> value.toFloat()
                    is Double -> value.toFloat()
                    else -> value
                }
            }.let {
                setUniforms(it)
            }
        }
    }
}