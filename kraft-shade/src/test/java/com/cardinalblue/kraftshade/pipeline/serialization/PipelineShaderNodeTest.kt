package com.cardinalblue.kraftshade.pipeline.serialization

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.Test
import org.junit.Assert.*

/**
 * Test to reproduce the issue where PipelineShaderNode still gets Double for Int values from JSON
 */
class PipelineShaderNodeTest {

    @Test
    fun testPipelineShaderNodeDeserializationWithIntegers() {
        // Create JSON that represents a PipelineShaderNode with integer properties
        val json = """
        [
            {
                "shaderClassName": "com.example.TestShader",
                "shaderProperties": {
                    "intValue": 42,
                    "doubleValue": 3.14,
                    "boolValue": true,
                    "stringValue": "test"
                },
                "inputs": ["input1"],
                "output": "output1"
            }
        ]
        """

        // Use the same Gson configuration as SerializedEffect
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

        // Deserialize the JSON into Array<PipelineShaderNode>
        val records = gson.fromJson(json, Array<PipelineShaderNode>::class.java)

        assertNotNull("Records should not be null", records)
        assertEquals("Should have one record", 1, records.size)

        val record = records[0]
        assertEquals("Shader class name should match", "com.example.TestShader", record.shaderClassName)
        assertNotNull("Shader properties should not be null", record.shaderProperties)

        // Check the types of values in shaderProperties
        val properties = record.shaderProperties

        val intValue = properties["intValue"]
        val doubleValue = properties["doubleValue"]
        val boolValue = properties["boolValue"]
        val stringValue = properties["stringValue"]

        println("[DEBUG_LOG] intValue: $intValue (${intValue?.let { it::class.simpleName }})")
        println("[DEBUG_LOG] doubleValue: $doubleValue (${doubleValue?.let { it::class.simpleName }})")
        println("[DEBUG_LOG] boolValue: $boolValue (${boolValue?.let { it::class.simpleName }})")
        println("[DEBUG_LOG] stringValue: $stringValue (${stringValue?.let { it::class.simpleName }})")

        // With PipelineShaderNodeAdapter, integers should now be properly deserialized as Int
        assertNotNull("Int value should not be null", intValue)
        assertTrue("Int value should be Int type", intValue is Int)
        assertEquals("Int value should be 42", 42, intValue)

        assertNotNull("Double value should not be null", doubleValue)
        assertTrue("Double value should be Double", doubleValue is Double)
        assertEquals("Double value should be 3.14", 3.14, doubleValue as Double, 0.0)

        assertNotNull("Bool value should not be null", boolValue)
        assertTrue("Bool value should be Boolean", boolValue is Boolean)
        assertEquals("Bool value should be true", true, boolValue)

        assertNotNull("String value should not be null", stringValue)
        assertTrue("String value should be String", stringValue is String)
        assertEquals("String value should be 'test'", "test", stringValue)
    }
}
