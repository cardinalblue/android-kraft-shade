---
sidebar_position: 6
---

# Convolution & Mask Processing

Convolution and mask processing shaders in KraftShade enable sophisticated image processing operations like sharpening, embossing, and morphological transformations.

## Overview

Convolution is a fundamental image processing technique that applies a mathematical operation to each pixel based on its surrounding pixels. This technique is the foundation for many effects including blurring, sharpening, edge detection, and embossing.

KraftShade provides several convolution-based shaders that operate on a 3x3 grid of pixels surrounding each pixel in the image. These shaders can be used to create a wide range of effects by simply changing the convolution matrix.

## Convolution Base Classes

### Sample3x3KraftShader

The base class for shaders that sample a 3x3 grid of pixels around each pixel in the input texture. This class handles the sampling logic but doesn't define how the samples are processed.

### Convolution3x3KraftShader

A specialized shader that applies a 3x3 convolution matrix to the image. This is the base class for many convolution-based effects.

**Parameters:**
- `convolution`: GlMat3 representing the 3x3 convolution matrix

[IMAGE: Convolution3x3KraftShader - Left side shows original image, right side shows image with custom convolution matrix applied]

### Convolution3x3WithColorOffsetKraftShader

An extension of the basic convolution shader that adds a color offset to the result, allowing for more sophisticated effects.

**Parameters:**
- `convolutionMatrix`: GlMat3 representing the 3x3 convolution matrix
- `colorOffset`: FloatArray (4 elements) representing RGBA color offsets

[IMAGE: Convolution3x3WithColorOffsetKraftShader - Left side shows original image, right side shows image with convolution and color offset applied]

## Image Enhancement Shaders

### SharpenKraftShader

Enhances the details in an image by increasing the contrast between adjacent pixels.

**Parameters:**
- `imageWidthFactor`: Float value related to the image width (default: 0f)
- `imageHeightFactor`: Float value related to the image height (default: 0f)
- `sharpness`: Float value controlling the strength of the sharpening effect (default: 0f)

**Example Use Cases:**
- Enhancing details in photographs
- Improving text readability
- Compensating for slight blur in images

[IMAGE: SharpenKraftShader - Left side shows original image, right side shows sharpened image]

### EmbossKraftShader

Creates an embossed effect that makes the image appear raised or stamped.

**Parameters:**
- `intensity`: Float value controlling the strength of the emboss effect (default: 1f)

**Example Use Cases:**
- Creating metallic or stamped effects
- Adding texture to flat surfaces
- Creating relief-like images

[IMAGE: EmbossKraftShader - Left side shows original image, right side shows embossed image]

## Laplacian Filters

### LaplacianKraftShader

Applies a Laplacian filter to detect edges in all directions. This shader extends Convolution3x3WithColorOffsetKraftShader with a specific convolution matrix.

**Example Use Cases:**
- Edge detection
- Feature extraction
- Image analysis

[IMAGE: LaplacianKraftShader - Left side shows original image, right side shows image with Laplacian filter applied]

### LaplacianMagnitudeKraftShader

Calculates the magnitude of the Laplacian to detect edges regardless of direction.

**Example Use Cases:**
- More robust edge detection
- Finding boundaries in images
- Preprocessing for computer vision algorithms

[IMAGE: LaplacianMagnitudeKraftShader - Left side shows original image, right side shows image with Laplacian magnitude filter applied]

## Morphological Operations

Morphological operations are a set of non-linear operations related to the shape or morphology of features in an image.

### DilationKraftShader

Expands bright regions and shrinks dark regions in an image. This is useful for filling in small holes or gaps.

**Parameters:**
- `radius`: Int value controlling the size of the dilation operation (default: 1)

**Example Use Cases:**
- Filling in small holes or gaps
- Expanding features in binary images
- Connecting nearby objects

[IMAGE: DilationKraftShader - Left side shows original image, right side shows image after dilation]

### ErosionKraftShader

Shrinks bright regions and expands dark regions in an image. This is useful for removing small objects or thin lines.

**Parameters:**
- `radius`: Int value controlling the size of the erosion operation (default: 1)

**Example Use Cases:**
- Removing small objects or noise
- Shrinking features in binary images
- Separating connected objects

[IMAGE: ErosionKraftShader - Left side shows original image, right side shows image after erosion]

## Common Convolution Matrices

Here are some common convolution matrices that you can use with the Convolution3x3KraftShader:

### Box Blur
```
1/9, 1/9, 1/9
1/9, 1/9, 1/9
1/9, 1/9, 1/9
```

### Gaussian Blur
```
1/16, 2/16, 1/16
2/16, 4/16, 2/16
1/16, 2/16, 1/16
```

### Sharpen
```
 0, -1,  0
-1,  5, -1
 0, -1,  0
```

### Edge Detection
```
-1, -1, -1
-1,  8, -1
-1, -1, -1
```

### Emboss
```
-2, -1,  0
-1,  1,  1
 0,  1,  2
```

## Using Convolution Shaders in Pipelines

Convolution shaders can be combined with other effects to create sophisticated image processing pipelines. They are particularly effective when used in sequence with color adjustments or blending operations.

## Performance Considerations

When working with convolution operations:

- Convolution shaders sample multiple pixels for each output pixel, which can be computationally expensive
- Larger convolution kernels (beyond 3x3) are significantly more expensive
- For large blur operations, consider using a two-pass approach (horizontal then vertical) instead of a single large convolution
- Morphological operations like dilation and erosion can be particularly expensive for large radii

## Advanced Techniques

### Combining Morphological Operations

Dilation and erosion can be combined to create more complex morphological operations:

- **Opening** (erosion followed by dilation): Removes small objects while preserving the shape and size of larger objects
- **Closing** (dilation followed by erosion): Fills in small holes and gaps while preserving the shape and size of objects

### Multi-Pass Convolution

For more sophisticated effects, you can apply multiple convolution passes in sequence to achieve complex transformations that wouldn't be possible with a single pass.

## Related Topics

- [Edge Detection](./edge-detection): Learn about specialized edge detection algorithms
- [Blur & Distortion](./blur-distortion): Understand blur effects that use convolution
- [Pipeline DSL](../pipeline-dsl): See how to combine shaders into complex effects
