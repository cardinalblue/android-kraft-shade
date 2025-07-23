---
sidebar_position: 3
---

# Architecture Overview

KraftShade is built with a modular, layered architecture that provides flexibility, performance, and ease of use. This document provides a high-level overview of the KraftShade architecture, its core components, and how they interact.

## High-Level Architecture

KraftShade's architecture is organized into several key layers:

```mermaid
flowchart TD
    classDef core fill:#f96,stroke:#333,stroke-width:2px
    classDef view fill:#9cf,stroke:#333,stroke-width:2px
    classDef dsl fill:#9f9,stroke:#333,stroke-width:2px
    classDef pipeline fill:#f6c,stroke:#933,stroke-width:4px,stroke-dasharray: 5 2
    
    App[Application] --> Views
    
    subgraph Views["View Components"]
        direction TB
        AndroidViews["Android Views"] --> KraftTextureView
        JetpackCompose["Jetpack Compose"] --> KraftShadeView
        
        KraftTextureView --> KraftEffectTextureView
        KraftEffectTextureView --> AnimatedKraftTextureView
        
        KraftShadeView --> KraftShadeEffectView
        KraftShadeEffectView --> KraftShadeAnimatedView
    end
    
    Views --> DSL
    
    subgraph DSL["DSL Layer"]
        direction TB
        PipelineDSL["Pipeline DSL"]
        KraftBitmapDSL["Bitmap DSL"]
    end
    
    DSL --> Core
    
    subgraph Core["Core Components"]
        direction TB

        GlEnv["OpenGL Environment (GlEnv)"]
        
        PipelineSystem["Pipeline System (Orchestration)"]

        PipelineSystem --> GlEnv
        
        KraftShaders --> GlEnv

        PipelineSystem --> InputSystem["Input System"]
        PipelineSystem --> KraftShaders
    end
    
    class Core core
    class Views view
    class DSL dsl
    class PipelineSystem pipeline
```

## Core Components

### OpenGL Environment (GlEnv)

The OpenGL Environment is the foundation of KraftShade, providing a managed context for OpenGL operations:

- Handles OpenGL context creation and management
- Manages the lifecycle of OpenGL resources
- Provides utilities for common OpenGL operations
- Ensures thread-safety for OpenGL operations

### Shader System

The Shader System is responsible for managing and executing GLSL shaders:

```mermaid
classDiagram
    class KraftShader {
        <<abstract>>
        -glProgId: Int
        -uniformLocationCache: Map
        +init(): Boolean
        +draw(bufferSize: GlSize, isScreenCoordinate: Boolean)
        +updateTexelSize()
        +beforeActualDraw(isScreenCoordinate: Boolean)
        +actualDraw(isScreenCoordinate: Boolean)
        +destroy()
    }
    
    class TextureInputKraftShader {
        <<abstract>>
        -input: KraftShaderTextureInput
        +drawWithInput(texture: Texture, size: GlSize, isScreenCoordinate: Boolean)
    }
    
    class TwoTextureInputKraftShader {
        <<abstract>>
        -secondInput: KraftShaderTextureInput
        +drawWithInput(texture1: Texture, texture2: Texture, size: GlSize, isScreenCoordinate: Boolean)
    }
    
    class ThreeTextureInputKraftShader {
        <<abstract>>
        -thirdInput: KraftShaderTextureInput
        +drawWithInput(texture1: Texture, texture2: Texture, texture3: Texture, size: GlSize, isScreenCoordinate: Boolean)
    }
    
    class KraftShaderFactory {
        +createShader<T: KraftShader>(): T
    }
    
    KraftShader <|-- TextureInputKraftShader
    TextureInputKraftShader <|-- TwoTextureInputKraftShader
    TextureInputKraftShader <|-- Sample3x3KraftShader
    TextureInputKraftShader <|-- SingleDirectionForTwoPassSamplingKraftShader
    TwoTextureInputKraftShader <|-- ThreeTextureInputKraftShader
```

Key components:

- **KraftShader**: Abstract base class for all shaders
- **TextureInputKraftShader**: Base class for shaders that take a texture input
- **TwoTextureInputKraftShader**: Base class for shaders that take two texture inputs
- **ThreeTextureInputKraftShader**: Base class for shaders that take three texture inputs
- **KraftShaderFactory**: Factory for creating shader instances

### Pipeline System

The Pipeline System orchestrates the execution of shaders in a defined sequence:

```mermaid
classDiagram
    class EffectExecution {
        <<interface>>
        +run()
        +destroy()
        +onBufferSizeChanged(size: GlSize)
    }
    
    class Pipeline {
        -steps: List~PipelineStep~
        -bufferPool: TextureBufferPool
        +addStep(step: PipelineStep)
        +run()
        +destroy()
    }
    
    class PipelineStep {
        <<abstract>>
        +run(scope: PipelineRunningScope)
    }
    
    class RunShaderStep {
        -shader: KraftShader
        +run(scope: PipelineRunningScope)
    }
    
    class RunTaskStep {
        -task: suspend () -> Unit
        +run(scope: PipelineRunningScope)
    }
    
    class TextureBufferPool {
        -buffers: Map
        +get(bufferReference: BufferReference): TextureBuffer
        +recycle(bufferReference: BufferReference)
        +delete()
    }
    
    EffectExecution <|.. Pipeline
    PipelineStep <|-- RunShaderStep
    PipelineStep <|-- RunTaskStep
    Pipeline --> PipelineStep
    Pipeline --> TextureBufferPool
```

Key components:

- **EffectExecution**: Interface for executing effects
- **Pipeline**: Main class for executing a sequence of shader operations
- **PipelineStep**: Abstract base class for pipeline steps
- **RunShaderStep**: Step that runs a shader
- **RunTaskStep**: Step that runs a custom task
- **TextureBufferPool**: Manages and recycles texture buffers

### Input System

The Input System provides a way to feed dynamic values into shaders:

- Supports time-based animations
- Allows for user interaction inputs
- Enables dynamic parameter changes

## DSL Layer

KraftShade provides a Kotlin DSL for building shader pipelines:

```mermaid
classDiagram
    class BasePipelineSetupScope {
        <<abstract>>
        +step(shader: KraftShader, configure: (KraftShader) -> Unit)
    }
    
    class GraphPipelineSetupScope {
        +stepWithInputTexture(shader: TextureInputKraftShader, inputTexture: Texture)
    }
    
    class SerialTextureInputPipelineScope {
        +step(shader: TextureInputKraftShader, configure: (TextureInputKraftShader) -> Unit)
    }
    
    class KraftBitmapDslScope {
        +withPipeline(setup: BasePipelineSetupScope.() -> Unit): Bitmap
    }
    
    BasePipelineSetupScope <|-- GraphPipelineSetupScope
    BasePipelineSetupScope <|-- SerialTextureInputPipelineScope
```

Key components:

- **BasePipelineSetupScope**: Base scope for pipeline setup
- **GraphPipelineSetupScope**: Scope for setting up graph pipelines
- **SerialTextureInputPipelineScope**: Scope for setting up serial pipelines
- **KraftBitmapDslScope**: Scope for creating bitmaps with effects

## View Components

KraftShade provides view components for both traditional Android Views and Jetpack Compose:

### Android Views

```mermaid
classDiagram
    class KraftTextureView {
        +runGlTask(task: KraftTextureViewTask): Job
        +terminate()
    }
    
    class KraftEffectTextureView {
        +setEffect(effectExecutionProvider: EffectExecutionProvider)
        +requestRender()
    }
    
    class AnimatedKraftTextureView {
        +setEffectWithTimeInput(effectExecutionProvider: AnimatedEffectExecutionProvider)
        +setEffectAndPlay(effectExecutionProvider: AnimatedEffectExecutionProvider)
        +setEffectAndPause(effectExecutionProvider: AnimatedEffectExecutionProvider)
        +play()
        +stop()
    }
    
    KraftTextureView <|-- KraftEffectTextureView
    KraftEffectTextureView <|-- AnimatedKraftTextureView
```

Key components:

- **KraftTextureView**: Base view for OpenGL rendering
- **KraftEffectTextureView**: View for rendering shader effects
- **AnimatedKraftTextureView**: View for rendering animated shader effects

### Jetpack Compose

```mermaid
classDiagram
    class KraftShadeBaseState {
        +runGlTask(task: KraftTextureViewTask): Job
        +terminate()
    }
    
    class KraftShadeEffectState {
        +setEffect(effectExecutionProvider: EffectExecutionProvider)
        +requestRender()
    }
    
    class KraftShadeAnimatedState {
        +setEffectAndPlay(effectExecutionProvider: AnimatedEffectExecutionProvider)
        +setEffectAndPause(effectExecutionProvider: AnimatedEffectExecutionProvider)
        +play()
        +stop()
    }
    
    KraftShadeBaseState <|-- KraftShadeEffectState
    KraftShadeEffectState <|-- KraftShadeAnimatedState
```

Key components:

- **KraftShadeBaseState**: Base state for Compose integration
- **KraftShadeEffectState**: State for rendering shader effects in Compose
- **KraftShadeAnimatedState**: State for rendering animated shader effects in Compose

## Data Flow

The following diagram illustrates the typical data flow in a KraftShade application:

```mermaid
flowchart LR
    classDef input fill:#f96,stroke:#333,stroke-width:2px
    classDef process fill:#9cf,stroke:#333,stroke-width:2px
    classDef output fill:#9f9,stroke:#333,stroke-width:2px
    
    Input[Input Texture] --> Pipeline
    Params[Shader Parameters] --> Pipeline
    
    subgraph Pipeline["Pipeline Execution"]
        direction TB
        Shader1[Shader 1] --> Buffer1[Intermediate Buffer]
        Buffer1 --> Shader2[Shader 2]
        Shader2 --> Buffer2[Intermediate Buffer]
        Buffer2 --> ShaderN[Shader N]
    end
    
    Pipeline --> Output[Output Texture/Surface]
    
    class Input input
    class Pipeline process
    class Output output
```

1. Input textures (from images, camera, etc.) enter the pipeline
2. Shaders process the textures, applying various effects
3. Intermediate buffers store results between shader steps
4. The final output is rendered to a texture or surface
5. The view component displays the result

## Resource Management

KraftShade efficiently manages OpenGL resources:

- **Automatic Buffer Recycling**: Intermediate buffers are automatically recycled
- **Texture Reuse**: Textures are reused when possible to reduce memory allocation
- **Proper Cleanup**: Resources are properly released when no longer needed
- **Thread Safety**: OpenGL operations are performed on the appropriate thread

## Conclusion

KraftShade's architecture is designed to provide a flexible, efficient, and easy-to-use framework for GPU-accelerated graphics processing on Android. The modular design allows for easy extension and customization, while the DSL provides a clean and intuitive API for building complex shader pipelines.

For more details on specific components, refer to the corresponding sections in the documentation.
