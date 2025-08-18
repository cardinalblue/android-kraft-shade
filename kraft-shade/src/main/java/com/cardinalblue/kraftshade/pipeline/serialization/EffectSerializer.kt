package com.cardinalblue.kraftshade.pipeline.serialization

import android.content.Context
import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.model.GlSizeF
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
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.math.BigDecimal
import java.util.Locale

/**
 * Converts JsonElement ➜ Kotlin Any with best-fit number types.
 * 
 * **Problem with original Gson implementation:**
 * By default, Gson deserializes all JSON numbers to `Double`, even when they represent integers.
 * This causes type mismatches when working with `Map<String, Any>` containing numeric values.
 * 
 * **Failed example with original Gson:**
 * ```kotlin
 * data class Item(val properties: Map<String, Any>)
 * 
 * val json = """{"properties": {"count": 1, "weight": 1.5}}"""
 * val gson = Gson()
 * val item = gson.fromJson(json, Item::class.java)
 * 
 * // With original Gson:
 * println(item.properties["count"] is Int)    // false - it's Double!
 * println(item.properties["count"] is Double) // true - 1.0 instead of 1
 * println(item.properties["weight"] is Double) // true - correct for decimal
 * ```
 * 
 * **Solution:**
 * This adapter intelligently converts JSON numbers to the most appropriate Kotlin type:
 * - Whole numbers within Int range → `Int`
 * - Whole numbers within Long range → `Long` 
 * - Larger whole numbers → `BigInteger`
 * - Decimal numbers → `Double`
 * 
 * **With NaturalNumberMapAdapter:**
 * ```kotlin
 * val gson = GsonBuilder()
 *     .registerTypeAdapter(object : TypeToken<Map<String, Any>>() {}.type, NaturalNumberMapAdapter())
 *     .create()
 * val item = gson.fromJson(json, Item::class.java)
 * 
 * println(item.properties["count"] is Int)    // true - correct!
 * println(item.properties["weight"] is Double) // true - correct!
 * ```
 */
class NaturalNumberMapAdapter : JsonDeserializer<Map<String, Any>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        ctx: JsonDeserializationContext
    ): Map<String, Any> = json.asJsonObject.entrySet().associate { (k, v) ->
        k to decode(v)
    }

    private fun decode(el: JsonElement): Any = when {
        el.isJsonArray     -> el.asJsonArray.map(::decode)
        el.isJsonObject    -> el.asJsonObject.entrySet().associate { (k, v) -> k to decode(v) }
        el.isJsonPrimitive -> decodePrimitive(el.asJsonPrimitive)
        else               -> error("Unexpected JSON element $el")
    }

    private fun decodePrimitive(p: JsonPrimitive): Any = when {
        p.isString  -> p.asString
        p.isBoolean -> p.asBoolean
        p.isNumber  -> narrowNumber(p.asBigDecimal)
        else        -> p.asString      // fallback
    }

    /** Decides whether the number can live in Int, Long or needs Double. */
    private fun narrowNumber(bd: BigDecimal): Any =
        if (bd.scale() == 0) {             // no fractional part
            try { bd.intValueExact() }     // within Int range?
            catch (_: ArithmeticException) {
                try { bd.longValueExact() }  // within Long range?
                catch (_: ArithmeticException) { bd.toBigInteger() }
            }
        } else bd.toDouble()
}

/**
 * Custom deserializer for PipelineShaderNode that properly handles shaderProperties
 * using NaturalNumberMapAdapter to ensure integers are deserialized as Int, not Double.
 */
internal class PipelineShaderNodeAdapter : JsonDeserializer<PipelineShaderNode> {
    private val mapAdapter = NaturalNumberMapAdapter()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        ctx: JsonDeserializationContext
    ): PipelineShaderNode {
        val jsonObject = json.asJsonObject

        val shaderClassName = jsonObject.get("shaderClassName").asString
        val inputs = jsonObject.get("inputs").asJsonArray.map { it.asString }
        val output = jsonObject.get("output").asString

        // Use NaturalNumberMapAdapter to properly deserialize shaderProperties
        val shaderProperties = mapAdapter.deserialize(
            jsonObject.get("shaderProperties"),
            object : TypeToken<Map<String, Any>>() {}.type,
            ctx
        )

        val serializableFields = if (jsonObject.has("serializableFields")) {
            mapAdapter.deserialize(
                jsonObject.get("serializableFields"),
                object : TypeToken<Map<String, Any>>() {}.type,
                ctx
            )
        } else emptyMap()

        return PipelineShaderNode(
            shaderClassName = shaderClassName,
            shaderProperties = shaderProperties,
            serializableFields = serializableFields,
            inputs = inputs,
            output = output
        )
    }
}

class EffectSerializer(private val context: Context, private val size: GlSize) {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(
            object : TypeToken<Map<String, Any>>() {}.type,
            NaturalNumberMapAdapter()
        )
        .create()

    suspend fun serialize(
        block: suspend GraphPipelineSetupScope.() -> Unit,
    ): String {
        val glEnv = GlEnv(context)
        val pipeline = Pipeline(
            glEnv = glEnv,
            bufferPool = TextureBufferPool(size),
            automaticBufferRecycle = false,
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
                    shaderProperties = shader.properties.toMap(), // clone it to avoid modification
                    serializableFields = shader.serializableFields.toMap(), // clone it to avoid modification
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
        val gson = GsonBuilder()
            .registerTypeAdapter(
                object : TypeToken<Map<String, Any>>() {}.type,
                NaturalNumberMapAdapter()
            )
            .registerTypeAdapter(
                PipelineShaderNode::class.java,
                PipelineShaderNodeAdapter()
            )
            .create()
        val records = gson.fromJson(json, Array<PipelineShaderNode>::class.java)

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

internal data class PipelineShaderNode(
    val shaderClassName: String,
    val shaderProperties: Map<String, Any>,
    val serializableFields: Map<String, Any>,
    val inputs: List<String>,
    val output: String,
) {
    fun createShader(): KraftShader {
        val shaderClass = Class.forName(shaderClassName)
        val constructor = shaderClass.getConstructor()
        return (constructor.newInstance() as KraftShader).apply {
            this@PipelineShaderNode.serializableFields.forEach {
                val value = it.value

                val method = this::class.java.methods.find { method ->
                    method.name == "set${it.key.replaceFirstChar { c -> c.uppercase(Locale.getDefault()) }}"
                }

                method?.invoke(this, when(method.parameterTypes.firstOrNull()) {
                    GlSizeF::class.java -> {
                        val value = value as List<*>
                        GlSizeF(
                            (value[0] as Double).toFloat(),
                            (value[1] as Double).toFloat()
                        )
                    }
                    Float::class.java -> (value as Number).toFloat()
                    Int::class.java -> (value as Number).toInt()
                    Long::class.java -> (value as Number).toLong()
                    Double::class.java -> (value as Number).toDouble()
                    String::class.java -> value as String
                    Boolean::class.java -> (value as Boolean)
                    else -> error("Unsupported type: ${it.value::class.java}")
                })
            }
            shaderProperties.mapNotNull { (key, value) ->
                when (value) {
                    is List<*> -> key to value.mapNotNull {
                        when (it) {
                            is Number -> it.toFloat()
                            else -> null
                        }
                    }.toFloatArray()
                    is Double -> key to value.toFloat()
                    else -> key to value
                }
            }.toMap().let {
                setUniforms(it)
            }
        }
    }
}
