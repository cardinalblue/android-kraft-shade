---
sidebar_position: 1
---

# Introduction to the DSL

The KraftShade Pipeline DSL (Domain Specific Language) provides a concise and expressive way to define image processing pipelines. It allows you to chain multiple shader effects together in various configurations to create complex visual effects.

## What is a Pipeline DSL?

A Pipeline DSL is a specialized syntax designed to make it easier to create and configure image processing pipelines. In KraftShade, the Pipeline DSL provides a structured way to:

- Define the sequence of shader operations
- Configure shader parameters
- Manage texture inputs and outputs
- Handle buffer allocation and recycling
- Create complex multi-pass rendering graphs

The DSL abstracts away many of the low-level details of OpenGL programming, allowing you to focus on the creative aspects of shader development.

## Core Concepts

### Pipeline

A pipeline is a sequence of rendering steps that process an input image to produce an output image. Each step in the pipeline typically applies a specific shader effect.

```kotlin
pipeline(windowSurface) {
    // Pipeline steps defined here
}
```

### Steps

Steps are individual operations within a pipeline. Each step typically applies a shader to an input texture and renders the result to an output buffer.

```kotlin
step(SaturationKraftShader()) { shader ->
    shader.saturation = 0.5f
}
```

### Textures and Buffers

Textures represent image data in GPU memory, while buffers are targets for rendering operations. The Pipeline DSL manages the allocation and recycling of these resources automatically.

```kotlin
// Convert a bitmap to a texture
val inputTexture = bitmap.asTexture()

// Create a buffer reference
val (buffer1, buffer2) = createBufferReferences("ping", "pong")
```

## Pipeline Types

KraftShade supports three main types of pipelines:

1. **Serial Pipeline**: A linear sequence of shader operations where each step's output becomes the input for the next step.

2. **Graph Pipeline**: A more complex structure where steps can have multiple inputs and outputs, allowing for non-linear processing flows.

3. **Nested Pipeline**: A combination of serial and graph pipelines, allowing for modular and reusable pipeline components.

## Basic Usage

Here's a simple example of using the Pipeline DSL to apply a crosshatch effect to an image:

```kotlin
pipeline(windowSurface) {
    serialSteps(bitmap.asTexture(), windowSurface) {
        step(CrosshatchKraftShader()) { shader ->
            shader.crossHatchSpacing = 0.03f
            shader.lineWidth = 0.003f
        }
    }
}
```

For more complex image processing, you can use the higher-level `kraftBitmap` DSL:

```kotlin
val processedBitmap = kraftBitmap(context, inputBitmap) {
    serialPipeline {
        step(ContrastKraftShader(4f))
        step(BrightnessKraftShader(-0.5f))
    }
}
```

## Next Steps

In the following sections, we'll explore each type of pipeline in more detail:

- [Serial Pipeline](./serial-pipeline.md): Learn how to create linear processing chains
- [Graph Pipeline](./graph-pipeline.md): Discover how to build complex multi-pass rendering graphs
- [Nested Pipeline](./nested-pipeline.md): See how to combine serial and graph pipelines
- [Best Practices](./best-practices.md): Tips for effective pipeline design
