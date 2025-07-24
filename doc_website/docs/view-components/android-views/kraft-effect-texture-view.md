---
sidebar_position: 2
---

# KraftEffectTextureView

`KraftEffectTextureView` extends [KraftTextureView](kraft-texture-view.md) to provide a convenient way to apply shader effects to images and other content.

## Overview

`KraftEffectTextureView` is designed for applying shader effects to content. It builds on the OpenGL rendering capabilities of `KraftTextureView` and adds functionality specifically for managing effects and controlling when rendering occurs.

This view is ideal for applications that need to apply visual effects to images or other content, with support for both one-time rendering and on-demand updates when effect parameters change.

## Key Features

- Manages an `EffectExecution` instance that defines the visual effect to be applied
- Provides methods to set and update effects
- Supports on-demand rendering when effect parameters change
- Handles aspect ratio adjustments for proper content display
- Automatically re-renders when the view size changes (configurable)

## Basic Usage

Here's a simple example of using `KraftEffectTextureView` to apply a saturation effect to an image:

```kotlin
class MyActivity : AppCompatActivity() {
    private lateinit var effectView: KraftEffectTextureView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create the view
        effectView = KraftEffectTextureView(this)
        setContentView(effectView)
        
        // Load an image and apply a saturation effect
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.my_image)
        
        effectView.setEffect(
            effectExecutionProvider = { windowSurface ->
                // Create a pipeline with a saturation shader
                pipeline(windowSurface) {
                    serialSteps(
                        inputTexture = bitmap.asTexture(),
                        targetBuffer = windowSurface
                    ) {
                        step(SaturationKraftShader()) { shader ->
                            shader.saturation = 1.5f  // Increase saturation
                        }
                    }
                }
            }
        )
    }
    
    override fun onDestroy() {
        // Clean up resources
        effectView.terminate()
        super.onDestroy()
    }
}
```

## Important Methods

### `setEffect`

The primary method for configuring the effect to be applied:

```kotlin
fun setEffect(
    afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = {},
    effectExecutionProvider: EffectExecutionProvider
)
```

This method:
- Takes an `EffectExecutionProvider` that creates the effect execution pipeline
- Optionally accepts an `afterSet` lambda that runs after the effect is set
- Typically used with the pipeline DSL to create a rendering pipeline

### `requestRender`

Triggers a render with the current effect:

```kotlin
fun requestRender()
```

Call this method when you've changed effect parameters and want to update the display.

## Properties

### `renderOnSizeChange`

Controls whether rendering is automatically triggered when the view size changes:

```kotlin
var renderOnSizeChange: Boolean = true
```

### `ratio`

Sets the aspect ratio of the view (width/height):

```kotlin
var ratio: Float = 0.0f
```

When set to a non-zero value, the view will maintain this aspect ratio during layout.

## Example: Adjustable Effect

This example shows how to create an effect with parameters that can be adjusted dynamically:

```kotlin
class MyEffectActivity : AppCompatActivity() {
    private lateinit var effectView: KraftEffectTextureView
    private var saturation = 1.0f
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_effect)
        
        effectView = findViewById(R.id.effect_view)
        val saturationSeekBar = findViewById<SeekBar>(R.id.saturation_seekbar)
        
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.my_image)
        
        // Set the aspect ratio based on the bitmap dimensions
        effectView.ratio = bitmap.width.toFloat() / bitmap.height
        
        // Set up the effect
        effectView.setEffect(
            effectExecutionProvider = { windowSurface ->
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
        )
        
        // Update saturation when the seek bar changes
        saturationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                saturation = progress / 100f * 2f  // Range: 0 to 2
                effectView.requestRender()  // Request a render with the new saturation
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }
}
```

## Considerations

- Effects are applied asynchronously in a coroutine context
- Always call `requestRender()` after changing effect parameters
- For animated effects that change over time, consider using [AnimatedKraftTextureView](animated-kraft-texture-view.md) instead
- The view automatically handles OpenGL context management, but you should still call `terminate()` when done

## Next Steps

For animated effects that change over time, see [AnimatedKraftTextureView](animated-kraft-texture-view.md).
