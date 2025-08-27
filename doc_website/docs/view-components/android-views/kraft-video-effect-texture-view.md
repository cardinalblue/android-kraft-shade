---
sidebar_position: 4
---

# KraftVideoEffectTextureView

`KraftVideoEffectTextureView` is a specialized view that extends `KraftEffectTextureView` to provide seamless video playback with shader effects. It handles video rendering, MediaPlayer management, and automatic texture updates for video frames.

## Overview

This view combines video playback capabilities with KraftShade's shader effect system, making it easy to apply real-time effects to video content. It automatically handles:

- Video texture creation and management
- MediaPlayer lifecycle
- Frame synchronization with Choreographer
- Video rotation and coordinate system transformations
- Playback controls and state management

## Key Features

- **Automatic video texture management**: Handles ExternalOESTexture creation and updates
- **MediaPlayer integration**: Built-in MediaPlayer with proper lifecycle management
- **Frame synchronization**: Uses Android's Choreographer for smooth frame updates
- **Effect pipeline support**: Specialized `setEffectWithPipeline` method for video processing
- **Playback controls**: Play, pause, resume functionality with state tracking
- **Lifecycle awareness**: Proper handling of activity pause/resume cycles
- **Auto-rotation handling**: Automatically applies correct video transformations

## Basic Usage

Here's a simple example of using `KraftVideoEffectTextureView` with brightness effect:

```kotlin
class VideoActivity : AppCompatActivity() {
    private lateinit var videoView: KraftVideoEffectTextureView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create the view
        videoView = KraftVideoEffectTextureView(this)
        setContentView(videoView)
        
        // Set video source
        val videoUri = Uri.parse("android.resource://$packageName/${R.raw.sample_video}")
        videoView.setVideoUri(videoUri, autoPlay = true)
        
        // Apply brightness effect
        videoView.setEffectWithPipeline { inputTexture, targetBuffer ->
            serialSteps(inputTexture, targetBuffer) {
                step(BrightnessKraftShader(0.2f))
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        videoView.onPause()
    }
    
    override fun onResume() {
        super.onResume()
        videoView.onResume()
    }
    
    override fun onDestroy() {
        videoView.releaseMediaPlayer()
        super.onDestroy()
    }
}
```

## Video-Specific Effect Pipeline

### `setEffectWithPipeline`

The most important method for video effects is `setEffectWithPipeline`, which handles video-specific operations automatically:

```kotlin
fun setEffectWithPipeline(
    afterSet: suspend GlEnvDslScope.(windowSurface: WindowSurfaceBuffer) -> Unit = { requestRender() },
    effectExecution: suspend GraphPipelineSetupScope.(inputTexture: TextureProvider, targetBuffer: GlBufferProvider) -> Unit
)
```

This method automatically handles:
- **Video texture updates**: Calls `SurfaceTexture.updateTexImage()` for the latest frame
- **Video rotation**: Applies correct rotation transformation from video metadata
- **Vertical flip**: Applies necessary coordinate system transformation for video rendering

### Complex Effect Pipeline Example

```kotlin
videoView.setEffectWithPipeline { inputTexture, targetBuffer ->
    serialSteps(inputTexture, targetBuffer) {
        // Apply saturation effect
        step(SaturationKraftShader(1.5f))
        
        // Apply brightness effect
        step(BrightnessKraftShader(0.3f))
        
        // Apply custom shader
        step(MyCustomVideoShader()) { shader ->
            shader.setCustomParameter(someValue)
        }
    }
}
```

## Playback Controls

### Setting Video Source

```kotlin
// Set video with auto-play
videoView.setVideoUri(uri, autoPlay = true)

// Set video without auto-play (shows first frame)
videoView.setVideoUri(uri, autoPlay = false)
```

### Playback Control Methods

```kotlin
// Check if video is currently playing
val isPlaying = videoView.isPlaying()

// Pause playback
videoView.pausePlayback()

// Resume playback
videoView.resumePlayback()

// Clean up MediaPlayer resources
videoView.releaseMediaPlayer()
```

### Lifecycle Methods

```kotlin
// In your Activity/Fragment
override fun onPause() {
    super.onPause()
    videoView.onPause() // Automatically pauses if playing
}

override fun onResume() {
    super.onResume()
    videoView.onResume() // Resumes if was playing before pause
}
```


## Important Considerations

### Memory Management

- Always call `releaseMediaPlayer()` in your activity's `onDestroy()`
- The view automatically manages video textures and OpenGL resources
- MediaPlayer is reused for multiple videos to optimize memory usage

### Lifecycle Integration

- Use `onPause()` and `onResume()` methods to properly handle activity lifecycle
- The view automatically tracks playback state during pause/resume cycles
- Choreographer callbacks are properly managed to prevent memory leaks
