---
sidebar_position: 1
---

# Built-in Shaders

Welcome to the Built-in Shaders section of KraftShade documentation. This section covers the pre-built shaders that come with KraftShade, ready to use in your applications.

## Overview

KraftShade provides a comprehensive collection of built-in shaders that cover a wide range of common image processing and visual effects. These shaders are optimized for performance and can be easily combined using the [Pipeline DSL](../pipeline-dsl) to create complex effects.

## Shader Categories

The built-in shaders are organized into several categories:

- [Base Shaders](./base-shaders): Fundamental shaders for basic operations
- [Alpha Transparency Effects](./alpha-transparency-effects): Shaders for handling transparency
- [Blending Modes](./blending-modes): Various ways to blend multiple images
- [Blur & Distortion](./blur-distortion): Effects for blurring and distorting images
- [Color Effects](./color-effects): Shaders for color manipulation
- [Convolution & Mask Processing](./convolution-mask-processing): Advanced image processing techniques
- [Edge Detection](./edge-detection): Algorithms for finding edges in images
- [Texture & Artistic Effects](./texture-artistic-effects): Creative and artistic shader effects

## Using Built-in Shaders

All built-in shaders follow a consistent API and can be used in your pipelines like this:

```kotlin
pipeline(targetBuffer) {
    serialSteps(
        inputTexture = inputBitmap.asTexture(),
        targetBuffer = targetBuffer
    ) {
        // Use a built-in shader to increase saturation by 50%
        step(SaturationKraftShader(saturation = 1.5f))
        
        // Chain with another shader to increase brightness by 20%
        step(BrightnessKraftShader(brightness = 0.2f))
    }
}
```

## Creating Custom Shaders

If the built-in shaders don't meet your needs, you can create your own custom shaders. See the Custom Shaders section for more information.

## Related Topics

- Core Components: Learn about the fundamental building blocks of KraftShade
- [Pipeline DSL](../pipeline-dsl): Understand how to combine shaders into pipelines