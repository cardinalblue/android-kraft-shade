---
sidebar_position: 2
---

# Color Effects

Color effects in KraftShade allow you to manipulate the color properties of images, enabling a wide range of adjustments from basic brightness and contrast to advanced color grading.

## Overview

Color manipulation is one of the most common image processing operations. KraftShade provides a comprehensive set of color effect shaders that can be used individually or combined to achieve sophisticated color grading and correction.

## Basic Color Adjustments

### BrightnessKraftShader

Adjusts the brightness of an image by adding a constant value to each color channel.

**Parameters:**
- `brightness`: Float value controlling the brightness adjustment (default: 0f)
  - Positive values increase brightness
  - Negative values decrease brightness

**Example Use Cases:**
- Correcting underexposed or overexposed images
- Enhancing visibility in dark areas
- Creating high-key or low-key effects

[IMAGE: BrightnessKraftShader - Left side shows original image, right side shows image with increased brightness]

### ContrastKraftShader

Adjusts the contrast of an image by scaling the color values around a midpoint.

**Parameters:**
- `contrast`: Float value controlling the contrast adjustment (default: 1.2f)
  - Values > 1.0 increase contrast
  - Values < 1.0 decrease contrast

**Example Use Cases:**
- Enhancing details in flat images
- Creating dramatic or muted looks
- Improving visibility of textures

[IMAGE: ContrastKraftShader - Left side shows original image, right side shows image with increased contrast]

### SaturationKraftShader

Adjusts the saturation of an image, controlling the intensity of colors.

**Parameters:**
- `saturation`: Float value controlling the saturation adjustment (default: 0f)
  - Values > 0.0 increase saturation
  - Values < 0.0 decrease saturation
  - Value of 0.0 has no effect
  - Value of -1.0 produces grayscale

**Example Use Cases:**
- Enhancing or muting colors
- Creating vibrant or subdued looks
- Preparing images for specific media

[IMAGE: SaturationKraftShader - Left side shows original image, right side shows image with increased saturation]

### HueKraftShader

Adjusts the hue of an image, shifting all colors around the color wheel.

**Parameters:**
- `hue`: Float value controlling the hue rotation in degrees (default: 90f)
  - Range: 0 to 360 degrees

**Example Use Cases:**
- Creating color variations
- Artistic color transformations
- Matching specific color schemes

[IMAGE: HueKraftShader - Left side shows original image, right side shows image with hue shifted by 90 degrees]

## Color Conversion

### GrayscaleKraftShader

Converts an image to grayscale while preserving luminance.

**Example Use Cases:**
- Creating black and white images
- Preparing for edge detection
- Reducing visual complexity

[IMAGE: GrayscaleKraftShader - Left side shows original color image, right side shows grayscale version]

### SepiaToneKraftShader

Applies a sepia tone effect to create a vintage, brownish appearance.

**Parameters:**
- `intensity`: Float value controlling the strength of the sepia effect (default: 1.0f)

**Example Use Cases:**
- Creating vintage or aged photo effects
- Adding warmth to images
- Stylistic transformations

[IMAGE: SepiaToneKraftShader - Left side shows original image, right side shows sepia-toned version]

### MonochromeKraftShader

Converts an image to monochrome (black and white) with adjustable intensity.

**Parameters:**
- `intensity`: Float value controlling the strength of the monochrome effect (default: 1.0f)

**Example Use Cases:**
- Creating high-contrast black and white images
- Artistic stylization
- Focusing on form rather than color

[IMAGE: MonochromeKraftShader - Left side shows original image, right side shows monochrome version]

### ColorInvertKraftShader

Inverts all colors in the image (similar to a photographic negative).

**Example Use Cases:**
- Creating negative effects
- Artistic stylization
- Enhancing visibility of certain details

[IMAGE: ColorInvertKraftShader - Left side shows original image, right side shows color-inverted version]

## Advanced Color Adjustments

### ColorMatrixKraftShader

Applies a 4x4 color matrix transformation to the image, allowing for sophisticated color manipulations.

**Parameters:**
- `intensity`: Float value controlling the strength of the effect (default: 1.0f)
- `colorMatrix`: FloatArray (16 elements) representing a 4x4 color transformation matrix
- `colorOffset`: FloatArray (4 elements) representing RGBA color offsets

**Example Use Cases:**
- Custom color transformations
- Implementing specific color filters
- Creating complex color effects

[IMAGE: ColorMatrixKraftShader - Left side shows original image, right side shows image with custom color matrix applied]

### ColorBalanceKraftShader

Adjusts the color balance of an image by modifying the shadows, midtones, and highlights separately.

**Parameters:**
- `shadows`: FloatArray (3 elements) controlling RGB adjustments for shadows
- `midtones`: FloatArray (3 elements) controlling RGB adjustments for midtones
- `highlights`: FloatArray (3 elements) controlling RGB adjustments for highlights
- `preserveLuminosity`: Boolean determining whether to preserve brightness (default: true)

**Example Use Cases:**
- Professional color grading
- Correcting color casts
- Creating specific color moods

[IMAGE: ColorBalanceKraftShader - Left side shows original image, right side shows image with adjusted color balance]

### WhiteBalanceKraftShader

Adjusts the white balance of an image by modifying the temperature and tint.

**Parameters:**
- `temperature`: Float value representing color temperature in Kelvin (default: 5000.0f)
- `tint`: Float value controlling green-magenta tint adjustment (default: 0.0f)

**Example Use Cases:**
- Correcting color temperature
- Matching lighting conditions
- Creating warm or cool atmospheres

[IMAGE: WhiteBalanceKraftShader - Left side shows original image, right side shows image with adjusted white balance]

### ExposureKraftShader

Adjusts the exposure of an image, simulating the effect of changing camera exposure.

**Parameters:**
- `exposure`: Float value controlling the exposure adjustment (default: 0f)
  - Positive values increase exposure
  - Negative values decrease exposure

**Example Use Cases:**
- Correcting exposure issues
- Creating high-key or low-key effects
- Simulating different lighting conditions

[IMAGE: ExposureKraftShader - Left side shows original image, right side shows image with adjusted exposure]

### GammaKraftShader

Adjusts the gamma of an image, affecting the midtones while preserving blacks and whites.

**Parameters:**
- `gamma`: Float value controlling the gamma adjustment (default: 1f)
  - Values > 1.0 lighten midtones
  - Values < 1.0 darken midtones

**Example Use Cases:**
- Correcting display gamma
- Adjusting midtone brightness
- Enhancing details in shadows or highlights

[IMAGE: GammaKraftShader - Left side shows original image, right side shows image with adjusted gamma]

### HighlightShadowKraftShader

Adjusts the highlights and shadows of an image independently.

**Parameters:**
- `shadows`: Float value controlling shadow adjustment (default: 0.0f)
  - Values > 0.0 lighten shadows
  - Values < 0.0 darken shadows
- `highlights`: Float value controlling highlight adjustment (default: 1.0f)
  - Values < 1.0 darken highlights
  - Values > 1.0 lighten highlights

**Example Use Cases:**
- Recovering details in shadows and highlights
- Reducing contrast while preserving detail
- Creating HDR-like effects

[IMAGE: HighlightShadowKraftShader - Left side shows original image, right side shows image with adjusted highlights and shadows]

### VibranceKraftShader

Adjusts the vibrance of an image, increasing saturation while preserving skin tones.

**Parameters:**
- `vibrance`: Float value controlling the vibrance adjustment (default: 0f)
  - Positive values increase vibrance
  - Negative values decrease vibrance

**Example Use Cases:**
- Enhancing colors without oversaturating skin tones
- Creating vivid images for display
- Improving dull images

[IMAGE: VibranceKraftShader - Left side shows original image, right side shows image with increased vibrance]

### RGBKraftShader

Adjusts the red, green, and blue channels of an image independently.

**Parameters:**
- `red`: Float value controlling the red channel multiplier (default: 1.0f)
- `green`: Float value controlling the green channel multiplier (default: 1.0f)
- `blue`: Float value controlling the blue channel multiplier (default: 1.0f)

**Example Use Cases:**
- Color correction
- Creating color casts
- Artistic color effects

[IMAGE: RGBKraftShader - Left side shows original image, right side shows image with adjusted RGB channels]

## Color Grading

### LevelsKraftShader

Adjusts the levels of an image, allowing for precise control over shadows, midtones, and highlights.

**Parameters:**
- `levelMinimum`: GlFloatArray controlling RGB minimum input levels
- `levelMiddle`: GlFloatArray controlling RGB gamma values
- `levelMaximum`: GlFloatArray controlling RGB maximum input levels
- `minOutput`: GlFloatArray controlling RGB minimum output levels
- `maxOutput`: GlFloatArray controlling RGB maximum output levels

**Example Use Cases:**
- Professional color grading
- Optimizing image contrast
- Correcting exposure issues

[IMAGE: LevelsKraftShader - Left side shows original image, right side shows image with adjusted levels]

### LookUpTableKraftShader

Applies a color lookup table (LUT) to an image for sophisticated color grading.

**Parameters:**
- `intensity`: Float value controlling the strength of the LUT effect (default: 1.0f)

**Example Use Cases:**
- Applying film-like color grading
- Creating consistent color styles across multiple images
- Implementing complex color transformations

[IMAGE: LookUpTableKraftShader - Left side shows original image, middle shows LUT, right side shows image with LUT applied]

## Special Color Effects

### SolarizeKraftShader

Creates a solarize effect by inverting colors that exceed a threshold.

**Parameters:**
- `threshold`: Float value controlling the solarization threshold (default: 0.5f)

**Example Use Cases:**
- Creating artistic effects
- Simulating photographic solarization
- Generating high-contrast images

[IMAGE: SolarizeKraftShader - Left side shows original image, right side shows solarized version]

### FalseColorKraftShader

Maps grayscale luminance values to a gradient between two colors.

**Parameters:**
- `firstColor`: FloatArray (3 elements) representing the RGB color for dark areas (default: black)
- `secondColor`: FloatArray (3 elements) representing the RGB color for light areas (default: white)

**Example Use Cases:**
- Visualizing data
- Creating thermal-like images
- Artistic stylization

[IMAGE: FalseColorKraftShader - Left side shows original image, right side shows false-colored version]

## Using Color Effects in Pipelines

Color effects can be combined to create sophisticated color grading pipelines. They are often used in sequence to build up a specific look or style.

## Performance Considerations

When working with color effects:

- Most color effects are relatively inexpensive in terms of performance
- Chaining multiple color effects can be more efficient than using separate passes
- Consider using ColorMatrixKraftShader to combine multiple simple color adjustments into a single operation
- LookUpTableKraftShader can be more expensive due to the additional texture lookup

## Related Topics

- [Alpha & Transparency Effects](./alpha-transparency-effects): Learn about transparency manipulation
- [Blending Modes](./blending-modes): Understand how different layers can be combined
- [Pipeline DSL](../pipeline-dsl): See how to combine shaders into complex effects
