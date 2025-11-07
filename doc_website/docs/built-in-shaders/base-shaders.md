---
sidebar_position: 1
---

# Base Shaders

Base shaders provide fundamental operations that serve as building blocks for more complex effects in KraftShade.

## Overview

Base shaders are the foundation of KraftShade's shader system. They implement essential operations that can be used alone or combined with other shaders to create sophisticated visual effects. These shaders are optimized for performance and follow a consistent API pattern.

## Available Base Shaders

### DrawTextureKraftShader

The simplest shader that draws the input texture to the output. While it appears to do "nothing" in terms of visual effects, it's actually quite useful for various purposes including texture positioning, coordinate transformation, and as a building block for more complex effects.

```kotlin
// Create a shader that draws the input texture directly
val shader = DrawTextureKraftShader()
```

#### Practical Use Cases

**1. Texture Positioning and Transformation**

You can use `withTransform` to move, scale, rotate, or flip the texture by transforming the sampling coordinates:

```kotlin
// Move texture to a different position
val shader = DrawTextureKraftShader().withTransform {
    translate(0.2f, 0.1f) // Move texture right and up
}

// Scale and rotate texture
val shader = DrawTextureKraftShader().withTransform {
    scale(0.8f, 0.8f)     // Scale down to 80%
    rotate(45f)           // Rotate 45 degrees
}

// Flip texture horizontally
val shader = DrawTextureKraftShader().withTransform {
    scale(-1f, 1f)        // Flip horizontally
    translate(1f, 0f)     // Adjust position after flip
}
```

**2. Texture Cropping and Viewport Control**

Transform coordinates to show only a portion of the texture:

```kotlin
// Show only the center 50% of the texture
val shader = DrawTextureKraftShader().withTransform {
    scale(2f, 2f)         // Scale up sampling
    translate(-0.25f, -0.25f) // Center the crop
}
```

**3. Testing and Debugging**

- **Pipeline Testing**: Verify that your pipeline setup is working correctly
- **Performance Baseline**: Measure the overhead of your pipeline without effect processing
- **Texture Validation**: Confirm that textures are loaded and passed correctly through the pipeline

**4. Conditional Processing**

Use as a fallback when no effects should be applied:

```kotlin
val shader = if (effectsEnabled) {
    SaturationKraftShader(saturation = 1.5f)
} else {
    DrawTextureKraftShader() // No effect, just draw the texture
}
```

### BypassableTextureInputKraftShader

A wrapper shader that can conditionally bypass the wrapped shader's processing. This is useful for enabling/disabling effects dynamically without restructuring your pipeline.

```kotlin
// Create a saturation shader that can be bypassed
val saturationShader = SaturationKraftShader(saturation = 1.5f)
val bypassableShader = BypassableTextureInputKraftShader(
    wrappedShader = saturationShader,
    bypass = false // Initially enabled
)

// Later, to bypass the shader:
bypassableShader.bypass = true
```

### BypassableTwoTextureInputKraftShader

Similar to `BypassableTextureInputKraftShader`, but designed for shaders that take two input textures (like blend shaders).

```kotlin
// Create a blend shader that can be bypassed
val blendShader = MultiplyBlendKraftShader()
val bypassableShader = BypassableTwoTextureInputKraftShader(
    wrappedShader = blendShader,
    bypass = false, // Initially enabled
    passTexture1 = true // When bypassed, pass the first texture
)
```

## Base Shader Types

KraftShade provides several base shader types that serve as parent classes for more specific shader implementations:

### TextureInputKraftShader

The most common base shader type that takes a single texture as input and produces a modified output.

### TwoTextureInputKraftShader

A base shader type that takes two textures as input, typically used for blending operations.

### Sample3x3KraftShader

A specialized shader that samples a 3x3 grid of pixels around each pixel in the input texture. This is the foundation for convolution operations, edge detection, and other neighborhood-based effects.

### SingleDirectionForTwoPassSamplingKraftShader

A specialized shader designed for two-pass operations where each pass processes the image in a different direction (typically horizontal and vertical). This approach is used for separable filters like Gaussian blur for better performance.

## Using Base Shaders in Pipelines

Base shaders can be used directly in your rendering pipelines:

```kotlin
pipeline(targetBuffer) {
    serialSteps(
        inputTexture = inputBitmap.asTexture(),
        targetBuffer = targetBuffer
    ) {
        // Use a base shader as a placeholder or for testing
        step(DrawTextureKraftShader())
        
        // Use a bypassable shader for conditional processing
        val bypassableShader = BypassableTextureInputKraftShader(
            wrappedShader = SaturationKraftShader(saturation = 1.5f)
        )
        step(bypassableShader)
    }
}
```

## Creating Custom Base Shaders

You can extend the base shader types to create your own custom shaders. For example:

```kotlin
class MyCustomShader : TextureInputKraftShader() {
    var intensity: Float by GlUniformDelegate("intensity")
    
    init {
        intensity = 1.0f
    }
    
    override fun loadFragmentShader(): String {
        return """
            precision mediump float;
            varying vec2 textureCoordinate;
            uniform sampler2D inputImageTexture;
            uniform float intensity;
            
            void main() {
                vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
                // Apply your custom effect here
                gl_FragColor = textureColor * intensity;
            }
        """
    }
}
```

## Related Topics

- [KraftShader](../core-components/shader-system/kraft-shader): Learn about the base shader class
- [GlUniformDelegate](../core-components/shader-system/gl-uniform-delegate): Understand how shader parameters are managed
- [Pipeline DSL](../pipeline-dsl): See how to combine shaders into pipelines
