# Multiple Vertex Attributes Support for Video Export

This document explains how to use the new vertex attribute support in KraftShade for video export with Media3 Transformer.

## Overview

The new vertex attribute support allows you to create complex vertex shader effects with custom per-vertex data that can change over time during video export. This is useful for:

- Wave and distortion effects with per-vertex parameters
- Particle systems with individual particle properties
- Mesh deformations with custom vertex data
- Complex animations that require per-vertex control

## Key Components

### 1. MultiAttributeKraftShader

Base class for shaders that support multiple vertex attributes beyond the standard position and texture coordinates.

```kotlin
class MyCustomShader : MultiAttributeKraftShader() {
    override fun loadVertexShader(): String = """
        attribute vec4 position;
        attribute vec4 inputTextureCoordinate;
        attribute vec2 customData;  // Your custom attribute
        
        varying vec2 textureCoordinate;
        
        void main() {
            // Use customData in vertex calculations
            gl_Position = position;
            textureCoordinate = inputTextureCoordinate.xy;
        }
    """
    
    override fun loadFragmentShader(): String = """
        // Fragment shader code
    """
}
```

### 2. KraftShadePipelineEffectWithAttributes

Media3 effect that supports custom vertex attributes for video processing.

```kotlin
val effect = KraftShadePipelineEffectWithAttributes(
    context = context,
    provider = videoEffectProvider,
    vertexAttributeProvider = { presentationTimeUs ->
        // Return vertex attributes based on current time
        listOf(
            VertexAttributeData(
                name = "customData",
                size = 2,
                buffer = createDataBuffer(presentationTimeUs)
            )
        )
    }
)
```

### 3. VertexAttributeData

Data class that defines a vertex attribute:

```kotlin
data class VertexAttributeData(
    val name: String,           // Attribute name in shader
    val size: Int,              // Components per vertex (1-4)
    val type: Int = GLES30.GL_FLOAT,
    val normalized: Boolean = false,
    val stride: Int = 0,
    val buffer: FloatBuffer     // Vertex data
)
```

## Usage Example with SaveResultButtonForAnimation

Here's how to use the new functionality with video export:

```kotlin
@Composable
fun MyEffectScreen() {
    var sourceImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageSize by remember { mutableStateOf<GlSize?>(null) }
    
    SaveResultButtonForAnimation(
        fileNamePrefix = "vertexEffect",
        imageSizeProvider = { imageSize },
        durationMilliseconds = 5000,
        sourceImageUri = sourceImageUri,
        createPipelineModifierFunction = { timeInput ->
            // Create the effect with vertex attributes
            val effect = createVertexAttributeEffect(context)
            
            // Return pipeline modifier
            object : PipelineModifierWithoutInputTexture {
                override fun GraphPipelineSetupScope.addStep(targetBuffer: BufferReference) {
                    // Pipeline setup handled by the effect
                }
            }
        },
        lastStepBlendShader = NormalBlendingKraftShader()
    )
}

private fun createVertexAttributeEffect(context: Context): KraftShadePipelineEffectWithAttributes {
    val shader = WaveDistortionShader()
    
    return KraftShadePipelineEffectWithAttributes(
        context = context,
        provider = { buffer, timeInput, videoTexture ->
            pipeline(buffer) {
                step(shader, buffer) { s ->
                    s.setInputTexture(videoTexture)
                    s.setUniforms(mapOf("time" to timeInput.value))
                }
            }
        },
        vertexAttributeProvider = { presentationTimeUs ->
            val time = presentationTimeUs / 1_000_000f
            
            // Create animated vertex data
            val waveData = FloatArray(8) { i ->
                sin(time + i * 0.5f) * 0.1f
            }
            
            listOf(
                VertexAttributeData(
                    name = "waveParams",
                    size = 2,
                    buffer = waveData.asFloatBuffer()
                )
            )
        }
    )
}
```

## Complete Working Example

See `VertexAttributeExampleUsage.kt` for complete examples including:

1. **Wave Distortion Effect**: Demonstrates animated wave parameters per vertex
2. **Particle System**: Shows how to handle multiple attributes for particle rendering

## Best Practices

1. **Performance**: Pre-allocate buffers when possible instead of creating new ones each frame
2. **Memory Management**: Reuse FloatBuffers to avoid garbage collection
3. **Attribute Naming**: Use descriptive names that match your vertex shader
4. **Size Optimization**: Only include necessary components (e.g., use size=2 for 2D data)

## Integration with Existing Code

The new classes are designed to work alongside existing KraftShade functionality:

- Use `KraftShadePipelineEffect` for standard effects without custom vertex attributes
- Use `KraftShadePipelineEffectWithAttributes` when you need custom vertex data
- Both can be used in the same video composition

## Limitations

- Vertex attributes are provided per frame, not per draw call
- All vertices in a draw call share the same attribute buffers
- The number of attributes is limited by the GPU (typically 16 total)

## Troubleshooting

1. **Attribute not found**: Check that the attribute name matches exactly in shader and code
2. **Rendering issues**: Verify buffer sizes match the number of vertices
3. **Performance**: Profile buffer allocation and consider pre-allocation strategies