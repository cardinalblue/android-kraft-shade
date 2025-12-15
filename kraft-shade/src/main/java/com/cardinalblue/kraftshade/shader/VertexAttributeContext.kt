package com.cardinalblue.kraftshade.shader

/**
 * A context object to pass vertex attributes from external sources (like Media3 effects) 
 * to shaders in the pipeline. This uses a ThreadLocal to ensure thread safety when 
 * multiple effects might be running.
 * 
 * This is primarily used for integration with Media3's video processing pipeline,
 * where vertex attributes need to be provided per frame during video export.
 */
object VertexAttributeContext {
    private val threadLocalAttributes = ThreadLocal<List<VertexAttributeData>>()
    
    /**
     * Set the current vertex attributes for the current thread.
     * This should be called before drawing operations that use MultiAttributeKraftShader.
     */
    fun setCurrentAttributes(attributes: List<VertexAttributeData>) {
        threadLocalAttributes.set(attributes)
    }
    
    /**
     * Get the current vertex attributes for the current thread.
     * Returns an empty list if no attributes have been set.
     */
    fun getCurrentAttributes(): List<VertexAttributeData> {
        return threadLocalAttributes.get() ?: emptyList()
    }
    
    /**
     * Clear the current vertex attributes for the current thread.
     * This should be called after drawing operations to prevent memory leaks.
     */
    fun clearCurrentAttributes() {
        threadLocalAttributes.remove()
    }
    
    /**
     * Check if there are any vertex attributes set for the current thread.
     */
    fun hasAttributes(): Boolean {
        return threadLocalAttributes.get() != null
    }
}