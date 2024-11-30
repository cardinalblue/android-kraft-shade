# KraftShade

KraftShade is a modern, high-performance OpenGL ES graphics rendering library for Android, designed to provide a type-safe, Kotlin-first abstraction over OpenGL ES 2.0. Built with coroutines support and a focus on developer experience, KraftShade makes complex graphics operations simple while maintaining flexibility and performance.

## Table of Contents
- [Why Another Graphics Library?](#why-another-graphics-library)
- [Features & Goals](#features--goals)
- [Core Components](#core-components)
- [Pipeline DSL](#pipeline-dsl)
- [Support Status](#support-status)
- [Usage](#usage)
- [Installation](#installation)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

## Why Another Graphics Library?

While GPUImage has been a popular choice for Android graphics processing, it comes with several limitations:

1. Java-centric Design
   - Lacks Kotlin idioms and modern language features
   - No coroutines support for thread (EGLContext) based operations (using GLThread)

2. Inflexible Pipeline
   - Rigid filter chain architecture (GPUImageFilterGroup)
   - Limited to single serial pipeline due to Bitmap-based texture inputs
   - No support for memory optimizations like ping-pong buffers

3. Development Challenges
   - Insufficient error handling and debugging capabilities
   - Limited control over resource allocation
   - Insufficient development tooling
   - Limited View component support
   - No active maintenance since 2021

## Features & Goals

KraftShade addresses these limitations with:

1. Modern Architecture
   - Kotlin-first design with coroutines support
   - Easy to use DSL for pipeline construction
   - Type-safe builder pattern
   - Automatic resource cleanup

2. Flexible Pipeline
   - Composable effects
   - Support for complex multi-pass rendering
   - Efficient texture and buffer management
   - On/Off-screen rendering support
   - Support for parallel processing
   - Automatic buffer management

3. Developer Experience
   - Flexible View components & Compose integration
   - Flexible input system for effect adjustment and re-rendering
   - Debugging tools with named buffer references
   - Comprehensive error handling

4. Performance
   - Minimal overhead
   - Smart resource management
   - Optimized rendering pipeline

## Core Components

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
2. Reset `PipelineRunContext` which is a state you can access in the pipeline
3. Iterate through steps in the pipeline and render to either intermediate buffers or the final target buffer
   - Intermediate buffers are TextureBuffers provided by TextureBufferPool
   - For the last step, the target buffer is usually the target surface
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

## Pipeline DSL

KraftShade provides a powerful DSL for setting up rendering pipelines with different levels of complexity:

### Serial Pipeline
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

### Graph Pipeline
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

### Nested Pipeline
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

## Support Status

### Base Shaders
- [x] TextureInputKraftShader (single texture input)
- [x] ThreeTextureInputKraftShader
- [x] TwoTextureInputKraftShader (GPUImageTwoInputFilter)
- [x] MixBlendKraftShader (GPUImageMixBlendFilter)
- [x] SingleDirectionForTwoPassSamplingKraftShader (GPUImageTwoPassTextureSamplingFilter)

### Functional Shaders
- [x] BypassableTextureInputKraftShader
- [x] BypassableTwoTextureInputKraftShader

### Color Effects
- [x] SaturationKraftShader (GPUImageSaturationFilter)
- [x] ContrastKraftShader (GPUImageContrastFilter)
- [x] BrightnessKraftShader (GPUImageBrightnessFilter)
- [x] HueKraftShader (GPUImageHueFilter)
- [x] WhiteBalanceKraftShader (GPUImageWhiteBalanceFilter)
- [x] GammaKraftShader (GPUImageGammaFilter)
- [x] ColorInversionKraftShader
- [x] GrayscaleKraftShader (GPUImageGrayscaleFilter)
- [x] HighlightShadowKraftShader (GPUImageHighlightShadowFilter)
- [x] ColorMatrixKraftShader (GPUImageColorMatrixFilter)
  - Additionally supports color offset
- [x] LookUpTableKraftShader (GPUImageLookupFilter)
- [x] ColorMappingKraftShader
  - For mapping specific colors to other colors
  - At most 8 color mappings. If you need to map more colors, you can use it multiple times.
- [x] RGBKraftShader (GPUImageRGBFilter)
- [x] ColorBalanceKraftShader (GPUImageColorBalanceFilter)

### Alpha & Transparency Effects
- [x] HazeKraftShader (GPUImageHazeFilter)
- [x] AlphaInvertKraftShader
- [x] ApplyAlphaMaskKraftShader

### Texture & Artistic Effects
- [x] CrosshatchKraftShader (GPUImageCrosshatchFilter)
- [x] PixelationKraftShader (GPUImagePixelationFilter)
- [x] Sample3x3KraftShader (GPUImage3x3TextureSamplingFilter)
- [x] ToonKraftShader (GPUImageToonFilter)

### Edge Detection
- [x] DirectionalSobelEdgeDetectionKraftShader (GPUImageDirectionalSobelEdgeDetectionFilter)
- [x] LaplacianKraftShader (GPUImageLaplacianFilter)
- [x] LaplacianMagnitudeKraftShader

### Convolution & Mask Processing
- [x] Convolution3x3KraftShader (GPUImage3x3ConvolutionFilter)
- [x] Convolution3x3WithColorOffsetKraftShader
- [x] EmbossKraftShader (GPUImageEmbossFilter)
- [x] DilationKraftShader (GPUImageDilationFilter)
- [x] ErosionKraftShader

### Blending Modes
- [x] MultiplyBlendKraftShader (GPUImageMultiplyBlendFilter)
- [x] ScreenBlendKraftShader (GPUImageScreenBlendFilter)
- [x] NormalBlendKraftShader (GPUImageNormalBlendFilter)
- [x] SourceOverBlendKraftShader (GPUImageSourceOverBlendFilter)
- [x] AlphaBlendKraftShader (GPUImageAlphaBlendFilter)
- [x] SimpleMixtureBlendKraftShader

### Blur & Distortion
- [x] CircularBlurKraftShader

### Coming Soon

#### Blend Modes
- [ ] AddBlendKraftShader (GPUImageAddBlendFilter)
- [ ] ColorBlendKraftShader (GPUImageColorBlendFilter)
- [ ] ColorBurnBlendKraftShader (GPUImageColorBurnBlendFilter)
- [ ] ColorDodgeBlendKraftShader (GPUImageColorDodgeBlendFilter)
- [ ] DarkenBlendKraftShader (GPUImageDarkenBlendFilter)
- [ ] DifferenceBlendKraftShader (GPUImageDifferenceBlendFilter)
- [ ] DissolveBlendKraftShader (GPUImageDissolveBlendFilter)
- [ ] DivideBlendKraftShader (GPUImageDivideBlendFilter)
- [ ] ExclusionBlendKraftShader (GPUImageExclusionBlendFilter)
- [ ] HardLightBlendKraftShader (GPUImageHardLightBlendFilter)
- [ ] HueBlendKraftShader (GPUImageHueBlendFilter)
- [ ] LightenBlendKraftShader (GPUImageLightenBlendFilter)
- [ ] LinearBurnBlendKraftShader (GPUImageLinearBurnBlendFilter)
- [ ] LuminosityBlendKraftShader (GPUImageLuminosityBlendFilter)
- [ ] OverlayBlendKraftShader (GPUImageOverlayBlendFilter)
- [ ] SaturationBlendKraftShader (GPUImageSaturationBlendFilter)
- [ ] SoftLightBlendKraftShader (GPUImageSoftLightBlendFilter)
- [ ] SubtractBlendKraftShader (GPUImageSubtractBlendFilter)

#### Color Effects
- [ ] ExposureKraftShader (GPUImageExposureFilter)
- [ ] FalseColorKraftShader (GPUImageFalseColorFilter)
- [ ] LevelsKraftShader (GPUImageLevelsFilter)
- [ ] MonochromeKraftShader (GPUImageMonochromeFilter)
- [ ] OpacityKraftShader (GPUImageOpacityFilter)
- [ ] PosterizeKraftShader (GPUImagePosterizeFilter)
- [ ] SepiaToneKraftShader (GPUImageSepiaToneFilter)
- [ ] SolarizeKraftShader (GPUImageSolarizeFilter)
- [ ] ToneCurveKraftShader (GPUImageToneCurveFilter)
- [ ] VibranceKraftShader (GPUImageVibranceFilter)
- [ ] VignetteKraftShader (GPUImageVignetteFilter)

#### Blur & Distortion
- [ ] BilateralBlurKraftShader (GPUImageBilateralBlurFilter)
- [ ] BoxBlurKraftShader (GPUImageBoxBlurFilter)
- [ ] BulgeDistortionKraftShader (GPUImageBulgeDistortionFilter)
- [ ] GaussianBlurKraftShader (GPUImageGaussianBlurFilter)
- [ ] GlassSphereKraftShader (GPUImageGlassSphereFilter)
- [ ] SphereRefractionKraftShader (GPUImageSphereRefractionFilter)
- [ ] SwirlKraftShader (GPUImageSwirlFilter)
- [ ] ZoomBlurKraftShader (GPUImageZoomBlurFilter)

#### Artistic Effects
- [ ] CGAColorspaceKraftShader (GPUImageCGAColorspaceFilter)
- [ ] HalftoneKraftShader (GPUImageHalftoneFilter)
- [ ] KuwaharaKraftShader (GPUImageKuwaharaFilter)
- [ ] RGBDilationKraftShader (GPUImageRGBDilationFilter)
- [ ] SharpenKraftShader (GPUImageSharpenFilter)
- [ ] SketchKraftShader (GPUImageSketchFilter)
- [ ] SmoothToonKraftShader (GPUImageSmoothToonFilter)

#### Edge Detection & Processing
- [ ] NonMaximumSuppressionKraftShader (GPUImageNonMaximumSuppressionFilter)
- [ ] SobelEdgeDetectionKraftShader (GPUImageSobelEdgeDetectionFilter)
- [ ] SobelThresholdKraftShader (GPUImageSobelThresholdFilter)
- [ ] ThresholdEdgeDetectionKraftShader (GPUImageThresholdEdgeDetectionFilter)
- [ ] WeakPixelInclusionKraftShader (GPUImageWeakPixelInclusionFilter)

#### Other
- [ ] ChromaKeyBlendKraftShader (GPUImageChromaKeyBlendFilter)
- [ ] LuminanceKraftShader (GPUImageLuminanceFilter)
- [ ] LuminanceThresholdKraftShader (GPUImageLuminanceThresholdFilter)
- [ ] TransformKraftShader (GPUImageTransformFilter)

## Usage

### Quick Start

1. Add KraftShade to your project (see [Installation](#installation))
2. Initialize logging in your Application class
3. Create your first effect in a Compose UI or Android View

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

### Compose UI Integration

Here's a simple example of using KraftShade with Jetpack Compose to create an image effect with adjustable saturation and brightness:

```kotlin
@Composable
fun ImageEffectDemo() {
    val state = rememberKraftShadeEffectState()
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image preview with effects
        Box(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            KraftShadeEffectView(
                modifier = Modifier.aspectRatio(aspectRatio),
                state = state
            )
        }

        // Effect controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Slider(
                value = saturation,
                onValueChange = { 
                    saturation = it
                    state.requestRender()
                },
                valueRange = 0f..2f
            )
            Text("Saturation: ${String.format("%.1f", saturation)}")
        }
    }
}
```

## Installation

Add KraftShade to your project's dependencies:

```gradle
dependencies {
    implementation 'com.cardinalblue:kraftshade:1.0.0'
}
```

### Version Compatibility
- Minimum Android SDK: 21 (Android 5.0)
- Target Android SDK: 34 (Android 14)
- Kotlin: 1.9.0+
- Jetpack Compose: 1.5.0+ (optional, for Compose integration)

## Roadmap

1. **Dynamic Shader Bypass Mechanism**
   - Implement a mechanism to map GlReference outputs to inputs
   - Allow steps to skip shader execution based on inputs
   - Optimize buffer recycling mechanism
   - Improve performance by avoiding unnecessary shader executions
   - Add DSL scope for shader setup
   - Implement GlReference mapping state management

## Contributing

We welcome contributions to KraftShade! Here's how you can help:

1. **Bug Reports**
   - Use the GitHub issue tracker
   - Include detailed steps to reproduce
   - Attach sample code if possible

2. **Feature Requests**
   - Open an issue with the "enhancement" label
   - Describe your use case
   - Provide example code if possible

3. **Pull Requests**
   - Fork the repository
   - Create a feature branch
   - Make your changes
   - Include tests in the demo app to show the change is working
   - Submit a pull request with a clear description

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
