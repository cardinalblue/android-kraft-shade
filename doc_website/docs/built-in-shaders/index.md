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

All built-in shaders follow a consistent API and can be used in your pipelines. Each shader is designed to perform a specific visual effect or transformation, and they can be combined in various ways to create complex effects.

For detailed usage examples and code snippets, please refer to the [Base Shaders](./base-shaders) section.

## Visual Examples

Each shader category includes visual examples showing the before and after results of applying different shaders. These examples help you understand the effect of each shader and how it might be used in your applications.

[IMAGE: Example shader effects - A grid showing various shader effects applied to the same source image]

## Creating Custom Shaders

If the built-in shaders don't meet your needs, you can create your own custom shaders by extending the base shader classes:

- Extend `TextureInputKraftShader` for shaders that take a single input texture
- Extend `TwoTextureInputKraftShader` for shaders that take two input textures
- Extend `Sample3x3KraftShader` for shaders that need to sample a 3x3 grid of pixels
- Extend `Convolution3x3KraftShader` for convolution-based shaders

Each shader category section includes information about creating custom shaders specific to that category.

## Shader Composition

One of KraftShade's strengths is the ability to compose multiple shaders to create complex effects. The Pipeline DSL makes this process straightforward, allowing you to chain shaders together in a readable and maintainable way.

For example, you might combine color adjustments like brightness and contrast with artistic effects like vignette to create a specific look for your images or videos.

## Performance Considerations

When working with shaders, keep these performance considerations in mind:

- Some shaders are more computationally expensive than others
- Chaining multiple shaders increases processing time
- Consider using intermediate buffers for complex multi-pass effects
- For real-time applications, test performance on target devices

## Related Topics

- [Core Components](../core-components): Learn about the fundamental building blocks of KraftShade
- [KraftShader](../core-components/shader-system/kraft-shader): Understand the base shader class
- [Pipeline DSL](../pipeline-dsl): Learn how to combine shaders into pipelines
- [Buffer Management](../core-components/pipeline-system/buffer-management): Understand how buffers are managed in multi-pass effects