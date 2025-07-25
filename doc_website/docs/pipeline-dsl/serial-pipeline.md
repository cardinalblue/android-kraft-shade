---
sidebar_position: 2
---

# Serial Pipeline

A Serial Pipeline in KraftShade is a linear sequence of shader operations where the output of each step becomes the input for the next step. This is the most common and straightforward way to chain multiple effects together.

## Understanding Serial Pipelines

Serial pipelines are ideal for:
- Applying multiple effects in sequence
- Creating a processing chain where each step builds on the previous one
- Simple image processing workflows

The key characteristic of a serial pipeline is its linear nature - data flows from one step to the next in a predefined order.

## Creating a Serial Pipeline

KraftShade provides two main ways to create serial pipelines:

### 1. Using `serialSteps`

The `serialSteps` function is the primary way to create a serial pipeline within a larger pipeline context:

```kotlin
pipeline(windowSurface) {
    serialSteps(
        inputTexture = bitmap.asTexture(),
        targetBuffer = windowSurface
    ) {
        // Serial steps defined here
    }
}
```

Parameters:
- `inputTexture`: The initial texture input for the first step
- `targetBuffer`: The final output buffer where the result will be rendered
- A lambda block where you define the steps of the serial pipeline

### 2. Using `kraftBitmap` with `serialPipeline`

For simpler use cases, especially when working with bitmaps, you can use the higher-level `kraftBitmap` DSL with `serialPipeline`:

```kotlin
val processedBitmap = kraftBitmap(context, inputBitmap) {
    serialPipeline {
        step(ContrastKraftShader(4f))
        step(BrightnessKraftShader(-0.5f))
    }
}
```

This approach is more concise and focuses on the shaders rather than the pipeline mechanics.

## Adding Steps to a Serial Pipeline

Within a serial pipeline, you can add steps using the `step` function:

```kotlin
serialSteps(bitmap.asTexture(), windowSurface) {
    step(CrosshatchKraftShader()) { shader ->
        shader.crossHatchSpacing = 0.03f
        shader.lineWidth = 0.003f
    }
}
```

Each `step` takes a shader instance and an optional setup action that configures the shader parameters.

## Ping-Pong Buffer Mechanism

Behind the scenes, serial pipelines use a ping-pong buffer mechanism to efficiently chain operations:

1. The first step renders to "buffer1" (ping)
2. The second step uses "buffer1" as input and renders to "buffer2" (pong)
3. The third step uses "buffer2" as input and renders to "buffer1" (ping)
4. And so on...

This mechanism is handled automatically by the `SerialTextureInputPipelineScope` class, which creates and manages the necessary buffer references.

```kotlin
// From the PipelineDSL.kt implementation
var drawToBuffer1 = true
val (buffer1, buffer2) = BufferReferenceCreator(
    pipeline,
    "$bufferReferencePrefix-ping",
    "$bufferReferencePrefix-pong",
)
```

## Advanced Serial Pipeline Features

### Mixture Steps

You can mix the output of a shader with the original input using `stepWithMixture`:

```kotlin
serialSteps(bitmap.asTexture(), windowSurface) {
    stepWithMixture(
        shader = GrayScaleKraftShader(),
        mixturePercentInput = constInput(0.5f)
    )
}
```

This creates a blend between the shader output and the original input, controlled by the `mixturePercentInput` parameter (0.0 to 1.0).

### Custom Run Steps

For more complex operations that don't fit the shader model, you can use a custom run step. It's guaranteed the running thread will be the one that has the GL context. You can change blending setup or do anything with or without using OpenGL API.
```kotlin
serialSteps(bitmap.asTexture(), windowSurface) {
    step("Custom operation") { runContext ->
        // Custom OpenGL operations here
    }
}
```

## Real-World Example

Here's a complete example that applies multiple effects in a serial pipeline:

```kotlin
pipeline(windowSurface) {
    serialSteps(
        inputTexture = bitmap.asTexture(),
        targetBuffer = windowSurface
    ) {
        // Apply a contrast adjustment
        step(ContrastKraftShader()) { shader ->
            shader.contrast = 1.5f
        }
        
        // Apply a saturation adjustment
        step(SaturationKraftShader()) { shader ->
            shader.saturation = 0.8f
        }
        
        // Apply a vignette effect
        step(VignetteKraftShader()) { shader ->
            shader.vignetteStart = 0.8f
            shader.vignetteEnd = 0.3f
        }
    }
}
```

This creates a processing chain that:
1. Adjusts the contrast of the input image
2. Reduces the saturation of the contrast-adjusted image
3. Applies a vignette effect to the contrast-adjusted, desaturated image
4. Renders the final result to the window surface

## Performance Considerations

Serial pipelines are efficient for most use cases, but keep in mind:

- Each step requires a render pass, which has some overhead
- The ping-pong buffer mechanism requires additional memory
- Very long chains of effects may impact performance

For complex effects that require multiple passes, consider using a [Graph Pipeline](./graph-pipeline.md) or [Nested Pipeline](./nested-pipeline.md) for more flexibility.
