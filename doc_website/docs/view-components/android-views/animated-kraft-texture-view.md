---
sidebar_position: 3
---

# AnimatedKraftTextureView

`AnimatedKraftTextureView` extends [KraftEffectTextureView](kraft-effect-texture-view.md) to provide animation capabilities for shader effects.

## Overview

`AnimatedKraftTextureView` is designed for creating time-based animations with shader effects. It builds on the effect rendering capabilities of `KraftEffectTextureView` and adds animation control through Android's Choreographer for frame-synchronized rendering.

This view is ideal for applications that need to create dynamic, animated visual effects that change over time, such as transitions, procedural animations, or time-based visual effects.

## Key Features

- Frame-synchronized rendering using Android's Choreographer
- Built-in play/pause controls for animation
- Integrated `TimeInput` for time-based animations
- Specialized methods for setting up animated effects
- Automatic frame skipping when rendering can't keep up with the frame rate

## Basic Usage

Here's a simple example of using `AnimatedKraftTextureView` to create an animated saturation effect:

```kotlin
class MyActivity : AppCompatActivity() {
    private lateinit var animatedView: AnimatedKraftTextureView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create the view
        animatedView = AnimatedKraftTextureView(this)
        setContentView(animatedView)
        
        // Load an image and set up an animated saturation effect
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.my_image)
        
        animatedView.setEffectAndPlay { windowSurface, timeInput ->
            // Create a pipeline with an animated saturation shader
            val saturationInput = timeInput.bounceBetween(0f, 2f)
            
            pipeline(windowSurface) {
                serialSteps(
                    inputTexture = bitmap.asTexture(),
                    targetBuffer = windowSurface
                ) {
                    step(SaturationKraftShader()) { shader ->
                        shader.saturation = saturationInput.get()
                    }
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        animatedView.stop()  // Pause animation when activity is paused
    }
    
    override fun onResume() {
        super.onResume()
        animatedView.play()  // Resume animation when activity is resumed
    }
    
    override fun onDestroy() {
        // Clean up resources
        animatedView.terminate()
        super.onDestroy()
    }
}
```

## Important Methods

### Setting Up Animations

`AnimatedKraftTextureView` provides several methods for setting up animated effects:

#### `setEffectWithTimeInput`

```kotlin
fun setEffectWithTimeInput(
    afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer, timeInput: TimeInput) -> Unit = { _, _ -> },
    effectExecutionProvider: AnimatedEffectExecutionProvider
)
```

This method:
- Takes an `AnimatedEffectExecutionProvider` that creates the effect execution pipeline with a `TimeInput`
- Optionally accepts an `afterSet` lambda that runs after the effect is set
- Does not automatically start the animation

#### `setEffectAndPlay`

```kotlin
fun setEffectAndPlay(effectExecutionProvider: AnimatedEffectExecutionProvider)
```

This method:
- Sets up the effect with the provided `AnimatedEffectExecutionProvider`
- Automatically starts the animation after the effect is set

#### `setEffectAndPause`

```kotlin
fun setEffectAndPause(effectExecutionProvider: AnimatedEffectExecutionProvider)
```

This method:
- Sets up the effect with the provided `AnimatedEffectExecutionProvider`
- Does not start the animation (remains paused)

### Animation Control

`AnimatedKraftTextureView` provides methods to control the animation playback:

#### `play`

```kotlin
fun play()
```

Starts or resumes the animation.

#### `stop`

```kotlin
fun stop()
```

Pauses the animation.

## Properties

### `timeInput`

```kotlin
val timeInput: TimeInput
```

A `TimeInput` instance that can be used to create time-based animations. This input automatically updates as the animation progresses.

### `playing`

```kotlin
val playing: Boolean
```

Indicates whether the animation is currently playing.

## Example: Creating a Time-Based Animation

This example shows how to create an animation that oscillates between different visual effects:

```kotlin
class MyAnimationActivity : AppCompatActivity() {
    private lateinit var animatedView: AnimatedKraftTextureView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation)
        
        animatedView = findViewById(R.id.animated_view)
        val playPauseButton = findViewById<Button>(R.id.play_pause_button)
        
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.my_image)
        
        // Set up the animated effect
        animatedView.setEffectWithTimeInput { windowSurface, timeInput ->
            // Create time-based inputs for various effects
            val saturationInput = timeInput.bounceBetween(0.5f, 1.5f, periodMs = 3000)
            val contrastInput = timeInput.bounceBetween(0.8f, 1.2f, periodMs = 5000)
            val hueRotationInput = timeInput.map { (it % 10000) / 10000f * 360f }
            
            pipeline(windowSurface) {
                serialSteps(
                    inputTexture = bitmap.asTexture(),
                    targetBuffer = windowSurface
                ) {
                    step(SaturationKraftShader()) { shader ->
                        shader.saturation = saturationInput.get()
                    }
                    
                    step(ContrastKraftShader()) { shader ->
                        shader.contrast = contrastInput.get()
                    }
                    
                    step(HueKraftShader()) { shader ->
                        shader.setHueInDegree(hueRotationInput.get())
                    }
                }
            }
            
            // Start the animation
            animatedView.play()
        }
        
        // Set up play/pause button
        playPauseButton.setOnClickListener {
            if (animatedView.playing) {
                animatedView.stop()
                playPauseButton.text = "Play"
            } else {
                animatedView.play()
                playPauseButton.text = "Pause"
            }
        }
    }
}
```

## Working with TimeInput

The `TimeInput` class provides several methods for creating time-based animations:

### `bounceBetween`

```kotlin
fun bounceBetween(min: Float, max: Float, periodMs: Long = 2000): SampledInput<Float>
```

Creates an input that oscillates between the minimum and maximum values over the specified period.

### `map`

```kotlin
fun <T> map(mapper: (Long) -> T): SampledInput<T>
```

Creates a custom input by mapping the elapsed time to a value of type T.

## Considerations

- Animations run at the device's refresh rate (typically 60fps)
- The animation automatically pauses when the view is detached from the window
- For complex effects, monitor performance and consider simplifying shaders if frame drops occur
- Always call `stop()` when the animation is not visible to conserve battery
- For non-animated effects, use [KraftEffectTextureView](kraft-effect-texture-view.md) instead

## Next Steps

For Jetpack Compose integration, see [KraftShadeAnimatedView](../jetpack-compose/kraft-shade-animated-view.md).
