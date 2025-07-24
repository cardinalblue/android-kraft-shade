---
sidebar_position: 1
---

# KraftShadeView

> **⚠️ Important Note:** In most cases, you usually don't need to use `KraftShadeView` directly. [KraftShadeEffectView](kraft-shade-effect-view.md) or [KraftShadeAnimatedView](kraft-shade-animated-view.md) provide higher-level functionality that is more suitable for most use cases. Only use `KraftShadeView` directly if you need very custom OpenGL rendering that doesn't fit the patterns of the specialized views.

`KraftShadeView` is a Jetpack Compose wrapper for [KraftTextureView](../android-views/kraft-texture-view.md), providing OpenGL rendering capabilities in Compose UIs.

## Overview

`KraftShadeView` integrates the basic OpenGL rendering capabilities of `KraftTextureView` into Jetpack Compose applications. It uses Compose's `AndroidView` to wrap the native Android view and provides a state-based API that's more idiomatic for Compose applications.

This component is the foundation for OpenGL rendering in Compose UIs with KraftShade, serving as the base for more specialized components like `KraftShadeEffectView` and `KraftShadeAnimatedView`.

## Key Features

- Integrates `KraftTextureView` into Jetpack Compose UIs
- Provides a state-based API for managing the view
- Handles proper resource cleanup with Compose's lifecycle
- Supports running OpenGL tasks through the state object

## Basic Usage

Here's a simple example of using `KraftShadeView` to render a custom shader in a Compose UI:

```kotlin
@Composable
fun TransparencyDemo() {
    // Create and remember the state
    val state = rememberKraftShadeState()
    
    // Create a colored background to demonstrate transparency
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        // Use KraftShadeView with the remembered state
        KraftShadeView(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f),
            state = state
        )
    }
    
    // Set up the shader when the composition is first created
    LaunchedEffect(state) {
        state.runGlTask { windowSurface ->
            // Create and apply a custom shader
            TransparencyShader().apply {
                drawTo(windowSurface)
            }
        }
    }
}

// A simple shader that demonstrates transparency
class TransparencyShader : KraftShader() {
    override fun loadFragmentShader(): String = """
        precision mediump float;
        varying vec2 textureCoordinate;

        void main() {
            // Create a circular mask
            vec2 center = vec2(0.5, 0.5);
            float radius = 0.4;
            float dist = distance(textureCoordinate, center);

            // Inside the circle: semi-transparent blue
            // Outside the circle: fully transparent
            if (dist < radius) {
                gl_FragColor = vec4(0.0, 0.0, 1.0, 0.5); // 50% transparent blue
            } else {
                gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0); // Fully transparent
            }
        }
    """.trimIndent()
}
```

## Components

### KraftShadeView Composable

```kotlin
@Composable
fun KraftShadeView(
    modifier: Modifier = Modifier,
    state: KraftShadeState = rememberKraftShadeState()
)
```

The main Composable function that creates a `KraftTextureView` and integrates it into your Compose UI.

Parameters:
- `modifier`: Standard Compose modifier for customizing the view's layout
- `state`: A `KraftShadeState` that manages the view's state and operations

### KraftShadeState

```kotlin
class KraftShadeState(scope: CoroutineScope)
```

Manages the state of the `KraftShadeView` and provides methods to interact with it.

Key methods:
- `runGlTask(task: KraftTextureViewTask): Job`: Runs an OpenGL task on the view
- `terminate()`: Cleans up resources when the view is no longer needed

### rememberKraftShadeState

```kotlin
@Composable
fun rememberKraftShadeState(): KraftShadeState
```

A Compose helper function that creates and remembers a `KraftShadeState` instance, ensuring it survives recomposition.

## Example: Custom Rendering in Compose

This example shows how to integrate `KraftShadeView` into a more complex Compose UI:

```kotlin
@Composable
fun CustomRenderingScreen() {
    val state = rememberKraftShadeState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Render area
        KraftShadeView(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(aspectRatio),
            state = state
        )
        
        // UI controls
        Text(
            text = "Custom OpenGL Rendering",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
    }
    
    // Set up the rendering
    LaunchedEffect(state) {
        state.runGlTask { windowSurface ->
            // Set a 16:9 aspect ratio
            aspectRatio = 16f / 9f
            
            // Create a custom shader for rendering
            CustomPatternShader().apply {
                drawTo(windowSurface)
            }
        }
    }
}

// A custom shader that creates a pattern
class CustomPatternShader : KraftShader() {
    override fun loadFragmentShader(): String = """
        precision mediump float;
        varying vec2 textureCoordinate;

        void main() {
            // Create a checkerboard pattern
            float x = floor(textureCoordinate.x * 10.0);
            float y = floor(textureCoordinate.y * 10.0);
            float pattern = mod(x + y, 2.0);
            
            if (pattern < 1.0) {
                gl_FragColor = vec4(0.2, 0.3, 0.8, 1.0); // Blue
            } else {
                gl_FragColor = vec4(0.8, 0.2, 0.2, 1.0); // Red
            }
        }
    """.trimIndent()
}
```

## Lifecycle Management

`KraftShadeView` automatically handles the view's lifecycle in sync with Compose's lifecycle:

1. When the Composable enters the composition, a new `KraftTextureView` is created
2. The view is attached to the provided `KraftShadeState`
3. When the Composable leaves the composition, a `DisposableEffect` calls `terminate()` to clean up resources

This ensures proper resource management and prevents memory leaks.

## Considerations

- OpenGL operations should be performed within the `runGlTask` method of the state object
- For applying shader effects to images, consider using [KraftShadeEffectView](kraft-shade-effect-view.md) instead
- For animated effects, use [KraftShadeAnimatedView](kraft-shade-animated-view.md)
- The underlying `KraftTextureView` operations run asynchronously in a coroutine context

## Next Steps

For applying shader effects to images in Compose, see [KraftShadeEffectView](kraft-shade-effect-view.md).
