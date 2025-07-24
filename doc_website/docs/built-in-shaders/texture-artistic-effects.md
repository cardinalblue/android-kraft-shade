---
sidebar_position: 4
---

# Texture & Artistic Effects

Texture and artistic effect shaders in KraftShade transform images into stylized renderings, simulating various artistic techniques and visual styles.

## Overview

Artistic effects transform ordinary images into stylized renderings that mimic traditional art forms, painting techniques, or other visual styles. These effects go beyond basic color adjustments to fundamentally alter the appearance of an image, often by manipulating texture, detail, and color relationships.

KraftShade provides a variety of artistic effect shaders that can be used to create unique visual styles for images and videos.

## Available Artistic Effect Shaders

### CrosshatchKraftShader

Creates a crosshatching effect that simulates the drawing technique used in sketching and traditional printing, where lines are drawn in different directions to create shading.

**Parameters:**
- `crossHatchSpacing`: Float value controlling the distance between crosshatch lines (default: 0.03f)
- `lineWidth`: Float value controlling the width of the crosshatch lines (default: 0.003f)

**Example Use Cases:**
- Creating sketch-like renderings
- Simulating traditional print techniques
- Artistic stylization for illustrations

[IMAGE: CrosshatchKraftShader - Left side shows original image, right side shows image with crosshatch effect applied]

### ToonKraftShader

Creates a cartoon-like effect by quantizing colors and enhancing edges, similar to cel shading in 3D animation.

**Parameters:**
- `quantizationLevels`: Float value controlling the number of color levels (default: 10.0f)
- `threshold`: Float value controlling the edge detection threshold (default: 0.2f)

**Example Use Cases:**
- Creating cartoon-like images
- Cel shading effects
- Comic book style rendering

[IMAGE: ToonKraftShader - Left side shows original image, right side shows image with toon/cel shading effect applied]

### KuwaharaKraftShader

Creates a painterly effect by averaging colors within regions while preserving edges, resulting in a smoothed appearance that resembles watercolor or oil painting.

**Parameters:**
- `radius`: Float value controlling the size of the sampling regions (default: 8.0f)

**Example Use Cases:**
- Creating painterly effects
- Simulating watercolor or oil painting
- Artistic noise reduction while preserving edges

[IMAGE: KuwaharaKraftShader - Left side shows original image, right side shows image with Kuwahara filter applied]

### VignetteKraftShader

Creates a vignette effect that darkens the corners of an image, drawing attention to the center and creating a classic photographic look.

**Parameters:**
- `vignetteCenter`: FloatArray (2 elements) specifying the center point of the vignette (default: (0.0f, 0.0f))
- `vignetteColor`: FloatArray (4 elements) specifying the RGBA color of the vignette (default: black)
- `vignetteStart`: Float value specifying where the vignette effect starts (default: 0.3f)
- `vignetteEnd`: Float value specifying where the vignette effect ends (default: 0.75f)

**Example Use Cases:**
- Creating classic photographic looks
- Directing attention to the center of an image
- Adding a vintage or cinematic feel

[IMAGE: VignetteKraftShader - Left side shows original image, right side shows image with vignette effect applied]

### PosterizeKraftShader

Reduces the number of colors in an image to create a poster-like effect with flat areas of color.

**Parameters:**
- `colorLevels`: Int value controlling the number of color levels per channel (default: 10)

**Example Use Cases:**
- Creating poster art effects
- Simulating screen printing
- Reducing color complexity for stylistic purposes

[IMAGE: PosterizeKraftShader - Left side shows original image, right side shows posterized image]

### HazeKraftShader

Creates a haze or fog effect by reducing contrast and adding a color tint to distant areas of the image.

**Parameters:**
- `distance`: Float value controlling the amount of haze (default: 0.2f)
- `slope`: Float value controlling how quickly the haze increases with "distance" (default: 0.0f)
- `hazeColor`: FloatArray (4 elements) specifying the RGBA color of the haze (set via property)

**Example Use Cases:**
- Creating atmospheric effects
- Simulating fog or mist
- Adding depth to landscapes

[IMAGE: HazeKraftShader - Left side shows original image, right side shows image with haze effect applied]

## Combining Artistic Effects

Artistic effects can be combined to create more complex and unique visual styles. For example, you could apply a Kuwahara filter followed by a vignette effect to create a painterly image with a classic photographic look.

## Selective Application

Artistic effects can be applied selectively to certain areas of an image using masks. This allows for creative combinations where different parts of an image have different artistic treatments.

[IMAGE: Selective Application - Left side shows original image, middle shows mask, right side shows result with effect applied only to masked area]

## Animated Artistic Effects

For video or animation, you can create dynamic artistic effects by animating parameters over time. This can create interesting transitions between different artistic styles or enhance the visual impact of motion.

## Performance Considerations

When working with artistic effects:

- Many artistic effects involve complex calculations and can be computationally expensive
- Consider applying effects at a lower resolution for better performance
- For real-time applications, adjust the quality parameters (like sampling radius) based on the device capabilities
- Some effects (like Kuwahara) are particularly expensive and may need optimization for real-time use

## Related Topics

- [Color Effects](./color-effects): Combine with color adjustments for enhanced artistic effects
- [Edge Detection](./edge-detection): Use edge detection as part of artistic stylization
- [Blending Modes](./blending-modes): Combine multiple effects using different blending modes
- [Pipeline DSL](../pipeline-dsl): See how to combine shaders into complex effects
