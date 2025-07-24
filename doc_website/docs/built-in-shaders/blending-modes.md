---
sidebar_position: 7
---

# Blending Modes

Blending modes determine how two images or layers are combined together in KraftShade. These shaders enable sophisticated compositing techniques essential for creating complex visual effects.

## Overview

Blending modes are mathematical operations that determine how pixels from two different images interact when overlaid. KraftShade provides a variety of blending modes inspired by those found in professional image editing software, allowing for creative combinations of textures and effects.

All blend shaders in KraftShade extend from the `TwoTextureInputKraftShader` base class, which handles the input of two textures. Most blend shaders also include an `intensity` parameter that controls the strength of the blend effect.

## Available Blending Mode Shaders

### MixBlendKraftShader

The base class for blend shaders that mix two textures based on a percentage. This is an abstract class that other blend shaders can extend.

**Parameters:**
- `mixturePercent`: Float value between 0.0 (only first texture) and 1.0 (only second texture)

### AlphaBlendKraftShader

Blends two textures based on the alpha channel of the second texture and a mixture percentage.

**Parameters:**
- `mixturePercent`: Float value between 0.0 (only first texture) and 1.0 (only second texture)
- `intensity`: Controls the strength of the blend effect

**Example Use Cases:**
- Smooth transitions between images
- Creating composite images with varying levels of transparency

[IMAGE: AlphaBlendKraftShader - Left side shows first texture, middle shows second texture, right side shows blended result]

### NormalBlendKraftShader

The standard blend mode that simply places the second texture over the first, respecting the alpha channel.

**Parameters:**
- `intensity`: Controls the strength of the blend effect

**Example Use Cases:**
- Basic layering of images
- Standard compositing operations

[IMAGE: NormalBlendKraftShader - Left side shows first texture, middle shows second texture, right side shows blended result]

### MultiplyBlendKraftShader

Multiplies the color values of the two textures, resulting in a darker image. White pixels have no effect, while black pixels make the result black.

**Parameters:**
- `intensity`: Controls the strength of the blend effect

**Example Use Cases:**
- Creating shadows and darkening effects
- Simulating the effect of placing two transparent slides on top of each other

[IMAGE: MultiplyBlendKraftShader - Left side shows first texture, middle shows second texture, right side shows multiplied result]

### ScreenBlendKraftShader

The opposite of multiply - it brightens the image. Black pixels have no effect, while white pixels make the result white.

**Parameters:**
- `intensity`: Controls the strength of the blend effect

**Example Use Cases:**
- Creating highlights and brightening effects
- Simulating the effect of projecting multiple images onto the same screen

[IMAGE: ScreenBlendKraftShader - Left side shows first texture, middle shows second texture, right side shows screened result]

### HardLightBlendKraftShader

A combination of multiply and screen. Dark areas in the second texture darken the first texture, while light areas lighten it.

**Parameters:**
- `intensity`: Controls the strength of the blend effect

**Example Use Cases:**
- Creating dramatic lighting effects
- Adding texture and contrast to images

[IMAGE: HardLightBlendKraftShader - Left side shows first texture, middle shows second texture, right side shows hard light blend result]

### AddBlendKraftShader

Adds the color values of the two textures, resulting in a brighter image.

**Parameters:**
- `intensity`: Controls the strength of the blend effect

**Example Use Cases:**
- Creating glow and light effects
- Combining multiple light sources

[IMAGE: AddBlendKraftShader - Left side shows first texture, middle shows second texture, right side shows additive blend result]

### SourceOverBlendKraftShader

Places the second texture over the first, respecting the alpha channel of both textures.

**Parameters:**
- `intensity`: Controls the strength of the blend effect

**Example Use Cases:**
- Standard alpha compositing
- Layering images with transparency

[IMAGE: SourceOverBlendKraftShader - Left side shows first texture, middle shows second texture, right side shows source-over blend result]

## Using Blending Mode Shaders in Pipelines

Blending mode shaders require two input textures and are typically used to combine the results of previous processing steps. They can be used in various stages of a pipeline to create complex visual effects.

## Performance Considerations

When working with blend modes:

- Blending operations require two texture inputs, which can increase memory usage
- Some blend modes are more computationally expensive than others
- Consider using intermediate buffers to avoid recalculating the same inputs multiple times
- For complex compositions with many layers, consider grouping blends to reduce the number of passes

## Related Topics

- [Alpha & Transparency Effects](./alpha-transparency-effects): Learn about transparency manipulation
- [Pipeline DSL](../pipeline-dsl): See how to combine shaders into complex effects
- [Buffer Management](../core-components/pipeline-system/buffer-management): Understand how to efficiently manage buffers for multi-pass effects
