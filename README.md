[![kraftshade](https://img.shields.io/maven-central/v/com.cardinalblue/kraftshade.svg?label=kraftshade)](https://central.sonatype.com/artifact/com.cardinalblue/kraftshade) [![kraftshade-compose](https://img.shields.io/maven-central/v/com.cardinalblue/kraftshade-compose.svg?label=kraftshade-compose)](https://central.sonatype.com/artifact/com.cardinalblue/kraftshade-compose)

📚 [Documentation](https://cardinalblue.github.io/android-kraft-shade/docs/intro)

# KraftShade

KraftShade is a modern, high-performance OpenGL ES graphics rendering library for Android, designed to provide a type-safe, Kotlin-first abstraction over OpenGL ES 2.0. Built with coroutines support and a focus on developer experience, KraftShade makes complex graphics operations simple while maintaining flexibility and performance.

## Table of Contents
- [Why Another Graphics Library?](#why-another-graphics-library)
- [Features & Goals](#features--goals)
- [Installation](#installation)
- [Core Components](#core-components)
- [Pipeline DSL](#pipeline-dsl)
- [Effect Serialization](#effect-serialization)
- [Support Status](#support-status)
- [Usage](#usage)
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

## Installation

KraftShade is available on Maven Central. You can integrate it into your project using Gradle.

### Gradle (build.gradle or build.gradle.kts)

#### Kotlin DSL (build.gradle.kts)

```kotlin
dependencies {
    // find the latest version from the badge at the beginning of this README file

    // Core library
    implementation("com.cardinalblue:kraftshade:latest_version")
    // Optional: Jetpack Compose integration
    implementation("com.cardinalblue:kraftshade-compose:latest_version")
}
```

### Version Catalog (libs.versions.toml)

If you're using Gradle's version catalog feature, add the following to your `libs.versions.toml` file:

```toml
[versions]
# find the latest version from the badge at the beginning of this README file
kraftshade = "..."

[libraries]
kraftshade-core = { group = "com.cardinalblue", name = "kraftshade", version.ref = "kraftshade" }
kraftshade-compose = { group = "com.cardinalblue", name = "kraftshade-compose", version.ref = "kraftshade" }
```

Then in your module's build.gradle.kts file:

```kotlin
dependencies {
    implementation(libs.kraftshade.core)
    implementation(libs.kraftshade.compose)
}
```

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
        step(SaturationKraftShader()) {
            saturation = sampledInput { saturation }
        }

        step(HueKraftShader()) {
            setHueInDegree(sampledInput { hue })
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
        targetBuffer = step1Result
    ) { shader ->
        shader.setParameter(input1.get())
    }

    step(
        Shader2(),
        targetBuffer = step2Result
    ) { shader ->
        shader.setParameter(input2.get())
    }

    step(
        BlendingShader().apply {
            mixRatio = 0.5f
        },
        targetBuffer = blendResult
    ) { shader ->
        shader.setTexture1(texture1)
        shader.setTexture2(texture2)
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
        graphStep() { inputTexture ->
            // create buffer references for intermediate results
            // graph steps...
            // write to graphTargetBuffer which is just one of the ping-pong buffers from serialSteps scope that's provided through the child scope
        }
    }
}
```

## Effect Serialization

KraftShade provides a powerful serialization system that allows you to convert complex effect pipelines into JSON format and reconstruct them later. This enables effect sharing, storage.

**Important**: The serialization system creates a **snapshot** of your pipeline at the time of serialization, capturing the static structure and parameter values that were active during serialization.

### Core Serialization Components

#### EffectSerializer
The [`EffectSerializer`](kraft-shade/src/main/java/com/cardinalblue/kraftshade/pipeline/serialization/EffectSerializer.kt:138) converts pipeline setups into JSON format:

```kotlin
class EffectSerializer(private val context: Context, private val size: GlSize) {
    suspend fun serialize(
        block: suspend GraphPipelineSetupScope.() -> Unit,
    ): String
}
```

#### SerializedEffect
The [`SerializedEffect`](kraft-shade/src/main/java/com/cardinalblue/kraftshade/pipeline/serialization/EffectSerializer.kt:209) reconstructs effects from JSON:

```kotlin
class SerializedEffect(
    private val json: String,
    private val getTextureProvider: (String) -> TextureProvider?,
) {
    suspend fun applyTo(pipeline: Pipeline, targetBuffer: GlBufferProvider)
}
```

### Serialization Workflow

The serialization process follows this workflow:

```mermaid
graph LR
    A[Pipeline Setup] --> B[EffectSerializer]
    B --> C[JSON String]
    C --> D[SerializedEffect]
    D --> E[Reconstructed Pipeline]
    F[Texture Providers] --> D
```

### How Serialization Works

1. **Capture Pipeline Structure**: The serializer executes your pipeline setup to collect all shader steps
2. **Extract Shader Information**: For each step, it captures:
   - Shader class name and properties
   - Input texture references
   - Output buffer references
3. **Generate JSON**: Creates a structured JSON representation of the pipeline
4. **Reconstruction**: Uses the JSON and provided texture providers to rebuild the exact same pipeline

### Usage Example

Here's a complete example showing how to serialize and deserialize an effect:

```kotlin
// Define your effect pipeline
suspend fun createVintageEffect(
    inputImage: Bitmap,
    maskImage: Bitmap
): suspend GraphPipelineSetupScope.() -> Unit = {
    val pipelineModifier = VintageGlowPipelineModifier(
        equalizedImage = sampledBitmapTextureProvider("input") { inputImage },
        faceMask = sampledBitmapTextureProvider("mask") { maskImage },
        contrast = sampledInput { 1.2f },
        brightness = sampledInput { 0.1f },
        intensity = sampledInput { 0.8f }
    )
    
    pipelineModifier.apply {
        addStep(
            inputTexture = sampledBitmapTextureProvider("input") { inputImage },
            outputBuffer = graphTargetBuffer
        )
    }
}

// Serialize the effect
val context = LocalContext.current
val serializer = EffectSerializer(context, GlSize(1024, 1024))
val jsonString = serializer.serialize(createVintageEffect(inputBitmap, maskBitmap))

// Deserialize and apply the effect
val serializedEffect = SerializedEffect(json = jsonString) { textureId ->
    when (textureId) {
        "input" -> sampledBitmapTextureProvider("input") { inputBitmap }
        "mask" -> sampledBitmapTextureProvider("mask") { maskBitmap }
        else -> null
    }
}

// Apply to your view
state.setEffect { targetBuffer ->
    createEffectExecutionProvider(serializedEffect)
}
```

### Texture Provider Mapping

When deserializing, you need to provide a mapping function that resolves texture names to [`TextureProvider`](kraft-shade/src/main/java/com/cardinalblue/kraftshade/shader/buffer/TextureProvider.kt) instances:

```kotlin
val serializedEffect = SerializedEffect(json = jsonString) { textureId ->
    when (textureId) {
        "input" -> sampledBitmapTextureProvider("input") { inputBitmap }
        "foreground_mask" -> sampledBitmapTextureProvider("mask") { maskBitmap }
        else -> {
            // Handle asset textures or other resources
            val bitmap = context.assets.open(textureId).use {
                BitmapFactory.decodeStream(it)
            }
            bitmap.asTexture()
        }
    }
}
```

### JSON Structure

The serialized JSON contains an array of shader nodes, each with:

```json
[
  {
    "shaderClassName": "com.cardinalblue.kraftshade.shader.SaturationKraftShader",
    "shaderProperties": {
      "saturation": 1.5
    },
    "inputs": ["input"],
    "output": "BufferReference@12345"
  },
  {
    "shaderClassName": "com.cardinalblue.kraftshade.shader.BrightnessKraftShader",
    "shaderProperties": {
      "brightness": 0.2
    },
    "inputs": ["BufferReference@12345"],
    "output": "BufferReference@67890"
  }
]
```

### Best Practices

1. **Consistent Naming**: Use consistent texture names (like `"input"`, `"foreground_mask"`) for better maintainability
2. **Size Considerations**: Choose appropriate [`GlSize`](kraft-shade/src/main/java/com/cardinalblue/kraftshade/model/GlSize.kt) for serialization based on your target use case
3. **Error Handling**: Always provide fallback texture providers for missing resources
4. **Testing**: Compare original and deserialized effects to ensure consistency

### Limitations

- **Static Snapshot Only**: Serialization captures fixed parameter values at serialization time - dynamic inputs like [`SampledInput<T>`](kraft-shade/src/main/java/com/cardinalblue/kraftshade/pipeline/input/SampledInput.kt), animations, or time-based effects are not preserved
- Sub-pipelines are not yet supported in serialization
- Complex input types beyond textures may require custom handling
- Shader properties must be serializable to JSON (primitives, arrays)

### Future Improvements

- **SerializableKraftShader Interface**: Implement a more flexible serialization system by defining a `SerializableKraftShader` interface. This would allow shaders to provide their own serialization information instead of the current approach where the serializer needs to recognize specific shader types like [`TwoTextureInputKraftShader`](kraft-shade/src/main/java/com/cardinalblue/kraftshade/shader/TwoTextureInputKraftShader.kt). This design would give better control over serialization behavior and make [`KraftShader`](kraft-shade/src/main/java/com/cardinalblue/kraftshade/shader/KraftShader.kt) more flexible.

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
- [x] ExposureKraftShader (GPUImageExposureFilter)
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
- [x] LevelsKraftShader (GPUImageLevelsFilter)
- [x] SolarizeKraftShader (GPUImageSolarizeFilter)
- [x] FalseColorKraftShader (GPUImageFalseColorFilter)
- [x] MonochromeKraftShader (GPUImageMonochromeFilter)
- [x] OpacityKraftShader (GPUImageOpacityFilter)
- [x] PosterizeKraftShader (GPUImagePosterizeFilter)
- [x] SepiaToneKraftShader (GPUImageSepiaToneFilter)
- [x] ToneCurveKraftShader (GPUImageToneCurveFilter)
- [x] VibranceKraftShader (GPUImageVibranceFilter)
- [x] VignetteKraftShader (GPUImageVignetteFilter)

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
- [x] SobelEdgeDetectionKraftShader (GPUImageSobelEdgeDetectionFilter)
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
- [x] HardLightBlendKraftShader (GPUImageHardLightBlendFilter)
- [x] SimpleMixtureBlendKraftShader
- [x] AddBlendKraftShader (GPUImageAddBlendFilter)

### Blur & Distortion
- [x] CircularBlurKraftShader
- [ ] BilateralBlurKraftShader (GPUImageBilateralBlurFilter)
- [ ] BoxBlurKraftShader (GPUImageBoxBlurFilter)
- [x] BulgeDistortionKraftShader (GPUImageBulgeDistortionFilter)
- [ ] GaussianBlurKraftShader (GPUImageGaussianBlurFilter)
- [ ] GlassSphereKraftShader (GPUImageGlassSphereFilter)
- [ ] SphereRefractionKraftShader (GPUImageSphereRefractionFilter)
- [x] SwirlKraftShader (GPUImageSwirlFilter)
- [x] ZoomBlurKraftShader (GPUImageZoomBlurFilter)

### Artistic Effects
- [x] SharpenKraftShader (GPUImageSharpenFilter)
- [x] KuwaharaKraftShader (GPUImageKuwaharaFilter)

### Coming Soon

#### Blend Modes
- [ ] ColorBlendKraftShader (GPUImageColorBlendFilter)
- [ ] ColorBurnBlendKraftShader (GPUImageColorBurnBlendFilter)
- [ ] ColorDodgeBlendKraftShader (GPUImageColorDodgeBlendFilter)
- [ ] DarkenBlendKraftShader (GPUImageDarkenBlendFilter)
- [ ] DifferenceBlendKraftShader (GPUImageDifferenceBlendFilter)
- [ ] DissolveBlendKraftShader (GPUImageDissolveBlendFilter)
- [ ] DivideBlendKraftShader (GPUImageDivideBlendFilter)
- [ ] ExclusionBlendKraftShader (GPUImageExclusionBlendFilter)
- [ ] HueBlendKraftShader (GPUImageHueBlendFilter)
- [ ] LightenBlendKraftShader (GPUImageLightenBlendFilter)
- [ ] LinearBurnBlendKraftShader (GPUImageLinearBurnBlendFilter)
- [ ] LuminosityBlendKraftShader (GPUImageLuminosityBlendFilter)
- [ ] OverlayBlendKraftShader (GPUImageOverlayBlendFilter)
- [ ] SaturationBlendKraftShader (GPUImageSaturationBlendFilter)
- [ ] SoftLightBlendKraftShader (GPUImageSoftLightBlendFilter)
- [ ] SubtractBlendKraftShader (GPUImageSubtractBlendFilter)

#### Blur & Distortion
- [ ] BilateralBlurKraftShader (GPUImageBilateralBlurFilter)
- [ ] BoxBlurKraftShader (GPUImageBoxBlurFilter)
- [ ] BulgeDistortionKraftShader (GPUImageBulgeDistortionFilter)
- [ ] GaussianBlurKraftShader (GPUImageGaussianBlurFilter)
- [ ] GlassSphereKraftShader (GPUImageGlassSphereFilter)
- [ ] SphereRefractionKraftShader (GPUImageSphereRefractionFilter)

#### Artistic Effects
- [ ] CGAColorspaceKraftShader (GPUImageCGAColorspaceFilter)
- [ ] HalftoneKraftShader (GPUImageHalftoneFilter)
- [ ] RGBDilationKraftShader (GPUImageRGBDilationFilter)
- [ ] SketchKraftShader (GPUImageSketchFilter)
- [ ] SmoothToonKraftShader (GPUImageSmoothToonFilter)

#### Edge Detection & Processing
- [ ] NonMaximumSuppressionKraftShader (GPUImageNonMaximumSuppressionFilter)
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
    
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var image by remember { mutableStateOf<Bitmap?>(null) }

    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current

    // Load image and set aspect ratio
    LaunchedEffect(Unit) {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sample_image)
        image = bitmap
        aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
    }

    // Set effect
    LaunchedEffect(Unit) {
        state.setEffect { targetBuffer ->
            pipeline(targetBuffer) {
                serialSteps(
                    inputTexture = sampledBitmapTextureProvider { image.value },
                    targetBuffer = targetBuffer,
                ) {
                    step(SaturationKraftShader()) {
                        saturation = sampledInput { saturation }
                    }
                    step(BrightnessKraftShader()) {
                        brightness = sampledInput { brightness }
                    }
                }
            }
        }
    }

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
                value = brightness,
                onValueChange = {
                    brightness = it
                    state.requestRender()
                },
                valueRange = 0f..1f
            )

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
Copyright 2025 Cardinal Blue

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
