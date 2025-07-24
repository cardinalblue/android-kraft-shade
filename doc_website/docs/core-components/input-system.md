---
sidebar_position: 4
---

# Input System

The Input System is a core component of the Kraft Shade framework that provides a flexible and powerful mechanism for handling various types of inputs in shader pipelines. It enables dynamic parameter control, animation, and interactive effects by abstracting input sources and providing a consistent interface for accessing input values.

## Overview

In graphics programming, shaders often require various inputs to control their behavior - from simple parameters like intensity or color to more complex inputs like time or user interactions. The Kraft Shade Input System addresses these needs by providing:

- A unified interface for all types of inputs
- Support for both constant and dynamically changing values
- Efficient caching and dirty state management for performance
- Transformation capabilities to modify input values
- Integration with the Pipeline system for frame-based processing

The Input System is designed to be both simple to use for common cases and flexible enough for complex scenarios. It plays a crucial role in creating dynamic, interactive visual effects by connecting external data sources to shader parameters.

## Input Types

The Input System supports various types of inputs that can be used in shader pipelines. At its core is the abstract `Input<T>` class, which serves as the base for all input implementations.

### Base Input Class

The `Input<T>` class is the foundation of the input system:

```kotlin
abstract class Input<T : Any> {
    internal abstract fun Pipeline.internalGet(): T
}
```

This abstract class defines a generic type parameter `T` that represents the type of value the input provides. All input implementations extend this class and implement the `internalGet()` function, which is an extension function on `Pipeline`.

### Immutable Inputs

Immutable inputs provide constant values that don't change during the lifetime of a pipeline. They're useful for fixed parameters that don't need to be updated dynamically.

```kotlin
class ImmutableInput<T : Any>(
    private val value: T
) : Input<T>() {
    override fun Pipeline.internalGet(): T {
        return value
    }
}

// Helper function to create immutable inputs
fun <T : Any> constInput(value: T) = ImmutableInput(value)
```

Example usage:
```kotlin
// Create a constant float input with value 0.5
val intensity = constInput(0.5f)
```

### Sampled Inputs

Sampled inputs provide values that can change over time. They implement a caching mechanism to ensure consistent values within the same frame and only recalculate when necessary.

```kotlin
abstract class SampledInput<T : Any> : Input<T>() {
    private var lastSample: T? = null
    private var isDirty: Boolean = true

    protected abstract fun Pipeline.provideSample(): T

    override fun Pipeline.internalGet(): T {
        trackInput(this@SampledInput)
        if (isDirty) {
            lastSample = provideSample()
            isDirty = false
        }
        return lastSample!!
    }

    internal open fun markDirty() {
        isDirty = true
    }
}
```

The `SampledInput` class:
- Maintains a cache of the last sampled value
- Tracks whether the input is "dirty" and needs to be resampled
- Provides a `markDirty()` method that's called at the beginning of each frame
- Requires subclasses to implement `provideSample()` to generate new values

A convenient way to create sampled inputs is using the `sampledInput` function:

```kotlin
fun <T : Any> sampledInput(action: PipelineRunningScope.() -> T) : SampledInput<T> {
    return WrappedSampledInput(action)
}
```

Example usage:
```kotlin
// Create a sampled input that provides a random value each frame
val randomValue = sampledInput { Math.random().toFloat() }
```

### Time Input

The `TimeInput` class is a specialized `SampledInput` that provides time-based values, useful for animations and time-dependent effects:

```kotlin
class TimeInput(
    private val getTime: () -> Long = { System.currentTimeMillis() }
) : SampledInput<Float>() {
    // Implementation details...
    
    fun reset() { /* ... */ }
    fun start() { /* ... */ }
    fun pause() { /* ... */ }
}
```

The `TimeInput` class:
- Provides the elapsed time in seconds as a Float
- Supports starting, pausing, and resetting the timer
- Can be customized with a custom time source for testing

Example usage:
```kotlin
// Create a time input and start it
val time = CommonInputs.time()

// Create a paused time input
val pausedTime = CommonInputs.time(start = false)
pausedTime.start() // Start it later
```

## Dynamic Inputs

Dynamic inputs allow for runtime changes to shader parameters and behaviors. They're essential for creating interactive and animated effects.

### Understanding the Sampling Mechanism

Sampled inputs use a sophisticated mechanism to ensure efficient and consistent value provision:

1. At the beginning of each frame, all sampled inputs are marked as "dirty"
2. When a sampled input's value is requested for the first time in a frame:
   - The input checks if it's dirty
   - If dirty, it calls `provideSample()` to get a new value and caches it
   - The dirty flag is cleared
3. Subsequent requests within the same frame return the cached value
4. This ensures that even if an input is used multiple times in a frame, the value remains consistent

This mechanism is particularly important for inputs like `TimeInput` where consistent values within a frame are crucial for correct rendering.

### Creating Custom Sampled Inputs

You can create custom sampled inputs by extending the `SampledInput` class:

```kotlin
class MyCustomInput : SampledInput<Float>() {
    private var value = 0f
    
    override fun Pipeline.provideSample(): Float {
        // Increment the value each frame
        value += 0.01f
        return value
    }
    
    // Additional methods as needed
}
```

Or more simply using the `sampledInput` function:

```kotlin
var counter = 0f
val incrementingValue = sampledInput {
    counter += 0.01f
    counter
}
```

### Using TimeInput for Animations

The `TimeInput` class is particularly useful for creating animations:

```kotlin
// Create a time input
val time = CommonInputs.time()

// Use it in a shader
shader.setUniform("time", time)
```

This allows creating effects that change over time, such as:
- Oscillating parameters
- Animated transitions
- Procedural animations
- Time-based color changes

## Input Transformations

Input transformations enable the modification and processing of input values before they are used in shaders. This allows for creating complex behaviors from simple inputs.

### Mapping Inputs

The `map` extension function allows transforming the value of any input:

```kotlin
fun <T : Any, R : Any> Input<T>.map(mapper: (T) -> R): Input<R>
```

This function automatically chooses the appropriate implementation based on the input type:
- For regular inputs, it returns a `MappedInput`
- For sampled inputs, it returns a `MappedSampledInput` to preserve the sampling behavior

Example usage:
```kotlin
// Create a time input
val time = CommonInputs.time()

// Map it to a sine wave oscillating between 0 and 1
val oscillator = time.map { sin(it * Math.PI.toFloat() * 2) * 0.5f + 0.5f }
```

### Utility Transformations

The input system provides several utility transformations:

#### Scale

```kotlin
fun Input<Float>.scale(scale: Float): Input<Float>
```

Multiplies the input value by a constant factor.

Example:
```kotlin
// Create a time input that runs twice as fast
val fastTime = CommonInputs.time().scale(2f)
```

#### Bounce Between

```kotlin
fun Input<Float>.bounceBetween(value1: Float, value2: Float): Input<Float>
```

Creates an input that oscillates between two values.

Example:
```kotlin
// Create an input that bounces between 0 and 1
val bouncing = CommonInputs.time().bounceBetween(0f, 1f)
```

### Chaining Transformations

Transformations can be chained to create complex behaviors:

```kotlin
// Create a time input
val time = CommonInputs.time()

// Create a value that oscillates between 0.2 and 0.8 at half speed
val oscillator = time
    .scale(0.5f)
    .map { sin(it * Math.PI.toFloat() * 2) * 0.3f + 0.5f }
```

### Creating Custom Transformations

You can create custom transformations by implementing extension functions on `Input<T>`:

```kotlin
fun Input<Float>.squared(): Input<Float> = map { it * it }

fun Input<Float>.clamp(min: Float, max: Float): Input<Float> = 
    map { kotlin.math.max(min, kotlin.math.min(it, max)) }
```

## Best Practices

### When to Use Different Input Types

- Use `ImmutableInput` (via `constInput()`) for values that don't change
- Use `SampledInput` (via `sampledInput()`) for values that need to be recalculated each frame
- Use `TimeInput` (via `CommonInputs.time()`) for time-based animations
- Use transformations to derive complex behaviors from simple inputs

### Performance Considerations

- Sampled inputs recalculate their values only once per frame, even if accessed multiple times
- Complex calculations in `provideSample()` methods should be optimized
- Consider caching expensive calculations even across frames if appropriate
- Be mindful of creating too many input transformations in performance-critical code

### Integration with Shaders

Inputs are typically used to set shader uniforms:

```kotlin
// Create inputs
val time = CommonInputs.time()
val intensity = constInput(0.8f)

// Use them in a shader
shader.setUniform("time", time)
shader.setUniform("intensity", intensity)
```

This creates a dynamic connection between the inputs and the shader, allowing the shader's behavior to change based on the input values.