package com.cardinalblue.kraftshade.pipeline

import android.content.Context
import android.util.Log
import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.ThreeTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlin.collections.forEach
import kotlin.reflect.full.primaryConstructor

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
                    shaderProperties = shader.properties,
                    inputs = buildList {
                        fun addTexture(texture: TextureProvider) {
                            if (texture is LoadedTexture) {
                                val textureName = requireNotNull(texture.name) {
                                    "Texture name cannot be null for LoadedTexture"
                                }
                                add(textureName)
                            } else {
                                add(texture.toString()) // use object's toString() for output reference
                            }
                        }

                        if (shader is TextureInputKraftShader) {
                            addTexture(shader._inputTexture)
                        }
                        if (shader is TwoTextureInputKraftShader) {
                            addTexture(shader._secondInputTexture)
                        }
                        if (shader is ThreeTextureInputKraftShader) {
                            addTexture(shader._thirdInputTexture)
                        }
                    },
                    output = step.targetBuffer.provideBuffer().toString() // use object's toString() for output reference
                )
            }

        pipeline.destroy()
        glEnv.terminate()

        return gson.toJson(nodes)
    }
}

class JsonPipeline(
    env: GlEnv,
    private val json: String,
    private val targetBuffer: GlBuffer,
    automaticRecycle: Boolean = true,
    private val textures: Map<String, TextureProvider> = emptyMap(),
    private val getAsset: (suspend (String) -> TextureProvider)? = null
) : Pipeline(
    glEnv = env,
    bufferPool = TextureBufferPool(targetBuffer.size),
    automaticRecycle = automaticRecycle
) {

    companion object {
        private const val TAG = "JsonPipeline"
    }

    override suspend fun run() {
        val steps = parse(json)
        steps.forEach { step -> addStep(step) }
        super.run()
    }

    private suspend fun parse(json: String): List<RunShaderStep<KraftShader>> {
        val records = Gson().fromJson(
            json,
            Array<PipelineShaderNode>::class.java
        )

        val buffers = mutableMapOf<String, TextureBuffer>()

        suspend fun getTexture(name: String): TextureProvider? {
            return if (name.startsWith("textures/")) getAsset?.invoke(name)
            else textures[name] ?: buffers[name]
        }

        return records.mapIndexed { index, record ->
            val shader = record.createShader()

            when (shader) {
                is ThreeTextureInputKraftShader -> {
                    val firstInput = getTexture(record.inputs[0])
                    val secondInput = getTexture(record.inputs[1])
                    val thirdInput = getTexture(record.inputs[2])
                    firstInput?.let { shader.setInputTexture(it) }
                        ?: Log.w(
                            TAG,
                            "First input texture is null for shader ${shader::class.simpleName}"
                        )
                    secondInput?.let { shader.setSecondInputTexture(it) }
                        ?: Log.w(
                            TAG,
                            "Second input texture is null for shader ${shader::class.simpleName}"
                        )
                    thirdInput?.let { shader.setThirdInputTexture(it) }
                        ?: Log.w(
                            TAG,
                            "Third input texture is null for shader ${shader::class.simpleName}"
                        )
                }

                is TwoTextureInputKraftShader -> {
                    val firstInput = getTexture(record.inputs[0])
                    firstInput?.let { shader.setInputTexture(it) }
                        ?: Log.w(
                            TAG,
                            "First input texture is null for shader ${shader::class.simpleName}"
                        )
                    if (record.inputs.size >= 2) {
                        val secondInput = getTexture(record.inputs[1])
                        secondInput?.let { shader.setSecondInputTexture(it) }
                            ?: Log.w(
                                TAG,
                                "Second input texture is null for shader ${shader::class.simpleName}"
                            )
                    }

                }

                is TextureInputKraftShader -> {
                    if (record.inputs.isNotEmpty()) {
                        val inputTexture = getTexture(record.inputs[0])
                        inputTexture?.let { shader.setInputTexture(it) }
                            ?: Log.w(
                                TAG,
                                "Input texture is null for shader ${shader::class.simpleName}"
                            )
                    }
                }
            }

            RunShaderStep(
                stepIndex = index,
                runContext = runContext,
                shader = shader,
                targetBuffer = if (index != records.lastIndex) {
                    buffers.getOrPut(record.output) { bufferPool[BufferReference(this)] }
                } else {
                    targetBuffer
                }
            )
        }
    }
}

internal data class PipelineShaderNode(
    val shaderClassName: String,
    val shaderProperties: Map<String, Any>,
    val inputs: List<String>,
    val output: String,
) {
    fun createShader(): KraftShader {
        val shaderClass = Class.forName(shaderClassName).kotlin
        val constructor = shaderClass.primaryConstructor ?: shaderClass.constructors.first()
        return (constructor.callBy(emptyMap()) as KraftShader).apply {
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