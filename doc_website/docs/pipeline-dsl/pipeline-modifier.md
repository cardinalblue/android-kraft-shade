---
sidebar_position: 5
---

# PipelineModifier

PipelineModifier is a powerful feature in KraftShade that allows you to create reusable, composable pipeline components. These modifiers encapsulate complex pipeline operations into reusable units that can be easily integrated into larger pipeline setups.

## Overview

A PipelineModifier is essentially a pre-configured set of pipeline steps that can be applied within any pipeline setup scope. This promotes code reuse, maintainability, and allows you to build complex effects by composing simpler, well-tested components.

## Creating a PipelineModifier

To create a PipelineModifier, you need to extend one of the base modifier classes:

- `PipelineModifierWithInputTexture` - For modifiers that process a single input texture
- `PipelineModifierWithBuffers` - For modifiers that work with multiple buffer inputs/outputs

### Example: Circular Blur with Edge Preservation

Here's a complete example of creating a PipelineModifier that applies circular blur while preserving edges:

```kotlin
package com.cardinalblue.effects.effect.pipeline

import com.cardinalblue.effects.effect.blur.PreserveEdgeFilter
import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
import com.cardinalblue.kraftshade.pipeline.PipelineModifierWithInputTexture
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider
import com.cardinalblue.kraftshade.shader.builtin.CircularBlurKraftShader

class CircularBlurPreservingEdgePipelineModifier(
    private val amount: Input<Float>,
    private val repeat: Input<Float>,
    private val edgeStrength: Input<Float>,
) : PipelineModifierWithInputTexture() {
    
    override suspend fun GraphPipelineSetupScope.addStep(
        inputTexture: TextureProvider,
        outputBuffer: GlBufferProvider
    ) {
        // Create intermediate buffer references
        val (edgeTexture, blurredTexture) = createBufferReferences("edge", "blurred")

        // Step 1: Detect edges using Sobel operator
        with(SobelEdgeDetectionModifier(edgeStrength)) {
            addStep(inputTexture, edgeTexture)
        }

        // Step 2: Apply circular blur
        step(
            shader = CircularBlurKraftShader(),
            targetBuffer = blurredTexture,
        ) { shader ->
            shader.setInputTexture(inputTexture)
            shader.amount = amount.get()
            shader.repeat = repeat.get()
        }

        // Step 3: Combine original, blurred, and edge textures
        step(
            shader = PreserveEdgeFilter(),
            targetBuffer = outputBuffer,
        ) { shader ->
            shader.setInputTexture(inputTexture)
            shader.setBlurredTexture(blurredTexture)
            shader.setEdgeTexture(edgeTexture)
        }
    }
}
```

## Using PipelineModifier in Pipelines

Once you've created a PipelineModifier, you can use it within any pipeline setup scope using the `with` scope function:

### In Serial Pipelines

```kotlin
serialSteps(inputTexture, outputBuffer) {
    val blurModifier = CircularBlurPreservingEdgePipelineModifier(
        amount = blurAmount,
        repeat = constInput(60f),
        edgeStrength = edgeStrength
    )
    
    with(blurModifier) { addStep() }
    
    step(BrightnessKraftShader()) { shader ->
        shader.brightness = brightness.get()
    }
    
    // Add more steps as needed
}
```

### In Graph Pipelines

```kotlin
graphPipeline(inputTexture, outputBuffer) {
    val edgeDetection = SobelEdgeDetectionModifier(constInput(1.0f))
    val blurEffect = CircularBlurPreservingEdgePipelineModifier(
        amount = blurAmount,
        repeat = constInput(30f),
        edgeStrength = constInput(0.5f)
    )
    
    // Use modifiers within graph nodes
    node("preprocess") {
        with(edgeDetection) { addStep() }
    }
    
    node("blur") {
        with(blurEffect) { addStep() }
    }
    
    // Connect nodes...
}
```

## Benefits of PipelineModifier

### 1. **Code Reusability**
Create once, use everywhere. PipelineModifiers can be shared across different effects and projects.

### 2. **Encapsulation**
Hide complex implementation details behind a simple interface. Users only need to know the inputs and outputs.

### 3. **Testing**
Modifiers can be tested independently, ensuring each component works correctly before integration.

### 4. **Composition**
Build complex effects by combining simple, well-tested modifiers.

### 5. **Maintainability**
Changes to a modifier's implementation automatically propagate to all uses without code duplication.

## Best Practices

### 1. **Single Responsibility**
Each modifier should have a single, well-defined purpose. Avoid creating monolithic modifiers that do too much.

### 2. **Clear Naming**
Use descriptive names that clearly indicate what the modifier does.

### 3. **Input Validation**
Validate inputs within the modifier to ensure robust error handling.

### 4. **Documentation**
Document the modifier's purpose, inputs, outputs, and any special considerations.

### 5. **Testing**
Create unit tests for your modifiers to ensure they behave correctly in isolation.

## Common Patterns

### Input Texture Modifier
For modifiers that process a single texture:

```kotlin
class SimpleBlurModifier(private val radius: Input<Float>) : PipelineModifierWithInputTexture() {
    override suspend fun GraphPipelineSetupScope.addStep(
        inputTexture: TextureProvider,
        outputBuffer: GlBufferProvider
    ) {
        step(BlurKraftShader()) { shader ->
            shader.inputTexture = inputTexture
            shader.radius = radius.get()
        }
    }
}
```

### Multi-Input Modifier
For modifiers that work with multiple inputs:

```kotlin
class BlendModifier(
    private val blendMode: Input<BlendMode>,
    private val opacity: Input<Float>
) : PipelineModifierWithBuffers() {
    override suspend fun GraphPipelineSetupScope.addStep(
        inputs: List<TextureProvider>,
        outputBuffer: GlBufferProvider
    ) {
        require(inputs.size == 2) { "Blend modifier requires exactly 2 inputs" }
        
        step(BlendKraftShader()) { shader ->
            shader.baseTexture = inputs[0]
            shader.overlayTexture = inputs[1]
            shader.blendMode = blendMode.get()
            shader.opacity = opacity.get()
        }
    }
}
```

## Integration with Existing Code

PipelineModifiers integrate seamlessly with existing KraftShade code:

- They work with both serial and graph pipelines
- They support all input types (constants, sliders, external inputs)
- They can be nested within other modifiers
- They maintain the same performance characteristics as inline pipeline steps

## Next Steps

- Explore the [Serial Pipeline](./serial-pipeline.md) documentation for more examples
- Learn about [Graph Pipeline](./graph-pipeline.md) for complex multi-pass effects
- Check out [Best Practices](./best-practices.md) for advanced usage patterns