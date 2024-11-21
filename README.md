# KraftShade

KraftShade is a modern, high-performance OpenGL ES graphics rendering library for Android, designed to provide a type-safe, Kotlin-first abstraction over OpenGL ES 2.0. Built with coroutines support and a focus on developer experience, KraftShade makes complex graphics operations simple while maintaining flexibility and performance.

## Why Another Graphics Library?

While GPUImage has been a popular choice for Android graphics processing, it comes with several limitations:

1. Java-centric Design
   - Lacks Kotlin idioms and modern language features
   - No coroutines support for thread (EGLContext) based operations (using GLThread)

2. Inflexible Pipeline
   - Rigid filter chain architecture (GPUImageFilterGroup)
   - Limited to single serial pipeline due to Bitmap-based texture inputs instead of texture IDs for multiple textures
   - No support for memory optimizations like ping-pong buffers

3. Development Challenges
   - Insufficient error handling and debugging capabilities
   - Limited control over resource allocation
   - Insufficient development tooling
   - Limited View component support
   - No active maintenance since 2021
   

## Goals

KraftShade aims to address these limitations with:

1. Modern Architecture
   - Kotlin-first design with coroutines support
   - Easy to use DSL for pipeline construction

2. Flexible Pipeline
   - Composable effects
   - Support for complex multi-pass rendering
   - Efficient texture and buffer management
   - On/Off-screen rendering support

3. Developer Experience
   - Flexible View components & Compose integration
   - Flexible input system for effect adjustment and re-rendering including animated effects
   - Debugging tools and utilities

4. Performance
   - Minimal overhead
   - Smart resource management
   - Optimized rendering pipeline

## Key Features

### Core Components

1. Graphics Infrastructure
   - `GlEnv`: OpenGL ES environment and context management
   - `KraftTextureView`: Base view for OpenGL rendering
   - `AnimatedKraftTextureView`: Animation support with Choreographer

2. Shader System
   - `KraftShader`: Base shader implementation
   - Built-in effects:
     * Saturation
     * Emboss
     * Look-up Table (LUT)
     * Convolution (3x3)
     * Alpha Blending
     * Circle Drawing
     * (remove some useless ones or move to demo module)
     * (port all from GPUImage)

3. Pipeline Architecture
   - (To add)

## Usage

### Basic Setup

```kotlin
// Initialize in Application class
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        KraftLogger.logLevel = KraftLogLevel.DEBUG
        KraftLogger.throwOnError = true
    }
}
```

### Creating a Simple Effect

```kotlin
class MyCustomShader : KraftShader() {
    override fun loadFragmentShader(): String = """
        precision mediump float;
        varying vec2 textureCoordinate;
        uniform sampler2D inputImageTexture;
        
        void main() {
            vec4 color = texture2D(inputImageTexture, textureCoordinate);
            gl_FragColor = color;
        }
    """.trimIndent()
}
```

### Using the Pipeline

```kotlin
val pipeline = SerialTextureInputPipeline(glEnv).apply {
    addEffect(SaturationKraftShader())
    addEffect(MyCustomShader())
}

kraftTextureView.runGlTask { windowSurface ->
    pipeline.drawTo(windowSurface)
}
```

## Logging
```kotlin
// Configure logging
KraftLogger.logLevel = KraftLogLevel.DEBUG
KraftLogger.throwOnError = true // Only for development
```

### Custom Effects

Create custom effects by extending `KraftShader`:

```kotlin
class CustomEffect : KraftShader() {
    private var intensity by GlUniformDelegate("intensity")
    
    override fun loadFragmentShader(): String = """
        // Your shader code here
    """.trimIndent()
    
    fun setIntensity(value: Float) {
        // no need to get the location anymore. GlUniformDelegate will handle it.
        intensity = value
    }
}
```

## Installation (to be updated)

```gradle
dependencies {
    implementation 'com.cardinalblue:kraftshade:1.0.0'
}
```

## Acknowledgments

Special thanks to [GPUImage for Android](https://github.com/cats-oss/android-gpuimage) which inspired the development of KraftShade.

## License

```
Copyright 2024 Cardinal Blue

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
