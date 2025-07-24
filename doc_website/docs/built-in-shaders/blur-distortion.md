---
sidebar_position: 8
---

# Blur & Distortion

Blur and distortion effects in KraftShade allow you to create a wide range of visual transformations, from subtle blurring to dramatic warping of images.

## Overview

Blur and distortion shaders manipulate the sampling coordinates or combine multiple samples to create effects that alter the spatial characteristics of an image. These effects are commonly used in photography, video processing, and creative applications to direct attention, create depth, or add artistic flair.

## Available Blur Shaders

### CircularBlurKraftShader

Creates a radial blur effect that simulates motion in a circular pattern around a center point.

**Parameters:**
- `amount`: Float value controlling the strength of the blur effect (default: 0.3f)
- `repeat`: Float value determining the number of samples to take (default: 30f)

**Example Use Cases:**
- Creating a spinning or whirlpool effect
- Simulating camera rotation
- Adding dynamic motion to static images

[IMAGE: CircularBlurKraftShader - Left side shows original image, right side shows image with circular blur applied]

### ZoomBlurKraftShader

Creates a radial blur that simulates motion towards or away from a center point, like a camera zooming in or out.

**Parameters:**
- `blurCenter`: GlVec2 value specifying the center point of the blur (default: (0.5f, 0.5f))
- `blurSize`: Float value controlling the strength of the blur effect (default: 1.0f)

**Example Use Cases:**
- Simulating camera zoom
- Creating depth effects
- Directing attention to a specific area of an image

[IMAGE: ZoomBlurKraftShader - Left side shows original image, right side shows image with zoom blur applied]

## Available Distortion Shaders

### SwirlKraftShader

Creates a swirling distortion effect that twists the image around a center point.

**Parameters:**
- `radius`: Float value specifying the radius of the effect (default: 0.5f)
- `angle`: Float value controlling the angle of the swirl in radians (default: 1.0f)
- `center`: GlVec2 value specifying the center point of the swirl (default: (0.5f, 0.5f))

**Example Use Cases:**
- Creating whirlpool or vortex effects
- Adding dynamic motion to static images
- Creating artistic distortions

[IMAGE: SwirlKraftShader - Left side shows original image, right side shows image with swirl distortion applied]

### BulgeDistortionKraftShader

Creates a bulge or pinch distortion effect, like looking through a fisheye lens or a magnifying glass.

**Parameters:**
- `radius`: Float value specifying the radius of the effect (default: 0.25f)
- `scale`: Float value controlling the scale factor (default: 0.5f, negative values create a pinch effect)
- `center`: GlVec2 value specifying the center point of the distortion (default: (0.5f, 0.5f))

**Example Use Cases:**
- Creating fisheye lens effects
- Simulating magnification or minimization
- Creating fun-house mirror distortions

[IMAGE: BulgeDistortionKraftShader - Left side shows original image, right side shows image with bulge distortion applied]

### PixelationKraftShader

Creates a pixelated effect by reducing the effective resolution of the image.

**Parameters:**
- `pixel`: Float value controlling the size of the pixels (default: 1.0f)

**Example Use Cases:**
- Creating retro or 8-bit style effects
- Censoring or obscuring parts of an image
- Creating mosaic-like artistic effects

[IMAGE: PixelationKraftShader - Left side shows original image, right side shows pixelated image]

## Using Blur & Distortion Shaders in Pipelines

Blur and distortion shaders can be combined with other effects to create complex visual transformations. They are particularly effective when used in combination with color effects or blending modes.

## Performance Considerations

When working with blur and distortion effects:

- Blur effects often involve multiple texture samples, which can be computationally expensive
- Higher quality blur effects require more samples, increasing the processing time
- Consider using lower resolution intermediate buffers for heavy blur operations
- For animated effects, adjust the parameters gradually to ensure smooth transitions
- Some distortion effects may cause sampling artifacts at the edges of the image

## Advanced Techniques

### Multi-Pass Blur

For higher quality blur effects, consider using a two-pass approach where horizontal and vertical blur are applied separately. This approach is more efficient than applying a single large blur and produces better results.

### Animated Distortions

For animated distortion effects, you can adjust parameters over time to create dynamic visual effects. This is particularly effective for creating flowing water, heat haze, or other natural phenomena.

## Related Topics

- [Texture & Artistic Effects](./texture-artistic-effects): Learn about other creative effects
- [Convolution & Mask Processing](./convolution-mask-processing): Understand kernel-based image processing
- [Pipeline DSL](../pipeline-dsl): See how to combine shaders into complex effects
