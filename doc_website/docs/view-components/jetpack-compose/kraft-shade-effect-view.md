---
sidebar_position: 2
---

# KraftShadeEffectView

`KraftShadeEffectView` is a Jetpack Compose wrapper for [KraftEffectTextureView](../android-views/kraft-effect-texture-view.md), providing shader effect capabilities in Compose UIs.

## Overview

`KraftShadeEffectView` integrates the shader effect capabilities of `KraftEffectTextureView` into Jetpack Compose applications. It builds on the foundation of [KraftShadeView](kraft-shade-view.md) and adds functionality specifically for applying visual effects to images and other content.

This component is ideal for Compose applications that need to apply visual effects to images, with support for both one-time rendering and on-demand updates when effect parameters change.

## Key Features

- Integrates `KraftEffectTextureView` into Jetpack Compose UIs
- Provides a state-based API for managing effects
- Supports setting and updating shader effects
- Allows on-demand rendering when effect parameters change
- Handles proper resource cleanup with Compose's lifecycle

## Basic Usage

Here's a simple example of using `KraftShadeEffectView` to apply a saturation effect to an image in a Compose UI:

```kotlin
@Composable
fun SaturationEffectDemo() {
    // Create and remember the state
    val state = rememberKraftShadeEffectState()
    var saturation by remember { mutableFloatStateOf(1.0f) }
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Render area
        KraftShadeEffectView(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(aspectRatio),
            state = state
        )
        
        // Saturation control
        Text("Saturation: ${saturation.format(1)}")
        Slider(
            value = saturation,
            onValueChange = { 
                saturation = it
                state.requestRender() // Request a render with the new saturation
            },
            valueRange = 0f..2f,
            modifier = Modifier.padding(16.dp)
        )
    }
    
    // Set up the effect when the composition is first created
    LaunchedEffect(Unit) {
        state.setEffect { windowSurface ->
            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.width.toFloat() / bitmap.height
            
            pipeline(windowSurface) {
                serialSteps(
                    inputTexture = bitmap.asTexture(),
                    targetBuffer = windowSurface
                ) {
                    step(SaturationKraftShader()) { shader ->
                        shader.saturation = saturation
                    }
                }
            }
        }
    }
}

// Helper extension function to format float values
fun Float.format(digits: Int) = "%.${digits}f".format(this)
```

## Components

### KraftShadeEffectView Composable

```kotlin
@Composable
fun KraftShadeEffectView(
    modifier: Modifier = Modifier,
    state: KraftShadeEffectState = rememberKraftShadeEffectState()
)
```

The main Composable function that creates a `KraftEffectTextureView` and integrates it into your Compose UI.

Parameters:
- `modifier`: Standard Compose modifier for customizing the view's layout
- `state`: A `KraftShadeEffectState` that manages the view's state and operations

### KraftShadeEffectState

```kotlin
class KraftShadeEffectState(
    scope: CoroutineScope,
    var skipRender: Boolean = false
)
```

Manages the state of the `KraftShadeEffectView` and provides methods to interact with it.

Key methods:
- `setEffect(afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = { requestRender() }, effectExecutionProvider: EffectExecutionProvider)`: Sets the effect to be applied
- `requestRender()`: Triggers a render with the current effect
- `setRenderOnSizeChange(enabled: Boolean)`: Controls whether rendering is automatically triggered when the view size changes
- `renderBlocking()`: Performs a blocking render (use with caution)

### rememberKraftShadeEffectState

```kotlin
@Composable
fun rememberKraftShadeEffectState(
    skipRendering: Boolean = false,
    renderOnSizeChange: Boolean = true
): KraftShadeEffectState
```

A Compose helper function that creates and remembers a `KraftShadeEffectState` instance, ensuring it survives recomposition.

Parameters:
- `skipRendering`: When true, rendering requests will be ignored
- `renderOnSizeChange`: Controls whether rendering is automatically triggered when the view size changes

## Example: Multiple Effects with Controls

This example shows how to apply multiple effects to an image with interactive controls:

```kotlin
@Composable
fun MultiEffectDemo() {
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Render area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
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
            Text("Saturation")
            Slider(
                value = saturation,
                onValueChange = { 
                    saturation = it
                    state.requestRender()
                },
                valueRange = 0f..2f
            )
            
            Text("Brightness")
            Slider(
                value = brightness,
                onValueChange = { 
                    brightness = it
                    state.requestRender()
                },
                valueRange = -1f..1f
            )
            
            Text("Contrast")
            Slider(
                value = contrast,
                onValueChange = { 
                    contrast = it
                    state.requestRender()
                },
                valueRange = 0.5f..1.5f
            )
        }
    }
    
    // Set up the effect
    LaunchedEffect(Unit) {
        state.setEffect { windowSurface ->
            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.width.toFloat() / bitmap.height
            
            pipeline(windowSurface) {
                serialSteps(
                    inputTexture = bitmap.asTexture(),
                    targetBuffer = windowSurface
                ) {
                    step(SaturationKraftShader()) { shader ->
                        shader.saturation = saturation
                    }
                    
                    step(BrightnessKraftShader()) { shader ->
                        shader.brightness = brightness
                    }
                    
                    step(ContrastKraftShader()) { shader ->
                        shader.contrast = contrast
                    }
                }
            }
        }
    }
}
```

## Integration with Compose State

`KraftShadeEffectView` works well with Compose's state management system. You can:

1. Use Compose state variables to control shader parameters
2. Call `requestRender()` when state changes to update the visual effect
3. Use `LaunchedEffect` to set up the initial effect

For more advanced integration, you can use the `asSampledInput()` extension function to convert Compose state to KraftShade inputs:

```kotlin
@Composable
fun ComposeStateIntegrationDemo() {
    val state = rememberKraftShadeEffectState()
    var saturation by remember { mutableFloatStateOf(1f) }
    val saturationState = remember { mutableStateOf(1f) }
    
    // When using asSampledInput, changes to the state are automatically
    // reflected in the shader without calling requestRender()
    LaunchedEffect(saturation) {
        saturationState.value = saturation
    }
    
    // Set up the effect with the state-based input
    LaunchedEffect(Unit) {
        state.setEffect { windowSurface ->
            pipeline(windowSurface) {
                serialSteps(inputTexture, windowSurface) {
                    step(SaturationKraftShader()) { shader ->
                        // Use the Compose state as a shader input
                        shader.saturation = saturationState.asSampledInput().get()
                    }
                }
            }
        }
    }
    
    // UI components...
}
```

## Considerations

- Effects are applied asynchronously in a coroutine context
- Always call `requestRender()` after changing effect parameters (unless using `asSampledInput()`)
- For animated effects that change over time, use [KraftShadeAnimatedView](kraft-shade-animated-view.md) instead
- The `skipRendering` parameter can be useful for temporarily disabling rendering during complex state changes
- Setting `renderOnSizeChange` to false can be useful when you want to control exactly when rendering occurs

## Next Steps

For animated effects in Compose, see [KraftShadeAnimatedView](kraft-shade-animated-view.md).
