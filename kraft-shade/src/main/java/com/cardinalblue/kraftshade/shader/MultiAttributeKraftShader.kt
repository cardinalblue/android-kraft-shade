package com.cardinalblue.kraftshade.shader

import android.opengl.GLES30
import com.cardinalblue.kraftshade.OpenGlUtils
import java.nio.FloatBuffer

/**
 * A vertex attribute definition for custom vertex shader attributes.
 * 
 * @property name The name of the attribute in the vertex shader
 * @property size The number of components per vertex attribute (1-4)
 * @property type The data type (e.g., GLES30.GL_FLOAT)
 * @property normalized Whether fixed-point data values should be normalized
 * @property stride The byte offset between consecutive vertex attributes
 * @property buffer The buffer containing the vertex attribute data
 */
data class VertexAttributeData(
    val name: String,
    val size: Int,
    val type: Int = GLES30.GL_FLOAT,
    val normalized: Boolean = false,
    val stride: Int = 0,
    val buffer: FloatBuffer
)

/**
 * Abstract base class for KraftShaders that support multiple custom vertex attributes.
 * This allows for complex vertex transformations beyond the standard position and texture coordinates.
 * 
 * Subclasses should:
 * 1. Override loadVertexShader() to provide a custom vertex shader with additional attributes
 * 2. Call setVertexAttributes() to provide the vertex attribute data
 * 3. Implement loadFragmentShader() as usual
 */
abstract class MultiAttributeKraftShader : TextureInputKraftShader() {
    
    private var vertexAttributes: List<VertexAttributeData> = emptyList()
    private val attributeLocations = mutableMapOf<String, Int>()
    
    /**
     * Set the vertex attributes for this shader.
     * This should be called before drawing, typically in a setup method or via runOnDraw.
     */
    protected fun setVertexAttributes(attributes: List<VertexAttributeData>) {
        vertexAttributes = attributes
    }
    
    /**
     * Update vertex attributes dynamically.
     * This is useful for animations where vertex data changes per frame.
     */
    fun updateVertexAttributes(attributes: List<VertexAttributeData>) {
        runOnDraw("updateVertexAttributes") {
            setVertexAttributes(attributes)
        }
    }
    
    override fun init(): Boolean {
        val wasInitialized = super.init()
        if (wasInitialized) {
            // Cache attribute locations after shader program is created
            cacheAttributeLocations()
        }
        return wasInitialized
    }
    
    private fun cacheAttributeLocations() {
        // Clear existing cache
        attributeLocations.clear()
        
        // Standard attributes are already handled by parent class
        // Cache any additional attributes that might be defined
        vertexAttributes.forEach { attribute ->
            if (attribute.name != "position" && attribute.name != "inputTextureCoordinate") {
                val location = GLES30.glGetAttribLocation(glProgId, attribute.name)
                if (location != -1) {
                    attributeLocations[attribute.name] = location
                    logger.d("Cached attribute location: ${attribute.name} -> $location")
                } else {
                    logger.w("Attribute '${attribute.name}' not found in shader program")
                }
            }
        }
    }
    
    override fun beforeActualDraw(isScreenCoordinate: Boolean) {
        super.beforeActualDraw(isScreenCoordinate)
        
        // Check if there are attributes from the Media3 context
        val contextAttributes = VertexAttributeContext.getCurrentAttributes()
        if (contextAttributes.isNotEmpty()) {
            // Use attributes from context if available (for Media3 integration)
            vertexAttributes = contextAttributes
        }
        
        // Enable and set up custom vertex attributes
        vertexAttributes.forEach { attribute ->
            when (attribute.name) {
                "position" -> {
                    // Position is handled in actualDraw by parent class
                }
                "inputTextureCoordinate" -> {
                    // Already handled by parent class
                }
                else -> {
                    // Handle custom attributes
                    val location = attributeLocations[attribute.name] 
                        ?: GLES30.glGetAttribLocation(glProgId, attribute.name).also {
                            if (it != -1) {
                                attributeLocations[attribute.name] = it
                            }
                        }
                    
                    if (location != -1) {
                        GLES30.glEnableVertexAttribArray(location)
                        GLES30.glVertexAttribPointer(
                            location,
                            attribute.size,
                            attribute.type,
                            attribute.normalized,
                            attribute.stride,
                            attribute.buffer
                        )
                    }
                }
            }
        }
    }
    
    override fun actualDraw(isScreenCoordinate: Boolean) {
        // Handle position attribute if provided in vertexAttributes
        val positionAttribute = vertexAttributes.find { it.name == "position" }
        
        if (positionAttribute != null) {
            // Use custom position data
            GLES30.glEnableVertexAttribArray(glAttribPosition)
            GLES30.glVertexAttribPointer(
                glAttribPosition,
                positionAttribute.size,
                positionAttribute.type,
                positionAttribute.normalized,
                positionAttribute.stride,
                positionAttribute.buffer
            )
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
            GLES30.glDisableVertexAttribArray(glAttribPosition)
        } else {
            // Use default position data from parent
            super.actualDraw(isScreenCoordinate)
        }
    }
    
    override fun afterActualDraw() {
        super.afterActualDraw()
        
        // Disable custom vertex attributes
        vertexAttributes.forEach { attribute ->
            if (attribute.name != "position" && attribute.name != "inputTextureCoordinate") {
                attributeLocations[attribute.name]?.let { location ->
                    if (location != -1) {
                        GLES30.glDisableVertexAttribArray(location)
                    }
                }
            }
        }
    }
    
    override suspend fun close(deleteRecursively: Boolean) {
        attributeLocations.clear()
        super.close(deleteRecursively)
    }
}

/**
 * A concrete implementation of MultiAttributeKraftShader that allows
 * vertex and fragment shaders to be provided as strings.
 */
class CustomMultiAttributeKraftShader(
    private val vertexShaderSource: String,
    private val fragmentShaderSource: String
) : MultiAttributeKraftShader() {
    
    override fun loadVertexShader(): String = vertexShaderSource
    
    override fun loadFragmentShader(): String = fragmentShaderSource
    
    /**
     * Convenience method to set vertex attributes and draw in one call.
     */
    fun drawWithAttributes(
        attributes: List<VertexAttributeData>,
        bufferSize: com.cardinalblue.kraftshade.model.GlSize,
        isScreenCoordinate: Boolean
    ) {
        setVertexAttributes(attributes)
        draw(bufferSize, isScreenCoordinate)
    }
}