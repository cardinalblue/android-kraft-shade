---
sidebar_position: 2
---

# GlUniformDelegate

The `GlUniformDelegate` is a key component of KraftShade's shader system that provides an elegant and efficient way to manage shader uniform parameters. It leverages Kotlin's property delegation feature to create a clean, type-safe API for setting shader parameters. For more information about how this fits into the shader system, see the [KraftShader documentation](./kraft-shader.md).

## Overview

In OpenGL, uniform variables are used to pass data from the application to the shader program. Setting these values typically requires:

1. Getting the location of the uniform variable in the shader program
2. Calling the appropriate `glUniform*` function to set the value

The `GlUniformDelegate` abstracts away these details, allowing shader parameters to be defined and used as simple Kotlin properties.

## How It Works

The `GlUniformDelegate` works through a deferred update mechanism:

1. When a shader parameter is set, the value is stored locally in the delegate
2. A task is queued with the shader's `runOnDraw` method
3. During the next draw call, the `runPendingOnDrawTasks` method executes all queued parameter updates
4. The uniform value is then sent to the GPU using the appropriate `glUniform*` function

This approach offers several advantages:
- Multiple parameter changes are batched together
- Updates only happen when needed (during draw calls)
- Redundant updates are avoided by tracking value changes

## Implementation Details

The `GlUniformDelegate` is implemented as a Kotlin property delegate that implements the `ReadWriteProperty` interface:

```kotlin
open class GlUniformDelegate<T : Any>(
    protected val name: String,
    protected val required: Boolean = true,
    protected val checkValueForSet: (T) -> Unit = {},
) : ReadWriteProperty<KraftShader, T> {
    // Implementation details...
}
```

### Key Components

1. **Uniform Location Caching**:
   - The location of the uniform in the shader program is cached for performance
   - The location is lazily initialized when first needed

2. **Value Storage**:
   - The current value is stored locally in the delegate
   - A hash code of the value is stored to detect changes

3. **Deferred Updates**:
   - When a value is set, a task is queued with the shader's `runOnDraw` method
   - The task will be executed during the next draw call

4. **Type Handling**:
   - The delegate supports various types (Float, Int, Boolean, vectors, matrices)
   - Each type is mapped to the appropriate `glUniform*` function

## Usage Example

Here's how `GlUniformDelegate` is used in a shader class:

```kotlin
class OpacityKraftShader(opacity: Float = 1.0f) : TextureInputKraftShader() {
    var opacity: Float by GlUniformDelegate("opacity")

    init {
        this.opacity = opacity
    }

    override fun loadFragmentShader(): String = OPACITY_FRAGMENT_SHADER
}
```

In this example:
1. The `opacity` property is defined using `GlUniformDelegate`
2. The delegate is configured to update the "opacity" uniform in the shader
3. When `opacity` is set, the update is queued for the next draw call

## Parameter Update Flow

The following sequence illustrates how parameter updates flow through the system:

1. **Property Access**:
   ```kotlin
   shader.opacity = 0.5f
   ```

2. **Delegate Setter**:
   - The `setValue` method of the delegate is called
   - The value is stored locally
   - A task is queued with the shader's `runOnDraw` method

3. **Draw Call**:
   - When `shader.draw()` is called, `runPendingOnDrawTasks` executes all queued tasks
   - The delegate's task retrieves the uniform location
   - The appropriate `glUniform*` function is called to set the value in the shader program

4. **Rendering**:
   - The shader program uses the updated uniform value during rendering

## Performance Optimizations

The `GlUniformDelegate` includes several optimizations:

1. **Value Change Detection**:
   - The delegate computes a hash code for the current value
   - If the new value has the same hash code, the update is skipped
   - This prevents redundant GPU updates when values haven't changed

2. **Location Caching**:
   - Uniform locations are cached to avoid repeated calls to `glGetUniformLocation`
   - This reduces CPU overhead during parameter updates

3. **Batched Updates**:
   - All parameter updates are batched and applied during the draw call
   - This minimizes OpenGL state changes and improves performance

## Supported Types

The `GlUniformDelegate` supports a wide range of types:

- **Primitive Types**: Boolean, Int, Float
- **Array Types**: FloatArray, GlFloatArray
- **Vector Types**: GlVec2, GlVec3, GlVec4
- **Matrix Types**: GlMat2, GlMat3, GlMat4
- **Size Types**: GlSize, GlSizeF

Each type is automatically mapped to the appropriate `glUniform*` function.
