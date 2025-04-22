package com.cardinalblue.kraftshade.demo.shader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.pipeline.input.ImmutableInput
import com.cardinalblue.kraftshade.shader.KraftShaderFactory
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer
import com.cardinalblue.kraftshade.shader.builtin.Es3DemoKraftShader
import com.cardinalblue.kraftshade.createShader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Example class demonstrating how to use OpenGL ES 3.0 features with KraftShade.
 * This class shows how to:
 * 1. Create a GlEnv that supports OpenGL ES 3.0 with fallback to 2.0
 * 2. Use the KraftShaderFactory to create shaders with the appropriate OpenGL ES version
 * 3. Apply the Es3DemoKraftShader to an image
 */
class Es3DemoShaderExample(
    private val context: Context
) {
    /**
     * Applies the Es3DemoKraftShader to the input bitmap and returns the result.
     * 
     * @param inputBitmap The bitmap to apply the effect to
     * @param intensity The intensity of the effect, from 0.0 to 1.0
     * @return The processed bitmap
     */
    suspend fun applyEs3DemoEffect(
        inputBitmap: Bitmap,
        intensity: Float = 0.5f
    ): Bitmap = withContext(Dispatchers.Default) {
            // Create a GlEnv instance that uses OpenGL ES 3.0
            val glEnv = GlEnv(context)
        
        try {
            // Log the OpenGL ES version that was successfully initialized
            println("Using OpenGL ES ${glEnv.glVersion}.0")
            
            // Create a texture buffer for the input bitmap
            val inputSize = GlSize(inputBitmap.width, inputBitmap.height)
            val inputBuffer = TextureBuffer(inputSize, glEnv)
            
            // Create a texture buffer for the output
            val outputBuffer = TextureBuffer(inputSize, glEnv)
            
            // Create the Es3DemoKraftShader using the KraftShaderFactory
            // This will automatically use the appropriate OpenGL ES version
            val demoShader = glEnv.createShader { version ->
                Es3DemoKraftShader(version).apply {
                    this.intensity = intensity
                }
            }
            
            // Create a pipeline to process the image
            val pipeline = Pipeline.create {
                // Load the input bitmap into the input buffer
                inputBuffer.loadBitmap(inputBitmap)
                
                // Set up the shader's input texture
                demoShader.inputTexture.input = ImmutableInput(inputBuffer)
                
                // Draw the shader to the output buffer
                demoShader.drawTo(outputBuffer)
                
                // Get the result as a bitmap
                val resultBitmap = outputBuffer.asBitmap()
                
                // Clean up resources
                demoShader.destroy()
                inputBuffer.release()
                outputBuffer.release()
                
                // Return the result
                resultBitmap
            }
            
            // Execute the pipeline
            glEnv.execute {
                pipeline.execute()
            }
        } finally {
            // Make sure to terminate the GlEnv when done
            glEnv.terminate()
        }
    }
    
    /**
     * Loads a bitmap from the assets folder.
     * 
     * @param assetPath The path to the asset
     * @return The loaded bitmap or null if loading failed
     */
    fun loadBitmapFromAssets(assetPath: String): Bitmap? {
        return try {
            context.assets.open(assetPath).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    companion object {
        /**
         * Creates a simple usage example that demonstrates how to use the Es3DemoKraftShader.
         * 
         * @param context The Android context
         * @param inputBitmap The bitmap to process
         * @param intensity The intensity of the effect
         * @return The processed bitmap
         */
        suspend fun createSimpleExample(
            context: Context,
            inputBitmap: Bitmap,
            intensity: Float = 0.5f
        ): Bitmap {
            val example = Es3DemoShaderExample(context)
            return example.applyEs3DemoEffect(inputBitmap, intensity)
        }
    }
}
