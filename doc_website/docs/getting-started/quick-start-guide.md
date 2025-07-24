---
sidebar_position: 2
---

# Quick Start Guide

This guide will help you get started with KraftShade by creating a simple image effect application. We'll walk through the basic steps to set up a project and create a simple effect that adjusts the saturation and brightness of an image.

## Prerequisites

Before you begin, make sure you have:

- Completed the [Installation](./installation) steps
- Basic knowledge of Android development
- An Android project set up with Kotlin

## Setting Up Your Project

### 1. Initialize Logging

First, set up logging in your Application class to help with debugging:

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable debug logging
        KraftLogger.logLevel = KraftLogger.Level.DEBUG
        
        // Throw exceptions on errors during development
        KraftLogger.throwOnError = true
    }
}
```

Don't forget to register your Application class in your AndroidManifest.xml.

### 2. Prepare Your Resources

Add a sample image to your project's resources. For example, place an image in your `res/drawable` folder.

## Creating a Simple Effect

### Using KraftShade with Android Views

Here's a simple example using traditional Android Views:

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var kraftEffectView: KraftEffectTextureView
    private lateinit var saturationSeekBar: SeekBar
    private lateinit var brightnessSeekBar: SeekBar
    
    private var saturation = 1.0f
    private var brightness = 0.0f
    private var sampleBitmap: Bitmap? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Load sample image
        sampleBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_image)
        
        // Initialize views
        kraftEffectView = findViewById(R.id.kraft_effect_view)
        saturationSeekBar = findViewById(R.id.saturation_seek_bar)
        brightnessSeekBar = findViewById(R.id.brightness_seek_bar)
        
        // Set up seek bars
        saturationSeekBar.max = 200
        saturationSeekBar.progress = 100
        saturationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                saturation = progress / 100f
                kraftEffectView.requestRender()
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        brightnessSeekBar.max = 100
        brightnessSeekBar.progress = 50
        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brightness = (progress - 50) / 50f
                kraftEffectView.requestRender()
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Initialize the effect
        updateEffect()
    }
    
    private fun updateEffect() {
        kraftEffectView.setEffect { targetBuffer ->
            pipeline(targetBuffer) {
                serialSteps(
                    inputTexture = sampleBitmap?.asTexture() ?: return@setEffect null,
                    targetBuffer = targetBuffer
                ) {
                    step(SaturationKraftShader()) {
                        saturation = sampledInput { this@MainActivity.saturation }
                    }
                    
                    step(BrightnessKraftShader()) {
                        brightness = sampledInput { this@MainActivity.brightness }
                    }
                }
            }
        }
    }
}
```

### Using KraftShade with Jetpack Compose

If you're using Jetpack Compose, here's how to create the same effect:

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
                    inputTexture = sampledBitmapTextureProvider { image },
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
            Text("Brightness: ${String.format("%.1f", brightness)}")
            Slider(
                value = brightness,
                onValueChange = {
                    brightness = it
                    state.requestRender()
                },
                valueRange = -1f..1f
            )
            
            Text("Saturation: ${String.format("%.1f", saturation)}")
            Slider(
                value = saturation,
                onValueChange = {
                    saturation = it
                    state.requestRender()
                },
                valueRange = 0f..2f
            )
        }
    }
}
```

## Understanding the Code

Let's break down the key components of the example:

1. **Setting up the effect state**:
   - In traditional views, we use `KraftEffectTextureView`
   - In Compose, we use `rememberKraftShadeEffectState()` and `KraftShadeEffectView`

2. **Creating a pipeline**:
   - We use the `pipeline` DSL to define our rendering pipeline
   - `serialSteps` creates a linear processing chain
   - Each `step` adds a shader to the pipeline

3. **Configuring shaders**:
   - We use `SaturationKraftShader` and `BrightnessKraftShader`
   - Parameters are set using `sampledInput` to create dynamic inputs

4. **Handling user input**:
   - We update shader parameters based on user input
   - In traditional views, we call `updateEffect()` when values change
   - In Compose, we call `state.requestRender()` when values change

## Next Steps

Now that you've created your first KraftShade application, you can:

- Learn more about [Basic Concepts](./basic-concepts) in KraftShade
- Create a more complex [First Effect](./first-effect)
- Explore the [Core Components](../core-components) of KraftShade
- Check out the [Built-in Shaders](../built-in-shaders) for more effect options
