---
sidebar_position: 4
---

import { HowToSchema } from '@site/src/components/JsonLdSchema';

<HowToSchema
  name="Create Your First Effect with KraftShade"
  description="Step-by-step guide to creating a vintage photo effect using KraftShade's pipeline system with multiple shaders"
  url="https://cardinalblue.github.io/android-kraft-shade/docs/getting-started/first-effect"
  totalTime="PT30M"
  steps={[
    "Set up the project with KraftShade dependencies and sample images",
    "Create the UI with Jetpack Compose and effect controls",
    "Build the effect pipeline with saturation, sepia, vignette, and grain effects",
    "Create a custom noise overlay shader for film grain effect",
    "Test and adjust the effect parameters in real-time"
  ]}
  supply={[
    "Android Studio",
    "KraftShade library",
    "Sample image resources",
    "Jetpack Compose"
  ]}
/>

# First Effect

In this guide, we'll walk through creating a more complex effect using KraftShade. We'll build a vintage photo effect that combines multiple shaders to achieve a stylized look.

## Prerequisites

Before starting, make sure you:

- Have completed the [Installation](./installation) steps
- Understand the [Basic Concepts](./basic-concepts) of KraftShade
- Have gone through the [Quick Start Guide](./quick-start-guide)

## Project Setup

First, let's set up our project with the necessary dependencies and resources:

1. Add KraftShade dependencies to your project (see [Installation](./installation))
2. Add sample images to your project resources
3. Set up logging in your Application class

## Creating a Vintage Photo Effect

We'll create a vintage photo effect that combines several shaders:

1. Saturation adjustment
2. Sepia tone
3. Vignette effect
4. Grain overlay

### Step 1: Set Up the UI

Let's start by setting up a simple UI to display our effect. We'll use Jetpack Compose for this example:

```kotlin
@Composable
fun VintageEffectDemo() {
    val state = rememberKraftShadeEffectState()
    
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var image by remember { mutableStateOf<Bitmap?>(null) }
    
    // Parameters for our effect
    var saturation by remember { mutableFloatStateOf(0.7f) }
    var sepiaIntensity by remember { mutableFloatStateOf(0.8f) }
    var vignetteStart by remember { mutableFloatStateOf(0.3f) }
    var vignetteEnd by remember { mutableFloatStateOf(0.75f) }
    var grainIntensity by remember { mutableFloatStateOf(0.1f) }
    
    val context = LocalContext.current
    
    // Load image and set aspect ratio
    LaunchedEffect(Unit) {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sample_image)
        image = bitmap
        aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
    }
    
    // Set up the UI layout
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
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                Text("Saturation: ${String.format("%.1f", saturation)}")
                Slider(
                    value = saturation,
                    onValueChange = {
                        saturation = it
                        state.requestRender()
                    },
                    valueRange = 0f..1f
                )
            }
            
            item {
                Text("Sepia Intensity: ${String.format("%.1f", sepiaIntensity)}")
                Slider(
                    value = sepiaIntensity,
                    onValueChange = {
                        sepiaIntensity = it
                        state.requestRender()
                    },
                    valueRange = 0f..1f
                )
            }
            
            item {
                Text("Vignette Start: ${String.format("%.1f", vignetteStart)}")
                Slider(
                    value = vignetteStart,
                    onValueChange = {
                        vignetteStart = it
                        state.requestRender()
                    },
                    valueRange = 0f..1f
                )
            }
            
            item {
                Text("Vignette End: ${String.format("%.1f", vignetteEnd)}")
                Slider(
                    value = vignetteEnd,
                    onValueChange = {
                        vignetteEnd = it
                        state.requestRender()
                    },
                    valueRange = 0f..1f
                )
            }
            
            item {
                Text("Grain Intensity: ${String.format("%.1f", grainIntensity)}")
                Slider(
                    value = grainIntensity,
                    onValueChange = {
                        grainIntensity = it
                        state.requestRender()
                    },
                    valueRange = 0f..0.5f
                )
            }
        }
    }
    
    // Apply the effect
    LaunchedEffect(Unit) {
        state.setEffect { targetBuffer ->
            createVintageEffect(targetBuffer, image, saturation, sepiaIntensity, vignetteStart, vignetteEnd, grainIntensity)
        }
    }
}
```

### Step 2: Create the Effect Pipeline

Now, let's implement the `createVintageEffect` function that will set up our pipeline:

```kotlin
private fun createVintageEffect(
    targetBuffer: GlBufferProvider,
    image: Bitmap?,
    saturation: Float,
    sepiaIntensity: Float,
    vignetteStart: Float,
    vignetteEnd: Float,
    grainIntensity: Float
): Pipeline? {
    if (image == null) return null
    
    return pipeline(targetBuffer) {
        // Create buffer references for intermediate results
        val (saturationResult, sepiaResult, vignetteResult) = createBufferReferences(
            "saturation_result",
            "sepia_result",
            "vignette_result"
        )
        
        // Step 1: Apply saturation adjustment
        step(
            SaturationKraftShader(),
            inputTexture = sampledBitmapTextureProvider { image },
            targetBuffer = saturationResult
        ) { shader ->
            shader.saturation = sampledInput { saturation }
        }
        
        // Step 2: Apply sepia tone
        step(
            SepiaToneKraftShader(),
            inputTexture = saturationResult,
            targetBuffer = sepiaResult
        ) { shader ->
            shader.intensity = sampledInput { sepiaIntensity }
        }
        
        // Step 3: Apply vignette effect
        step(
            VignetteKraftShader(),
            inputTexture = sepiaResult,
            targetBuffer = vignetteResult
        ) { shader ->
            shader.vignetteStart = sampledInput { vignetteStart }
            shader.vignetteEnd = sampledInput { vignetteEnd }
        }
        
        // Step 4: Apply grain effect and render to final target
        step(
            NoiseOverlayKraftShader(),
            inputTexture = vignetteResult,
            targetBuffer = targetBuffer
        ) { shader ->
            shader.intensity = sampledInput { grainIntensity }
        }
    }
}
```

### Step 3: Create a Custom Noise Overlay Shader

For the grain effect, we'll create a custom shader. This demonstrates how to extend KraftShade with your own shaders:

```kotlin
class NoiseOverlayKraftShader : TextureInputKraftShader() {
    var intensity by glUniform1f("u_intensity", 0.1f)
    
    override val fragmentShaderSource: String = """
        precision mediump float;
        varying vec2 v_texCoord;
        uniform sampler2D s_texture;
        uniform float u_intensity;
        
        // Simple pseudo-random function
        float random(vec2 st) {
            return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
        }
        
        void main() {
            vec4 color = texture2D(s_texture, v_texCoord);
            float noise = random(v_texCoord) * u_intensity;
            
            // Add noise to each channel
            color.rgb += noise;
            
            gl_FragColor = color;
        }
    """
}
```

## Understanding the Pipeline

Let's break down what's happening in our vintage effect pipeline:

1. **Buffer References**: We create named buffer references for intermediate results. This helps with debugging and makes the pipeline more readable.

2. **Serial Processing**: We process the image through a series of steps:
   - Saturation adjustment: Reduces the color saturation for a faded look
   - Sepia tone: Adds a warm, brownish tint characteristic of old photos
   - Vignette: Darkens the edges of the image
   - Noise overlay: Adds film grain for an authentic vintage feel

3. **Dynamic Parameters**: Each shader has parameters that can be adjusted in real-time using the sliders in our UI.

## Extending the Effect

You can extend this effect in several ways:

### Adding Color Grading with LookUpTableKraftShader

```kotlin
// Add after the vignette step
step(
    LookUpTableKraftShader(),
    inputTexture = vignetteResult,
    targetBuffer = lookupResult
) { shader ->
    shader.intensity = sampledInput { lookupIntensity }
    shader.lookupTexture = sampledBitmapTextureProvider { lookupBitmap }
}
```

### Adding a Subtle Blur

```kotlin
// Add after the saturation step
step(
    CircularBlurKraftShader(),
    inputTexture = saturationResult,
    targetBuffer = blurResult
) { shader ->
    shader.blurRadius = sampledInput { blurRadius }
}
```

## Using the Graph Pipeline for Complex Effects

For more complex effects, you can use the graph pipeline to create non-linear processing flows:

```kotlin
pipeline(targetBuffer) {
    // Create buffer references
    val (baseProcessed, overlay, blended) = createBufferReferences(
        "base_processed",
        "overlay",
        "blended"
    )
    
    // Process the base image
    step(
        SaturationKraftShader(),
        inputTexture = sampledBitmapTextureProvider { baseImage },
        targetBuffer = baseProcessed
    ) { shader ->
        shader.saturation = sampledInput { saturation }
    }
    
    // Create an overlay effect
    step(
        EdgeDetectionKraftShader(),
        inputTexture = sampledBitmapTextureProvider { baseImage },
        targetBuffer = overlay
    ) { shader ->
        shader.intensity = sampledInput { edgeIntensity }
    }
    
    // Blend the two results
    step(
        ScreenBlendKraftShader(),
        targetBuffer = blended
    ) { shader ->
        shader.setTexture1(baseProcessed)
        shader.setTexture2(overlay)
    }
    
    // Final adjustments
    step(
        ContrastKraftShader(),
        inputTexture = blended,
        targetBuffer = targetBuffer
    ) { shader ->
        shader.contrast = sampledInput { contrast }
    }
}
```

## Serializing Your Effect

Once you've created an effect you like, you can serialize it for later use:

```kotlin
// Serialize the effect
val serializer = EffectSerializer(context, GlSize(1024, 1024))
val jsonString = serializer.serialize { targetBuffer ->
    createVintageEffect(targetBuffer, image, 0.7f, 0.8f, 0.3f, 0.75f, 0.1f)
}

// Save the JSON string to a file or database
context.openFileOutput("vintage_effect.json", Context.MODE_PRIVATE).use {
    it.write(jsonString.toByteArray())
}

// Later, deserialize and apply the effect
val json = context.openFileInput("vintage_effect.json").bufferedReader().use { it.readText() }
val serializedEffect = SerializedEffect(json) { textureId ->
    when (textureId) {
        "input" -> sampledBitmapTextureProvider("input") { image }
        else -> null
    }
}

state.setEffect { targetBuffer ->
    createEffectExecutionProvider(serializedEffect)
}
```

## Next Steps

Now that you've created your first complex effect with KraftShade, you can:

- Explore the [Core Components](../core-components) in more detail
- Learn about the [Pipeline DSL](../pipeline-dsl) for building even more complex effects
- Check out the [Built-in Shaders](../built-in-shaders) for more ready-to-use effects
- Dive into Advanced Topics like custom shader development and performance optimization
