---
sidebar_position: 3
---

# Basic Concepts

This guide introduces the key concepts and terminology used in KraftShade. Understanding these concepts will help you build more complex and efficient graphics applications.

## Core Architecture

KraftShade is built around several key architectural components that work together to provide a flexible and powerful graphics rendering system.

### GlEnv (Graphics Environment)

The `GlEnv` is the foundation of KraftShade's rendering system:

- It encapsulates all OpenGL ES environment setup in a single class
- Creates a dedicated thread dispatcher and binds the GLES context to that thread
- Ensures all OpenGL operations run in the correct context
- Manages the lifecycle of OpenGL resources

### Shader System

The shader system is responsible for executing graphics operations:

- **KraftShader**: The base shader implementation that provides a modern Kotlin interface to OpenGL shaders
- **GlUniformDelegate**: Handles shader uniform variables with features like:
  - Support for multiple types (Int, Float, FloatArray, GlMat2/3/4)
  - Deferred location query and value setting
  - Optional uniforms support (useful for base class implementations)
  - Automatic GLES API selection for value binding
- **Texture Inputs**: Simplified management of texture inputs to shaders

### Pipeline System

The pipeline system orchestrates the execution of multiple shaders:

- **Pipeline**: A container for a sequence of shader operations
- **PipelineRunContext**: Maintains state during pipeline execution
- **Buffer Management**: Handles intermediate buffers and resource recycling

### Input System

The input system provides a flexible way to feed data into shaders:

- **Input\<T\>**: Base input type for shader parameters
- **SampledInput\<T\>**: Dynamic input handling for values that change over time
- **TimeInput**: Time-based animations
- **MappedInput**: Value transformations

### View Components

KraftShade provides ready-to-use view components for both traditional Android Views and Jetpack Compose:

- **Android Views**:
  - `KraftTextureView`: Base OpenGL rendering view
  - `KraftEffectTextureView`: Effect-enabled view
  - `AnimatedKraftTextureView`: Animation support with Choreographer
- **Jetpack Compose Integration**:
  - `KraftShadeView`: Compose wrapper for KraftTextureView
  - `KraftShadeEffectView`: Compose wrapper for KraftEffectTextureView
  - `KraftShadeAnimatedView`: Compose wrapper for AnimatedKraftTextureView

## Key Concepts

### Shaders

In KraftShade, shaders are the building blocks of visual effects:

- **KraftShader**: The base class for all shaders
- **Shader Parameters**: Properties that control the behavior of a shader
- **Texture Inputs**: Images or buffers that a shader processes
- **Bypassable Shaders**: Shaders that can be conditionally enabled or disabled

### Textures and Buffers

KraftShade uses textures and buffers to manage image data:

- **Texture**: An image stored in GPU memory
- **TextureProvider**: A source of texture data
- **GlBufferProvider**: A target for rendering operations
- **TextureBufferPool**: Manages reusable texture buffers for efficient memory usage

### Pipeline DSL

KraftShade provides a Domain-Specific Language (DSL) for building rendering pipelines:

- **Serial Pipeline**: A linear sequence of shader operations
- **Graph Pipeline**: A complex network of shader operations with flexible input/output connections
- **Nested Pipeline**: A combination of serial and graph pipelines for complex effects

### Pipeline Execution Flow

The typical flow for executing a pipeline in KraftShade is:

1. **Input Updates**: Sample values from ViewModel or MutableState in Compose UI
2. **Reset PipelineRunContext**: Initialize the state for the current frame
3. **Execute Pipeline Steps**: Iterate through steps in the pipeline:
   - Render to intermediate buffers or the final target buffer
   - Get values from input and set them as parameters of the shader
   - Run the shader to render to the target buffer for the step
   - Automatically recycle buffers that are no longer needed

### Effect Serialization

KraftShade allows you to serialize and deserialize effects:

- **EffectSerializer**: Converts pipeline setups into JSON format
- **SerializedEffect**: Reconstructs effects from JSON
- **Texture Provider Mapping**: Maps texture names to TextureProvider instances

## Common Shader Types

KraftShade includes several categories of built-in shaders:

- **Base Shaders**: Core shader implementations like TextureInputKraftShader
- **Color Effects**: Shaders for color manipulation like SaturationKraftShader
- **Alpha & Transparency Effects**: Shaders for transparency like AlphaBlendKraftShader
- **Texture & Artistic Effects**: Shaders for artistic effects like CrosshatchKraftShader
- **Edge Detection**: Shaders for edge detection like SobelEdgeDetectionKraftShader
- **Convolution & Mask Processing**: Shaders for convolution operations like Convolution3x3KraftShader
- **Blending Modes**: Shaders for blending operations like MultiplyBlendKraftShader
- **Blur & Distortion**: Shaders for blur and distortion effects like CircularBlurKraftShader

## Next Steps

Now that you understand the basic concepts of KraftShade, you can:

- Create your [First Effect](./first-effect) with KraftShade
- Explore the [Core Components](../core-components) in more detail
- Learn about the [Pipeline DSL](../pipeline-dsl) for building complex effects
- Check out the [Built-in Shaders](../built-in-shaders) for ready-to-use effects
