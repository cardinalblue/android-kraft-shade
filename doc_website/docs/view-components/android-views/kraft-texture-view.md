---
sidebar_position: 1
---

# KraftTextureView

> **⚠️ Important Note:** In most cases, you usually don't need to use `KraftTextureView` directly. [KraftEffectTextureView](kraft-effect-texture-view.md) or [AnimatedKraftTextureView](animated-kraft-texture-view.md) provide higher-level functionality that is more suitable for most use cases. Only use `KraftTextureView` directly if you need very custom OpenGL rendering that doesn't fit the patterns of the specialized views.

`KraftTextureView` is the base class for all View implementations in KraftShade. It provides the fundamental OpenGL rendering capabilities that other specialized views build upon.

## Overview

`KraftTextureView` extends Android's `TextureView` and provides a foundation for OpenGL rendering in KraftShade. It manages the OpenGL environment (`GlEnv`) and window surface, handling the lifecycle of these components in sync with the view's lifecycle.

While you typically won't use `KraftTextureView` directly (instead using `KraftEffectTextureView` or `AnimatedKraftTextureView`), understanding its core functionality is important for advanced usage scenarios.

## Key Features

- Manages the OpenGL environment lifecycle
- Provides a window surface for rendering
- Handles task scheduling for OpenGL operations
- Synchronizes with the view's attachment/detachment lifecycle
- Supports transparent rendering

## Basic Usage

Here's a simple example of using `KraftTextureView` to render a custom shader:

```kotlin
class MyActivity : AppCompatActivity() {
    private lateinit var kraftTextureView: KraftTextureView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create the view
        kraftTextureView = KraftTextureView(this)
        setContentView(kraftTextureView)
        
        // Run an OpenGL task when the view is ready
        kraftTextureView.runGlTask { windowSurface ->
            // Create and apply a custom shader
            val myShader = MyCustomShader()
            myShader.drawTo(windowSurface)
        }
    }
    
    override fun onDestroy() {
        // Clean up resources
        kraftTextureView.terminate()
        super.onDestroy()
    }
}
```

## Important Methods

### `runGlTask`

The most important method in `KraftTextureView` is `runGlTask`, which allows you to execute OpenGL operations:

```kotlin
fun runGlTask(task: KraftTextureViewTask): Job
```

This method:
- Takes a suspend function that receives a `GlEnvDslScope` and `WindowSurfaceBuffer`
- Returns a Kotlin coroutine `Job` that can be used to track or cancel the operation
- Automatically queues tasks if the OpenGL environment isn't ready yet

### `terminate`

Call this method to clean up resources when you're done with the view:

```kotlin
fun terminate()
```

## Considerations

- `KraftTextureView` operations run asynchronously in a coroutine context
- Tasks submitted via `runGlTask` may be queued if the OpenGL environment isn't ready
- Always call `terminate()` when you're done with the view to prevent memory leaks
- For most use cases, consider using `KraftEffectTextureView` or `AnimatedKraftTextureView` instead

## Next Steps

For most applications, you'll want to use one of the specialized views that extend `KraftTextureView`:

- [KraftEffectTextureView](kraft-effect-texture-view.md) - For applying shader effects to images
- [AnimatedKraftTextureView](animated-kraft-texture-view.md) - For creating animated shader effects
