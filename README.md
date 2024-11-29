# KraftShade

KraftShade is a modern, high-performance OpenGL ES graphics rendering library for Android, designed to provide a type-safe, Kotlin-first abstraction over OpenGL ES 2.0. Built with coroutines support and a focus on developer experience, KraftShade makes complex graphics operations simple while maintaining flexibility and performance.

## Installation

```gradle
dependencies {
    implementation 'com.cardinalblue:kraftshade:1.0.0'
}
```

## Why Another Graphics Library?

While GPUImage has been a popular choice for Android graphics processing, it comes with several limitations:

1. Java-centric Design
   - Lacks Kotlin idioms and modern language features
   - No coroutines support for thread (EGLContext) based operations (using GLThread)

2. Inflexible Pipeline
   - Rigid filter chain architecture (GPUImageFilterGroup)
   - Limited to single serial pipeline due to Bitmap-based texture inputs instead of texture IDs for multiple textures
   - No support for memory optimizations like ping-pong buffers

3. Development Challenges
   - Insufficient error handling and debugging capabilities
   - Limited control over resource allocation
   - Insufficient development tooling
   - Limited View component support
   - No active maintenance since 2021


## Goals

KraftShade aims to address these limitations with:

1. Modern Architecture
   - Kotlin-first design with coroutines support
   - Easy to use DSL for pipeline construction

2. Flexible Pipeline
   - Composable effects
   - Support for complex multi-pass rendering
   - Efficient texture and buffer management
   - On/Off-screen rendering support

3. Developer Experience
   - Flexible View components & Compose integration
   - Flexible input system for effect adjustment and re-rendering including animated effects
   - Debugging tools and utilities

4. Performance
   - Minimal overhead
   - Smart resource management
   - Optimized rendering pipeline

## Key Features and Core Components

### GlEnv (Graphics Environment)
- Encapsulates all OpenGL ES environment setup in a single class
- Creates a dedicated thread dispatcher and binds the GLES context to that thread
- Ensures all OpenGL operations run in the correct context

### Shader System
- `KraftShader`: Base shader implementation with modern Kotlin features
- `GlUniformDelegate`: Smart uniform handling
  * Supports multiple types (Int, Float, FloatArray, GlMat2/3/4)
  * Deferred location query and value setting
  * Optional uniforms support (useful for base class implementations)
  * Automatic GLES API selection for value binding
- `KraftShaderTextureInput`: Simplified texture input management
- Debug mode support with detailed logging and error tracking
- Bypassable shader support for conditional effect application

### Pipeline Running Flow (per frame)
1. Input updates (sample from ViewModel or MutableState in Compose UI)
2. Reset `PipelineRunContext` which is a state you can access in the pipeline. During the execution of the steps, you can abort the running of the pipeline using it. Debug functionalities can be added to this state later.
3. Iterate through steps in the pipeline and render to either intermediate buffers or the final target buffer
   - Intermediate buffers are TextureBuffers provided by TextureBufferPool
   - For the last step, the target buffer is usually the target buffer of the pipeline (window surface or a created TextureBuffer for getting bitmaps off-screen)
   - Flow for running one step (shader)
     1. Get values from input and set them as parameters of the shader
     2. Run the shader to render to the target buffer for the step
     3. Automatic recycling buffers not referenced anymore back to the pool

### Input System
- `Input<T>`: Base input type
- `SampledInput<T>`: Dynamic input handling
  * `TimeInput`: Time-based animations
  * `MappedInput`: Value transformations
- Supports complex animations and transitions
- Example of mapped input:
  ```kotlin
  fun SampledInput<Float>.bounceBetween(value1: Float, value2: Float): SampledInput<Float> {
      val min = min(value1, value2)
      val max = max(value1, value2)
      val interval = max - min
      return map { value ->
          val intervalValue = value % (interval * 2f)
          if (intervalValue < interval) {
              intervalValue + value1
          } else {
              2f * interval - intervalValue
          }
      }
  }
  ```

### View Components
- Android Views:
  * `KraftTextureView`: Base OpenGL rendering view
  * `KraftEffectTextureView`: Effect-enabled view
  * `AnimatedKraftTextureView`: Animation support with Choreographer
- Jetpack Compose Integration:
  * `KraftShadeView`: AndroidView wrapper for KraftTextureView
  * `KraftShadeEffectView`: AndroidView wrapper for KraftEffectTextureView
  * `KraftShadeAnimatedView`: AndroidView wrapper for AnimatedKraftTextureView

### Pipeline DSL
KraftShade provides a powerful DSL for setting up rendering pipelines with different levels of complexity:

#### Serial Pipeline
Simple linear processing chain, similar to GPUImageFilterGroup:
```kotlin
pipeline(windowSurface) {
    serialSteps(
        inputTexture = bitmap.asTexture(),
        targetBuffer = windowSurface,
    ) {
        step(
            SaturationKraftShader(),
            sampledInput { saturation }
        ) { (saturationInput) ->
            this.saturation = saturationInput.cast()
        }

        step(
            HueKraftShader(),
            sampledInput { hue }
        ) { (hueInput) ->
            setHueInDegree(hueInput.cast())
        }
    }
}
```

#### Graph Pipeline
Complex multi-pass rendering with flexible input/output connections:
```kotlin
pipeline(windowSurface) {
    // Create buffer references for intermediate results
    val (step1Result, step2Result, blendResult) = createBufferReferences(
        "graph_step1",
        "graph_step2",
        "graph_blend",
    )
    step(
        Shader1(),
        targetBuffer = step1Result,
        input1
    ) { (input1) ->
        setParameter(input1.cast())
    }

    step(
        Shader2(),
        targetBuffer = step2Result,
        input2
    ) { (input2) ->
        setParameter(input2.cast())
    }

    step(
        BlendingShader().apply {
            mixRatio = 0.5f
        },
        targetBuffer = blendResult,
        step1Result.asTextureInput(),
        step2Result.asTextureInput()
    ) { (texture1, texture2) ->
        setTexture1(texture1)
        setTexture2(texture2)
    }
}
```

#### Nested Pipeline
Combine serial and graph pipelines for complex effects:
```kotlin
pipeline(windowSurface) {
    serialSteps(
      ...
    ) {
        graphStep() { inputTextureProvider ->
            // create buffer references for intermediate results
            // graph steps...
            // write to graphTargetBuffer which is just one of the ping-pong buffers from serialSteps scope that's provided through the child scope
        }
    }
}
```

Key Features:
- Type-safe builder pattern
- Automatic buffer management
- Flexible pipeline composition
- Support for parallel processing
- Easy debugging with named buffer references
- Automatic resource cleanup

## Support status of [GPUImage for iOS](https://github.com/BradLarson/GPUImage2) shaders
- [x] Saturation
- [x] Contrast
- [x] Brightness
- [ ] Levels
- [ ] Exposure
- [ ] RGB
- [ ] RGB Diation
- [x] Hue
- [x] White Balance
- [ ] Monochrome
- [ ] False Color
- [ ] Sharpen
- [ ] Unsharp Mask
- [ ] Transform Operation
- [ ] Crop
- [x] Gamma
- [ ] Highlights and Shadows
- [x] Haze
- [ ] Sepia Tone
- [ ] Amatorka
- [ ] Miss Etikate
- [ ] Soft Elegance
- [x] Color Inversion
- [ ] Solarize
- [ ] Vibrance
- [ ] Highlight and Shadow Tint
- [ ] Luminance
- [ ] Luminance Threshold
- [ ] Average Color
- [ ] Average Luminance
- [ ] Average Luminance Threshold
- [ ] Adaptive Threshold
- [ ] Polar Pixellate
- [x] Pixellate
- [ ] Polka Dot
- [ ] Halftone
- [x] Crosshatch
- [ ] Sobel Edge Detection
- [ ] Prewitt Edge Detection
- [ ] Canny Edge Detection
- [ ] Threshold Sobel EdgeDetection
- [ ] Harris Corner Detector
- [ ] Noble Corner Detector
- [ ] Shi Tomasi Feature Detector
- [ ] Colour FAST Feature Detector
- [ ] Low Pass Filter
- [ ] High Pass Filter
- [ ] Sketch Filter
- [ ] Threshold Sketch Filter
- [ ] Toon Filter
- [ ] SmoothToon Filter
- [ ] Tilt Shift
- [ ] CGA Colorspace Filter
- [ ] Posterize
- [x] Convolution 3x3
- [x] Emboss Filter
- [x] Laplacian
- [ ] Chroma Keying
- [ ] Kuwahara Filter
- [ ] Kuwahara Radius3 Filter
- [ ] Vignette
- [ ] Gaussian Blur
- [ ] Box Blur
- [ ] Bilateral Blur
- [ ] Motion Blur
- [ ] Zoom Blur
- [ ] iOS Blur
- [ ] Median Filter
- [ ] Swirl Distortion
- [ ] Bulge Distortion
- [ ] Pinch Distortion
- [ ] Sphere Refraction
- [ ] Glass Sphere Refraction
- [ ] Stretch Distortion
- [ ] Dilation
- [ ] Erosion
- [ ] Opening Filter
- [ ] Closing Filter
- [ ] Local Binary Pattern
- [ ] Color Local Binary Pattern
- [ ] Dissolve Blend
- [ ] Chroma Key Blend
- [ ] Add Blend
- [ ] Divide Blend
- [x] Multiply Blend
- [ ] Overlay Blend
- [ ] Lighten Blend
- [ ] Darken Blend
- [ ] Color Burn Blend
- [ ] Color Dodge Blend
- [ ] Linear Burn Blend
- [x] Screen Blend
- [ ] Difference Blend
- [ ] Subtract Blend
- [ ] Exclusion Blend
- [ ] HardLight Blend
- [ ] SoftLight Blend
- [ ] Color Blend
- [ ] Hue Blend
- [ ] Saturation Blend
- [ ] Luminosity Blend
- [x] Normal Blend
- [x] Source Over Blend
- [x] Alpha Blend
- [ ] Non Maximum Suppression
- [ ] Thresholded Non Maximum Suppression
- [ ] Directional Non Maximum Suppression
- [ ] Opacity
- [ ] Weak Pixel Inclusion Filter
- [x] Color Matrix
- [x] Directional Sobel Edge Detection
- [x] Lookup (LUT)
- [ ] Tone Curve (*.acv files)

## Others
- [x] Texture 3x3
- [x] Gray Scale

## Roadmap

1. **Dynamic Shader Bypass Mechanism**
   - Implement a mechanism to map GlReference outputs to inputs, allowing steps to skip shader execution
   - Steps can decide not to run shaders based on inputs and mark output target buffers as mapped from its input buffer (the main texture of TextureInputKraftShader)
   - Modify automatic recycling mechanism to check if any BufferReferences in later steps are mapped to the current buffer to decide whether to recycle or not
   - This optimization will improve performance by avoiding unnecessary shader executions based on dynamic inputs
   - We may need a DSL scope for the block of setting up the shader for this
   - GlReference mapping state will be cleared at the beginning of each Pipeline run

## Usage

### Logging Setup

```kotlin
// Initialize in Application class
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        KraftLogger.logLevel = KraftLogger.Level.DEBUG
        KraftLogger.throwOnError = true
    }
}
```

### Custom Effects

Create custom effects by extending `KraftShader`:

```kotlin
class CustomEffect : KraftShader() {
    private var intensity by GlUniformDelegate("intensity")

    override fun loadFragmentShader(): String = """
        // Your shader code here
    """.trimIndent()

    fun setIntensity(value: Float) {
        // no need to get the location anymore. GlUniformDelegate will handle it.
        intensity = value
    }
}
```

## Acknowledgments

Special thanks to [GPUImage for Android](https://github.com/cats-oss/android-gpuimage) which inspired the development of KraftShade.

## License

```
Copyright 2024 Cardinal Blue

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
