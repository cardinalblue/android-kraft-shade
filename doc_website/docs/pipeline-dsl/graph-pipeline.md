---
sidebar_position: 3
---

# Graph Pipeline

A Graph Pipeline in KraftShade provides a more flexible approach to shader operations compared to a Serial Pipeline. Instead of a linear sequence, a Graph Pipeline allows you to create complex, non-linear processing flows where you have precise control over the input and output of each step.

## Understanding Graph Pipelines

Graph pipelines are ideal for:
- Complex multi-pass rendering techniques
- Effects that require intermediate results
- Scenarios where you need precise control over buffer management
- Advanced compositing operations

The key characteristic of a graph pipeline is its non-linear nature - you explicitly define where each step gets its input from and where it renders its output to.

## Creating a Graph Pipeline

You can create a graph pipeline using the `graphSteps` function within a pipeline context:

```kotlin
pipeline(windowSurface) {
    graphSteps(targetBuffer = windowSurface) {
        // Graph steps defined here
    }
}
```

Parameters:
- `targetBuffer`: The final output buffer where the result will be rendered
- A lambda block where you define the steps of the graph pipeline

## Adding Steps to a Graph Pipeline

Within a graph pipeline, you have several ways to add steps:

### Basic Step

The most basic way to add a step is with the `step` function:

```kotlin
graphSteps(targetBuffer = windowSurface) {
    step(
        shader = MyCustomShader(),
        targetBuffer = buffer1,
        setupAction = { shader ->
            // Configure shader parameters
        }
    )
}
```

Parameters:
- `shader`: The shader to use for this step
- `targetBuffer`: The buffer to render the output to
- `setupAction`: An optional lambda to configure the shader

### Step with Input Texture

For shaders that require an input texture, you can use `stepWithInputTexture`:

```kotlin
graphSteps(targetBuffer = windowSurface) {
    stepWithInputTexture(
        shader = BlurKraftShader(),
        inputTexture = sourceTexture,
        targetBuffer = buffer1
    )
}
```

This method has two variants:
1. For constant textures (loaded from a bitmap)
2. For texture providers (like buffer references)

## Buffer Management in Graph Pipelines

One of the key features of graph pipelines is explicit buffer management. You can create buffer references and use them as inputs and outputs for different steps:

```kotlin
graphSteps(targetBuffer = windowSurface) {
    // Create buffer references
    val (horizontalBlurBuffer, verticalBlurBuffer) = createBufferReferences(
        "horizontal-blur",
        "vertical-blur"
    )
    
    // First pass: horizontal blur
    stepWithInputTexture(
        shader = BlurKraftShader(),
        inputTexture = sourceTexture,
        targetBuffer = horizontalBlurBuffer
    ) { shader ->
        shader.direction = BlurDirection.HORIZONTAL
    }
    
    // Second pass: vertical blur using the result of the first pass
    stepWithInputTexture(
        shader = BlurKraftShader(),
        inputTexture = horizontalBlurBuffer,
        targetBuffer = verticalBlurBuffer
    ) { shader ->
        shader.direction = BlurDirection.VERTICAL
    }
    
    // Final pass: blend the blurred result with the original
    stepWithInputTexture(
        shader = AlphaBlendKraftShader(),
        inputTexture = sourceTexture,
        targetBuffer = graphTargetBuffer
    ) { shader ->
        shader.setSecondInputTexture(verticalBlurBuffer)
        shader.mixturePercent = 0.8f
    }
}
```

This example demonstrates a two-pass blur effect followed by a blend operation, which would be difficult to express in a serial pipeline.

## Custom Run Steps

For operations that don't fit the shader model, you can use a custom run step:

```kotlin
graphSteps(targetBuffer = windowSurface) {
    step("Custom operation") { runContext ->
        // Custom OpenGL operations here
    }
}
```

## Differences from Serial Pipeline

The main differences between graph and serial pipelines are:

1. **Explicit Buffer Management**: In a graph pipeline, you explicitly specify the target buffer for each step, while in a serial pipeline, the buffers are managed automatically using a ping-pong mechanism.

2. **Non-Linear Flow**: Graph pipelines allow for non-linear processing flows, where steps can use the outputs of any previous step as input, not just the immediately preceding step.

3. **Multiple Inputs**: Graph pipelines make it easier to work with shaders that require multiple input textures, such as blend operations.

4. **Reuse of Intermediate Results**: You can reuse the output of a step multiple times in different parts of the pipeline.

## Implementation Details

The `GraphPipelineSetupScope` class provides the implementation for graph pipelines. It extends `BasePipelineSetupScope` and adds methods specific to graph pipelines:

```kotlin
@KraftShadeDsl
class GraphPipelineSetupScope(
    glEnv: GlEnv,
    pipeline: Pipeline,
    val graphTargetBuffer: GlBufferProvider,
) : BasePipelineSetupScope(glEnv, pipeline) {
    // Methods for adding steps to the graph pipeline
}
```

The `graphTargetBuffer` property represents the final output buffer for the graph pipeline.

## Real-World Example: Two-Pass Blur

Here's a complete example that implements a two-pass Gaussian blur effect using a graph pipeline:

```kotlin
pipeline(windowSurface) {
    graphSteps(targetBuffer = windowSurface) {
        // Create buffer references for intermediate results
        val (horizontalBlurBuffer, verticalBlurBuffer) = createBufferReferences(
            "horizontal-blur",
            "vertical-blur"
        )
        
        // First pass: horizontal blur
        stepWithInputTexture(
            shader = GaussianBlurKraftShader(),
            inputTexture = bitmap.asTexture(),
            targetBuffer = horizontalBlurBuffer
        ) { shader ->
            shader.blurSize = 10f
            shader.horizontal = true
        }
        
        // Second pass: vertical blur
        stepWithInputTexture(
            shader = GaussianBlurKraftShader(),
            inputTexture = horizontalBlurBuffer,
            targetBuffer = verticalBlurBuffer
        ) { shader ->
            shader.blurSize = 10f
            shader.horizontal = false
        }
        
        // Final pass: apply vignette to the blurred result
        stepWithInputTexture(
            shader = VignetteKraftShader(),
            inputTexture = verticalBlurBuffer,
            targetBuffer = graphTargetBuffer
        ) { shader ->
            shader.vignetteStart = 0.8f
            shader.vignetteEnd = 0.3f
        }
    }
}
```

This creates a processing graph that:
1. Applies a horizontal Gaussian blur to the input image
2. Applies a vertical Gaussian blur to the result of step 1
3. Applies a vignette effect to the fully blurred image
4. Renders the final result to the window surface

## When to Use Graph Pipelines

Consider using graph pipelines when:

- You need to implement multi-pass effects like bloom, depth of field, or shadow mapping
- You want to reuse intermediate results in different parts of your pipeline
- You need precise control over buffer allocation and management
- Your effect requires non-linear processing flows

For simpler linear effects, a [Serial Pipeline](./serial-pipeline.md) might be more appropriate. For complex effects that combine both approaches, consider using a [Nested Pipeline](./nested-pipeline.md).
