---
sidebar_position: 6
---

# Best Practices

This guide provides recommendations and best practices for using the KraftShade Pipeline DSL effectively. Following these guidelines will help you create efficient, maintainable, and performant shader pipelines.

## Pipeline Design

### Choose the Right Pipeline Type

Select the appropriate pipeline type based on your needs:

- **Serial Pipeline**: Use for linear sequences of effects where each step builds on the previous one.
- **Graph Pipeline**: Use for complex effects that require multiple passes or non-linear processing.
- **Nested Pipeline**: Use for modular, reusable effect components or to organize complex pipelines.

```kotlin
// Simple linear processing - use Serial Pipeline
serialSteps(bitmap.asTexture(), windowSurface) {
    step(ContrastKraftShader())
    step(SaturationKraftShader())
}

// Complex multi-pass effect - use Graph Pipeline
graphSteps(targetBuffer = windowSurface) {
    // Create buffer references
    val (horizontalBlurBuffer, verticalBlurBuffer) = createBufferReferences(
        "horizontal-blur",
        "vertical-blur"
    )
    
    // Multiple passes with explicit buffer management
    stepWithInputTexture(shader, inputTexture, horizontalBlurBuffer)
    stepWithInputTexture(shader, horizontalBlurBuffer, verticalBlurBuffer)
    stepWithInputTexture(shader, verticalBlurBuffer, graphTargetBuffer)
}
```

### Keep Pipelines Modular

Break down complex effects into smaller, reusable components:

```kotlin
// Reusable blur component
fun GraphPipelineSetupScope.applyTwoPassBlur(
    inputTexture: TextureProvider,
    outputBuffer: GlBufferProvider,
    blurSize: Float
) {
    val (horizontalBlurBuffer) = createBufferReferences("horizontal-blur")
    
    // Horizontal pass
    stepWithInputTexture(
        shader = GaussianBlurKraftShader(),
        inputTexture = inputTexture,
        targetBuffer = horizontalBlurBuffer
    ) { shader ->
        shader.blurSize = blurSize
        shader.horizontal = true
    }
    
    // Vertical pass
    stepWithInputTexture(
        shader = GaussianBlurKraftShader(),
        inputTexture = horizontalBlurBuffer,
        targetBuffer = outputBuffer
    ) { shader ->
        shader.blurSize = blurSize
        shader.horizontal = false
    }
}

// Usage
graphSteps(targetBuffer = windowSurface) {
    applyTwoPassBlur(bitmap.asTexture(), graphTargetBuffer, 5f)
}
```

### Use Descriptive Names

Give your buffers and steps descriptive names to improve readability and debugging:

```kotlin
// Good: Descriptive buffer names
val (horizontalBlurBuffer, verticalBlurBuffer) = createBufferReferences(
    "horizontal-blur",
    "vertical-blur"
)

// Good: Descriptive step purpose
step("Apply vignette effect") { runContext ->
    // Implementation
}
```

## Performance Optimization

### Minimize Render Passes

Each step in a pipeline requires a render pass, which has overhead. Minimize the number of passes when possible:

```kotlin
// Less efficient: Two separate steps
step(ContrastKraftShader()) { shader ->
    shader.contrast = 1.5f
}
step(SaturationKraftShader()) { shader ->
    shader.saturation = 0.8f
}

// More efficient: Combined shader if possible
step(ContrastAndSaturationKraftShader()) { shader ->
    shader.contrast = 1.5f
    shader.saturation = 0.8f
}
```

### Reuse Buffer References

When possible, reuse buffer references to reduce memory usage:

```kotlin
// Create a limited number of buffer references
val (buffer1, buffer2) = createBufferReferences("buffer1", "buffer2")

// Reuse them across multiple steps
stepWithInputTexture(shader1, inputTexture, buffer1)
stepWithInputTexture(shader2, buffer1, buffer2)
stepWithInputTexture(shader3, buffer2, buffer1)
stepWithInputTexture(shader4, buffer1, graphTargetBuffer)
```

### Consider Buffer Sizes

Be mindful of buffer sizes, especially for intermediate results:

```kotlin
// For effects that don't need full resolution, you can use smaller buffers
val halfSizeBuffer = TextureBuffer(width / 2, height / 2)

// Apply effect at lower resolution
stepWithInputTexture(shader, inputTexture, halfSizeBuffer)

// Upscale when needed
stepWithInputTexture(upscaleShader, halfSizeBuffer, fullSizeBuffer)
```

### Use Mixture Steps Efficiently

The `stepWithMixture` function adds an extra render pass. Use it judiciously:

```kotlin
// Less efficient: Using stepWithMixture for small adjustments
stepWithMixture(
    shader = GrayScaleKraftShader(),
    mixturePercentInput = constInput(0.9f)
)

// More efficient: Use a shader with built-in intensity parameter if available
step(SaturationKraftShader()) { shader ->
    shader.saturation = 0.1f  // 0.0 would be grayscale, 0.1 is slight desaturation
}
```

## Code Organization

### Group Related Operations

Group related operations together using nested pipelines:

```kotlin
serialSteps(inputTexture, targetBuffer) {
    // Color adjustments group
    serialStep {
        step(ContrastKraftShader())
        step(SaturationKraftShader())
        step(BrightnessKraftShader())
    }
    
    // Artistic effects group
    serialStep {
        step(VignetteKraftShader())
        step(CrosshatchKraftShader())
    }
}
```

### Extract Reusable Components

Extract commonly used effect combinations into extension functions or use [PipelineModifier](./pipeline-modifier.md) for more complex reusable components:

```kotlin
// Extension function for a common effect combination
suspend fun SerialTextureInputPipelineScope.applyBasicColorAdjustments(
    contrast: Float = 1.0f,
    saturation: Float = 1.0f,
    brightness: Float = 0.0f
) {
    step(ContrastKraftShader()) { shader ->
        shader.contrast = contrast
    }
    
    step(SaturationKraftShader()) { shader ->
        shader.saturation = saturation
    }
    
    step(BrightnessKraftShader()) { shader ->
        shader.brightness = brightness
    }
}

// Usage
serialSteps(inputTexture, targetBuffer) {
    applyBasicColorAdjustments(contrast = 1.2f, saturation = 0.8f)
    // Other steps...
}
```

### Use Higher-Level Abstractions When Appropriate

For simple cases, use higher-level abstractions like `kraftBitmap`:

```kotlin
// Simple, concise approach for basic effects
val processedBitmap = kraftBitmap(context, inputBitmap) {
    serialPipeline {
        step(ContrastKraftShader(1.5f))
        step(SaturationKraftShader(0.8f))
    }
}
```

## Error Handling and Debugging

### Use Proper Scope Methods

Use the appropriate methods for each pipeline scope to avoid errors:

```kotlin
// In a serial pipeline, use serialStep instead of serialSteps
serialSteps(inputTexture, targetBuffer) {
    // Correct: Use serialStep for nested serial pipeline
    serialStep {
        // Steps here...
    }
    
    // Correct: Use graphStep for nested graph pipeline
    graphStep { inputTexture ->
        // Steps here...
    }
}
```

### Add Debug Information

Include debug information in your pipeline steps:

```kotlin
// Add purpose for debug
step("Apply vignette effect") { runContext ->
    // Implementation
}

// Use descriptive buffer names
val (blurBuffer) = createBufferReferences("gaussian-blur-result")
```

### Check Buffer Sizes

Be aware of buffer sizes, especially when working with different input sources:

```kotlin
// Add a check or log for buffer sizes
step("Check buffer sizes") { runContext ->
    val bufferSize = getPoolBufferSize()
    KraftLogger.d("Current buffer size: $bufferSize")
}
```

## Resource Management

### Release Resources When Done

Ensure resources are properly released when no longer needed:

```kotlin
// Use the use() extension function for automatic resource cleanup
GlEnv(context).use { env ->
    env.execute {
        // Pipeline operations...
    }
} // GlEnv is automatically released here
```

### Reuse Textures When Possible

For frequently used textures, consider reusing them:

```kotlin
// Load texture once
val commonTexture = LoadedTexture(bitmap)

// Use it multiple times
stepWithInputTexture(shader1, commonTexture, buffer1)
stepWithInputTexture(shader2, commonTexture, buffer2)
```

## Advanced Techniques

### Combine with Serialized Effects

You can combine DSL-defined pipelines with serialized effects:

```kotlin
// Apply a serialized effect within a pipeline
serialSteps(inputTexture, targetBuffer) {
    // Regular steps
    step(ContrastKraftShader())
    
    // Apply a serialized effect
    step(serializedEffect, targetBuffer)
    
    // More regular steps
    step(VignetteKraftShader())
}
```

### Use Custom Run Steps for Complex Operations

For operations that don't fit the shader model, use custom run steps:

```kotlin
step("Custom OpenGL operations") { runContext ->
    // Direct OpenGL operations
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId)
    // More OpenGL code...
}
```

### Leverage Input Parameters

Use the `Input<T>` type for dynamic shader parameters:

```kotlin
// Create an input parameter
val saturationInput = mutableInput(1.0f)

// Use it in a shader
step(SaturationKraftShader()) { shader ->
    shader.saturation = saturationInput.get()
}

// Later, update the input value
saturationInput.set(0.5f)
```

## Common Pitfalls to Avoid

### Avoid Excessive Nesting

While nesting is powerful, excessive nesting can make your code hard to follow:

```kotlin
// Avoid: Deep nesting
serialSteps(inputTexture, targetBuffer) {
    serialStep {
        graphStep { inputTexture ->
            serialSteps(inputTexture, graphTargetBuffer) {
                // Too deep!
            }
        }
    }
}

// Better: Flatten when possible
serialSteps(inputTexture, targetBuffer) {
    // First group of operations
    step(ContrastKraftShader())
    
    // Complex operation as a single graph step
    graphStep { inputTexture ->
        // Graph operations here
    }
    
    // Final operations
    step(VignetteKraftShader())
}
```

### Don't Ignore Buffer Lifecycle

Be careful about buffer references that cross pipeline boundaries:

```kotlin
// Problematic: Using a buffer reference outside its scope
val (buffer) = createBufferReferences("temp-buffer")
graphSteps(targetBuffer) {
    stepWithInputTexture(shader, inputTexture, buffer)
}
// buffer might be recycled here!
stepWithInputTexture(shader, buffer, outputBuffer) // Potential issue!

// Better: Keep buffer usage within its scope
graphSteps(targetBuffer) {
    val (buffer) = createBufferReferences("temp-buffer")
    stepWithInputTexture(shader, inputTexture, buffer)
    stepWithInputTexture(shader, buffer, graphTargetBuffer)
}
```

### Avoid Unnecessary Buffer Creation

Don't create new buffers when you can reuse existing ones:

```kotlin
// Avoid: Creating new buffers for each effect
serialSteps(inputTexture, targetBuffer) {
    for (i in 0 until 10) {
        val (tempBuffer) = createBufferReferences("effect-$i") // Inefficient!
        // Use tempBuffer...
    }
}

// Better: Reuse buffers
serialSteps(inputTexture, targetBuffer) {
    // Serial pipeline automatically uses ping-pong buffers
    for (i in 0 until 10) {
        step(EffectKraftShader()) { shader ->
            shader.intensity = i / 10f
        }
    }
}
```

## Conclusion

By following these best practices, you can create efficient, maintainable, and performant shader pipelines with the KraftShade Pipeline DSL. Remember that the right approach depends on your specific use case, so adapt these guidelines as needed.

For more information, refer to the documentation on:
- [Introduction to the DSL](./introduction.md)
- [Serial Pipeline](./serial-pipeline.md)
- [Graph Pipeline](./graph-pipeline.md)
- [Nested Pipeline](./nested-pipeline.md)
