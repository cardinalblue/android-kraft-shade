---
sidebar_position: 4
---

# Nested Pipeline

Nested Pipelines in KraftShade allow you to combine the features of Serial and Graph Pipelines, creating modular and reusable pipeline components. This approach enables you to build complex effects by composing simpler ones, improving code organization and reusability.

## Understanding Nested Pipelines

Nested pipelines are ideal for:
- Building complex effects from simpler components
- Creating reusable effect modules
- Organizing complex rendering logic into manageable pieces
- Combining the linear flow of serial pipelines with the flexibility of graph pipelines

The key characteristic of nested pipelines is their hierarchical structure - pipelines can contain other pipelines, allowing for modular composition of effects.

## Types of Nested Pipelines

KraftShade supports two main types of nested pipelines:

### 1. Serial Steps within Graph Pipeline

You can include a serial pipeline within a graph pipeline using the `serialSteps` function:

```kotlin
graphSteps(targetBuffer = windowSurface) {
    // Create a buffer for the serial pipeline result
    val (serialResult) = createBufferReferences("serial-result")
    
    // Some graph steps...
    
    // Nested serial pipeline
    serialSteps(
        inputTexture = sourceTexture,
        targetBuffer = serialResult
    ) {
        // Serial steps defined here
        step(ContrastKraftShader()) { shader ->
            shader.contrast = 1.5f
        }
        
        step(SaturationKraftShader()) { shader ->
            shader.saturation = 0.8f
        }
    }
    
    // Continue with more graph steps using serialResult...
}
```

This allows you to create a linear sequence of operations within your graph pipeline.

### 2. Graph Step within Serial Pipeline

You can include a graph pipeline within a serial pipeline using the `graphStep` function:

```kotlin
serialSteps(inputTexture = bitmap.asTexture(), targetBuffer = windowSurface) {
    // Some serial steps...
    
    // Nested graph step
    graphStep { inputTexture ->
        // Graph operations defined here, using inputTexture as source
        // and graphTargetBuffer as the output
        
        val (blurBuffer) = createBufferReferences("blur-buffer")
        
        stepWithInputTexture(
            shader = BlurKraftShader(),
            inputTexture = inputTexture,
            targetBuffer = blurBuffer
        )
        
        stepWithInputTexture(
            shader = VignetteKraftShader(),
            inputTexture = blurBuffer,
            targetBuffer = graphTargetBuffer
        )
    }
    
    // Continue with more serial steps...
}
```

This allows you to create a complex, non-linear processing graph within your serial pipeline.

### 3. Serial Step within Serial Pipeline

You can also nest a serial pipeline within another serial pipeline using the `serialStep` function:

```kotlin
serialSteps(inputTexture = bitmap.asTexture(), targetBuffer = windowSurface) {
    // Some serial steps...
    
    // Nested serial step
    serialStep {
        // Another serial pipeline using serialStartTexture as input
        // and serialTargetBuffer as output
        step(ContrastKraftShader()) { shader ->
            shader.contrast = 1.5f
        }
        
        step(SaturationKraftShader()) { shader ->
            shader.saturation = 0.8f
        }
    }
    
    // Continue with more serial steps...
}
```

This is useful for organizing related effects into logical groups.

## Implementation Details

The nesting capabilities are implemented through the following methods in the `PipelineDSL.kt` file:

### In `BasePipelineSetupScope`

```kotlin
@KraftShadeDsl
open suspend fun graphSteps(
    targetBuffer: GlBufferProvider,
    block: suspend GraphPipelineSetupScope.() -> Unit
) {
    val scope = GraphPipelineSetupScope(env, pipeline, targetBuffer)
    block(scope)
}

@KraftShadeDsl
open suspend fun serialSteps(
    inputTexture: TextureProvider,
    targetBuffer: GlBufferProvider,
    block: suspend SerialTextureInputPipelineScope.() -> Unit
) {
    val scope = SerialTextureInputPipelineScope(
        currentStepIndex = pipeline.stepCount,
        env = env,
        pipeline = pipeline,
        serialStartTexture = inputTexture,
        serialTargetBuffer = targetBuffer
    )

    // we have to do it in two steps, because before the block is finished. We don't know which
    // of the step is the last step that we have to draw to the target buffer.
    scope.block()
    scope.applyToPipeline()
}
```

### In `SerialTextureInputPipelineScope`

```kotlin
@KraftShadeDsl
suspend fun graphStep(
    block: suspend GraphPipelineSetupScope.(inputTexture: TextureProvider) -> Unit
) {
    steps.add(InternalGraphStep(block))
}

@KraftShadeDsl
suspend fun serialStep(block: suspend SerialTextureInputPipelineScope.() -> Unit) {
    steps.add(InternalSerialStep(block))
}
```

## Real-World Example: Complex Photo Filter

Here's a complete example that implements a complex photo filter using nested pipelines:

```kotlin
pipeline(windowSurface) {
    serialSteps(
        inputTexture = bitmap.asTexture(),
        targetBuffer = windowSurface
    ) {
        // Apply basic color adjustments
        step(ContrastKraftShader()) { shader ->
            shader.contrast = 1.2f
        }
        
        step(SaturationKraftShader()) { shader ->
            shader.saturation = 0.9f
        }
        
        // Apply a complex blur effect using a nested graph step
        graphStep { inputTexture ->
            // Create buffer references for the blur passes
            val (horizontalBlurBuffer, verticalBlurBuffer) = createBufferReferences(
                "horizontal-blur",
                "vertical-blur"
            )
            
            // First pass: horizontal blur
            stepWithInputTexture(
                shader = GaussianBlurKraftShader(),
                inputTexture = inputTexture,
                targetBuffer = horizontalBlurBuffer
            ) { shader ->
                shader.blurSize = 5f
                shader.horizontal = true
            }
            
            // Second pass: vertical blur
            stepWithInputTexture(
                shader = GaussianBlurKraftShader(),
                inputTexture = horizontalBlurBuffer,
                targetBuffer = verticalBlurBuffer
            ) { shader ->
                shader.blurSize = 5f
                shader.horizontal = false
            }
            
            // Blend the blurred result with the original
            stepWithInputTexture(
                shader = AlphaBlendKraftShader(),
                inputTexture = inputTexture,
                targetBuffer = graphTargetBuffer
            ) { shader ->
                shader.setSecondInputTexture(verticalBlurBuffer)
                shader.mixturePercent = 0.7f
            }
        }
        
        // Apply final artistic effects
        serialStep {
            step(CrosshatchKraftShader()) { shader ->
                shader.crossHatchSpacing = 0.03f
                shader.lineWidth = 0.002f
            }
            
            stepWithMixture(
                shader = CrosshatchKraftShader(),
                mixturePercentInput = constInput(0.5f)
            ) { shader ->
                shader.crossHatchSpacing = 0.05f
                shader.lineWidth = 0.003f
            }
        }
    }
}
```

This example demonstrates:
1. A top-level serial pipeline for the overall effect
2. A nested graph step for a complex blur effect
3. A nested serial step for artistic effects
4. Reuse of intermediate results within the nested pipelines

## Best Practices for Nested Pipelines

When working with nested pipelines, consider these best practices:

1. **Modular Design**: Use nested pipelines to create modular, reusable effect components.

2. **Logical Grouping**: Group related operations into nested pipelines to improve code organization.

3. **Appropriate Nesting**: Choose the right type of nesting based on your needs:
   - Use `serialStep` for linear sequences of operations
   - Use `graphStep` for complex, non-linear operations

4. **Buffer Management**: Be mindful of buffer usage in nested pipelines, especially in complex graphs.

5. **Performance Considerations**: Nested pipelines can introduce additional overhead, so use them judiciously.

## Limitations and Considerations

When using nested pipelines, be aware of these limitations:

1. **Error Handling**: The `serialSteps` method in a `SerialTextureInputPipelineScope` is overridden to throw an error, directing you to use `serialStep` instead:

```kotlin
@DangerousKraftShadeApi
@KraftShadeDsl
override suspend fun serialSteps(
    inputTexture: TextureProvider,
    targetBuffer: GlBufferProvider,
    block: suspend SerialTextureInputPipelineScope.() -> Unit
) {
    error("please use graphStep instead of serialSteps in serial scope")
}
```

2. **Buffer Lifecycle**: Buffers created in nested pipelines are managed by the parent pipeline, so be careful about buffer references that cross pipeline boundaries.

3. **Complexity**: While nested pipelines offer great flexibility, they can also make your code more complex and harder to debug. Use them when the benefits of modularity outweigh the added complexity.

## Next Steps

Now that you understand how to create and use nested pipelines, you might want to explore:

- [Best Practices](./best-practices.md) for pipeline design
- Advanced techniques for optimizing pipeline performance
- Creating reusable effect modules using nested pipelines
