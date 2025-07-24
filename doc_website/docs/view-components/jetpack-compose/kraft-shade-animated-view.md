---
sidebar_position: 3
---

# KraftShadeAnimatedView

`KraftShadeAnimatedView` is a Jetpack Compose wrapper for [AnimatedKraftTextureView](../android-views/animated-kraft-texture-view.md), providing animation capabilities for shader effects in Compose UIs.

## Overview

`KraftShadeAnimatedView` integrates the animation capabilities of `AnimatedKraftTextureView` into Jetpack Compose applications. It builds on the foundation of [KraftShadeEffectView](kraft-shade-effect-view.md) and adds functionality specifically for creating time-based animations with shader effects.

This component is ideal for Compose applications that need to create dynamic, animated visual effects that change over time, such as transitions, procedural animations, or time-based visual effects.

## Key Features

- Integrates `AnimatedKraftTextureView` into Jetpack Compose UIs
- Provides a state-based API for managing animated effects
- Supports play/pause controls for animation
- Includes a built-in `TimeInput` for time-based animations
- Handles proper resource cleanup with Compose's lifecycle

## Basic Usage

Here's a simple example of using `KraftShadeAnimatedView` to create an animated saturation effect in a Compose UI:

```kotlin
@Composable
fun AnimatedSaturationDemo() {
    // Create and remember the state
    val state = rememberKraftShadeAnimatedState()
    var isPlaying by remember { mutableStateOf(true) }
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated view
        KraftShadeAnimatedView(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(aspectRatio),
            state = state
        )
        
        // Play/Pause button
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = {
                if (isPlaying) {
                    state.stop()
                } else {
                    state.play()
                }
                isPlaying = !isPlaying
            }
        ) {
            Text(if (isPlaying) "Pause" else "Play")
        }
    }
    
    // Set up the animated effect
    LaunchedEffect(Unit) {
        state.setEffectAndPlay { windowSurface, timeInput ->
            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.width.toFloat() / bitmap.height
            
            // Create a saturation input that oscillates between 0 and 1
            val saturationInput = timeInput.bounceBetween(0f, 1f)
            
            pipeline(windowSurface) {
                serialSteps(bitmap.asTexture(), windowSurface) {
                    step(SaturationKraftShader()) { shader ->
                        shader.saturation = saturationInput.get()
                    }
                }
            }
        }
    }
}
```

## Components

### KraftShadeAnimatedView Composable

```kotlin
@Composable
fun KraftShadeAnimatedView(
    modifier: Modifier = Modifier,
    state: KraftShadeAnimatedState = rememberKraftShadeAnimatedState()
)
```

The main Composable function that creates an `AnimatedKraftTextureView` and integrates it into your Compose UI.

Parameters:
- `modifier`: Standard Compose modifier for customizing the view's layout
- `state`: A `KraftShadeAnimatedState` that manages the view's state and operations

### KraftShadeAnimatedState

```kotlin
class KraftShadeAnimatedState(scope: CoroutineScope)
```

Manages the state of the `KraftShadeAnimatedView` and provides methods to interact with it.

Key methods:
- `setEffectAndPlay(effectExecutionProvider: AnimatedEffectExecutionProvider)`: Sets the effect and starts the animation
- `setEffectAndPause(effectExecutionProvider: AnimatedEffectExecutionProvider)`: Sets the effect without starting the animation
- `play()`: Starts or resumes the animation
- `stop()`: Pauses the animation
- `getTimeInput()`: Gets the `TimeInput` instance for creating time-based animations

Properties:
- `playing`: Indicates whether the animation is currently playing

### rememberKraftShadeAnimatedState

```kotlin
@Composable
fun rememberKraftShadeAnimatedState(): KraftShadeAnimatedState
```

A Compose helper function that creates and remembers a `KraftShadeAnimatedState` instance, ensuring it survives recomposition.

## Example: Complex Animation with Multiple Effects

This example shows how to create a more complex animation with multiple time-based effects:

```kotlin
@Composable
fun ComplexAnimationDemo() {
    val state = rememberKraftShadeAnimatedState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated view
        KraftShadeAnimatedView(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(aspectRatio),
            state = state
        )
        
        // Control buttons
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { state.play() }) {
                Text("Play")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(onClick = { state.stop() }) {
                Text("Pause")
            }
        }
    }
    
    // Set up the animated effect
    LaunchedEffect(Unit) {
        state.setEffectAndPlay { windowSurface, timeInput ->
            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.width.toFloat() / bitmap.height
            
            // Create multiple time-based inputs with different periods
            val saturationInput = timeInput.bounceBetween(0.5f, 1.5f, periodMs = 3000)
            val contrastInput = timeInput.bounceBetween(0.8f, 1.2f, periodMs = 5000)
            val hueRotationInput = timeInput.map { (it % 10000) / 10000f * 360f }
            
            // Create a swirl effect with animated center point
            val swirlCenterX = timeInput.bounceBetween(0.3f, 0.7f, periodMs = 4000)
            val swirlCenterY = timeInput.bounceBetween(0.3f, 0.7f, periodMs = 6000)
            val swirlAngle = timeInput.bounceBetween(0f, 2f, periodMs = 2000)
            
            pipeline(windowSurface) {
                serialSteps(bitmap.asTexture(), windowSurface) {
                    // Color adjustments
                    step(SaturationKraftShader()) { shader ->
                        shader.saturation = saturationInput.get()
                    }
                    
                    step(ContrastKraftShader()) { shader ->
                        shader.contrast = contrastInput.get()
                    }
                    
                    step(HueKraftShader()) { shader ->
                        shader.setHueInDegree(hueRotationInput.get())
                    }
                    
                    // Distortion effect
                    step(SwirlKraftShader()) { shader ->
                        shader.center = GlVec2(swirlCenterX.get(), swirlCenterY.get())
                        shader.angle = swirlAngle.get()
                        shader.radius = 0.5f
                    }
                }
            }
        }
    }
}
```

## Working with TimeInput in Compose

The `TimeInput` class provides several methods for creating time-based animations that work well with Compose:

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

## Lifecycle Management

`KraftShadeAnimatedView` automatically handles the animation lifecycle in sync with Compose's lifecycle:

1. When the Composable enters the composition, a new `AnimatedKraftTextureView` is created
2. The view is attached to the provided `KraftShadeAnimatedState`
3. When the Composable leaves the composition, a `DisposableEffect` calls `terminate()` to clean up resources
4. The animation automatically stops when the view is detached from the window

This ensures proper resource management and prevents unnecessary rendering when the view is not visible.

## Considerations

- Animations run at the device's refresh rate (typically 60fps)
- For complex effects, monitor performance and consider simplifying shaders if frame drops occur
- Always call `stop()` when the animation is not visible to conserve battery
- For non-animated effects, use [KraftShadeEffectView](kraft-shade-effect-view.md) instead
- The `playing` property can be used to update UI elements based on the animation state
