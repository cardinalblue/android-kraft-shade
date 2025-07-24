---
sidebar_position: 3
---

# Alpha & Transparency Effects

Alpha and transparency effects in KraftShade allow you to manipulate the transparency and opacity of images, which is essential for compositing and creating visual effects.

## Overview

Alpha channel manipulation is a fundamental aspect of image processing. KraftShade provides several shaders specifically designed to work with the alpha (transparency) channel of images. These shaders can be used to create masks, adjust opacity, or prepare images for compositing with other elements.

## Available Alpha & Transparency Shaders

### OpacityKraftShader

Adjusts the overall opacity of an image while preserving its original color values.

**Parameters:**
- `opacity`: Float value between 0.0 (completely transparent) and 1.0 (fully opaque)

**Example Use Cases:**
- Fading elements in and out
- Creating semi-transparent overlays
- Adjusting the visibility of layers in a composition

[IMAGE: OpacityKraftShader - Left side shows original image, right side shows image with reduced opacity (50%)]

### AlphaInvertKraftShader

Inverts the alpha channel of an image, making transparent areas opaque and opaque areas transparent while preserving color information.

**Example Use Cases:**
- Creating inverted masks
- Special effects that play with transparency
- Preparing images for certain compositing operations

[IMAGE: AlphaInvertKraftShader - Left side shows original image with alpha channel, right side shows image with inverted alpha]

### ApplyAlphaMaskKraftShader

Applies an alpha mask from a second texture to the input texture. This allows you to define the transparency of one image based on another image.

**Parameters:**
- `reverseAlpha`: Boolean that determines whether to invert the mask effect (default: false)
- `maskChannel`: The color channel to use as the mask source (default: GlColorChannel.A)

**Example Use Cases:**
- Creating complex shaped transparency
- Applying gradient transparency
- Masking one image with the shape of another

[IMAGE: ApplyAlphaMaskKraftShader - Left side shows original image, middle shows mask image, right side shows result after applying mask]

## Using Alpha & Transparency Shaders in Pipelines

Alpha and transparency shaders are particularly useful in multi-step pipelines where you need to composite multiple elements. They can be combined with other shader types to create sophisticated visual effects.

## Combining with Other Shaders

Alpha and transparency shaders are often used in combination with other shader types:

- **With Blend Shaders**: Control how images are composited together
- **With Color Effects**: Apply color adjustments to semi-transparent areas
- **With Masks**: Create complex masking effects

## Performance Considerations

When working with alpha and transparency:

- Be mindful of premultiplied vs. non-premultiplied alpha, as this can affect blending results
- Alpha operations can sometimes be combined with other effects to reduce the number of rendering passes
- For complex masking operations, consider pre-computing masks when possible to improve performance

## Related Topics

- [Blending Modes](./blending-modes): Learn about different ways to blend images together
- [Color Effects](./color-effects): Understand how color adjustments interact with transparency
- [Pipeline DSL](../pipeline-dsl): See how to combine shaders into complex effects
