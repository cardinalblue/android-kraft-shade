---
sidebar_position: 5
---

# Edge Detection

Edge detection shaders in KraftShade identify and highlight boundaries between different regions in an image, which is essential for feature extraction, object recognition, and artistic effects.

## Overview

Edge detection is a fundamental image processing technique that identifies points in an image where the brightness changes sharply or has discontinuities. These points typically organize into a set of curved line segments called edges. Edge detection is widely used in computer vision, image analysis, and for creating artistic effects.

KraftShade provides several edge detection shaders that implement different algorithms, each with its own characteristics and use cases.

## Available Edge Detection Shaders

### SobelEdgeDetectionKraftShader

Implements the Sobel operator, a classic edge detection algorithm that computes the gradient magnitude of the image intensity function. It highlights areas of high spatial frequency that correspond to edges.

**How It Works:**
The Sobel operator uses two 3×3 kernels which are convolved with the original image to calculate approximations of the derivatives - one for horizontal changes, and one for vertical. For each pixel, the resulting gradient approximations are combined to give the gradient magnitude.

**Example Use Cases:**
- Feature extraction in computer vision
- Creating outline or sketch effects
- Preprocessing for object recognition
- Artistic edge highlighting

[IMAGE: SobelEdgeDetectionKraftShader - Left side shows original image, right side shows image with Sobel edge detection applied]

### DirectionalSobelEdgeDetectionKraftShader

An extension of the Sobel operator that preserves the directional information of edges, not just their magnitude. This provides more detailed edge information, including the orientation of edges.

**How It Works:**
Similar to the standard Sobel operator, but instead of combining the horizontal and vertical gradients into a single magnitude, it preserves both components. This allows for distinguishing between horizontal, vertical, and diagonal edges.

**Example Use Cases:**
- Advanced feature extraction
- Direction-aware edge processing
- Creating effects that depend on edge orientation
- Pattern recognition

[IMAGE: DirectionalSobelEdgeDetectionKraftShader - Left side shows original image, right side shows image with directional Sobel edge detection applied]

## Laplacian-Based Edge Detection

While covered in the [Convolution & Mask Processing](./convolution-mask-processing) section, it's worth mentioning that KraftShade also provides Laplacian-based edge detection:

### LaplacianKraftShader

Uses the Laplacian operator to detect edges by finding areas where the second derivative of the image intensity has a zero crossing.

[IMAGE: LaplacianKraftShader - Left side shows original image, right side shows image with Laplacian edge detection applied]

### LaplacianMagnitudeKraftShader

Calculates the magnitude of the Laplacian to detect edges regardless of direction.

[IMAGE: LaplacianMagnitudeKraftShader - Left side shows original image, right side shows image with Laplacian magnitude edge detection applied]

## Using Edge Detection in Pipelines

Edge detection shaders are often used as part of a larger image processing pipeline. They can be combined with other effects to create more sophisticated visual results.

### Edge Overlay

Detect edges and overlay them on the original image to create a highlighted edge effect.

[IMAGE: Edge Overlay - Left side shows original image, middle shows detected edges, right side shows original with edges overlaid]

### Edge-Aware Blur

Apply blur that preserves edges to create a smooth image that maintains important details.

[IMAGE: Edge-Aware Blur - Left side shows original image, right side shows image with edge-aware blur applied]

## Customizing Edge Detection

You can customize edge detection by adjusting the threshold or sensitivity to create different visual effects:

### Thresholding After Edge Detection

Apply a threshold to the edge detection result to create binary edges (black and white only).

[IMAGE: Thresholded Edges - Left side shows original edge detection result, right side shows thresholded binary edges]

## Performance Considerations

When working with edge detection:

- Edge detection shaders typically involve convolution operations, which can be computationally expensive
- Consider applying edge detection at a lower resolution for better performance
- For real-time applications, the Sobel operator is generally more efficient than Laplacian-based methods
- Pre-processing with a slight blur can help reduce noise and improve edge detection results

## Advanced Edge Detection Techniques

While KraftShade provides the most common edge detection algorithms, you can implement more advanced techniques by combining existing shaders or creating custom ones:

### Canny Edge Detection

The Canny edge detector is a multi-stage algorithm that can be approximated by combining several KraftShade shaders:

1. Apply Gaussian blur to reduce noise
2. Apply Sobel edge detection
3. Apply non-maximum suppression (can be implemented with a custom shader)
4. Apply hysteresis thresholding (can be implemented with multiple passes and thresholds)

[IMAGE: Canny Edge Detection - Left side shows original image, right side shows image with Canny edge detection applied]

## Related Topics

- [Convolution & Mask Processing](./convolution-mask-processing): Learn about kernel-based image processing
- [Texture & Artistic Effects](./texture-artistic-effects): Combine edge detection with artistic effects
- [Pipeline DSL](../pipeline-dsl): See how to combine shaders into complex effects
