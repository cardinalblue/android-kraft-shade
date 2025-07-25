# KraftShade Documentation Plan

This document outlines the structure and content plan for the KraftShade documentation website. The documentation is organized into sections and articles, following Docusaurus's structure.

## Overview

This document outlines the structure, current state, and future plans for the KraftShade documentation. It serves as a guide for documentation contributors and maintainers.

## Documentation Structure

The documentation will be organized into the following main sections:

## 1. Introduction
- **Overview** - Introduction to KraftShade, its purpose, and key features
- **Why KraftShade** - Comparison with other graphics libraries and unique selling points
- **Architecture Overview** - High-level overview of the KraftShade architecture

## 2. Getting Started
- **Installation** - How to add KraftShade to your project
- **Quick Start Guide** - Simple example to get up and running
- **Basic Concepts** - Introduction to key concepts and terminology
- **First Effect** - Step-by-step guide to creating your first shader effect

## 3. Core Components
- **GlEnv (Graphics Environment)** - Detailed explanation of the OpenGL environment
- **Shader System** - In-depth guide to the shader system
  - **KraftShader** - Base shader implementation
  - **GlUniformDelegate** - Uniform handling
  - **Texture Inputs** - Managing texture inputs
- **Pipeline System** - Comprehensive guide to the pipeline system
  - **Pipeline Running Flow** - How the pipeline executes per frame
  - **Buffer Management** - How buffers are managed and recycled
- **Input System** - Guide to the input system

## 4. View Components
- **Android Views** - Using KraftShade with Android Views
  - **KraftTextureView** - Base OpenGL rendering view
  - **KraftEffectTextureView** - Effect-enabled view
  - **AnimatedKraftTextureView** - Animation support
- **Jetpack Compose Integration** - Using KraftShade with Jetpack Compose
  - **KraftShadeView** - Compose wrapper for KraftTextureView
  - **KraftShadeEffectView** - Compose wrapper for KraftEffectTextureView
  - **KraftShadeAnimatedView** - Compose wrapper for AnimatedKraftTextureView

## 5. Pipeline DSL
- **Introduction to the DSL** - Overview of the Pipeline DSL
- **Serial Pipeline** - Creating linear processing chains
- **Graph Pipeline** - Creating complex multi-pass rendering
- **Nested Pipeline** - Combining serial and graph pipelines
- **PipelineModifier** - Creating reusable pipeline components
- **Best Practices** - Tips for effective pipeline design

## 6. Built-in Shaders
- **Base Shaders** - Core shader implementations
- **Color Effects** - Shaders for color manipulation
- **Alpha & Transparency Effects** - Shaders for transparency
- **Texture & Artistic Effects** - Shaders for artistic effects
- **Edge Detection** - Shaders for edge detection
- **Convolution & Mask Processing** - Shaders for convolution operations
- **Blending Modes** - Shaders for blending operations
- **Blur & Distortion** - Shaders for blur and distortion effects

## 7. Advanced Topics
- **Effect Serialization** - Working with serialized effects
  - **Serialization Overview** - Introduction to effect serialization
  - **EffectSerializer** - Converting pipelines to JSON
  - **SerializedEffect** - Reconstructing effects from JSON
  - **Texture Provider Mapping** - Managing texture providers
  - **Serialization Limitations** - Understanding the limitations
  - **Advanced Serialization** - Advanced serialization techniques

## Current State of Documentation

The documentation is currently in various stages of completion:

- **Complete**: 
  - Architecture Overview
  - KraftShader
  - GlUniformDelegate
  - Overview (Introduction)
  - Installation

- **In Progress**:
  - Many sections have placeholder files with only titles

- **Not Started**:
  - Several sections need content creation

## Implementation Plan

The documentation will be implemented in phases:

### Phase 1: Core Documentation
- Introduction
- Getting Started
- Core Components (basic coverage)
- View Components (basic coverage)

### Phase 2: Expanded Documentation
- Pipeline DSL
- Built-in Shaders (comprehensive coverage)
- Advanced Topics (basic coverage)

### Phase 3: Complete Documentation
- Advanced Topics (comprehensive coverage)
- Additional examples and use cases

## Revision Process

### Document Review and Linking

As part of the ongoing documentation maintenance, we regularly review existing content and add appropriate links between related documents. This process includes:

1. **Review**: Examine all existing documentation for accuracy, completeness, and clarity
2. **Identify Connections**: Identify relationships between different documents and concepts
3. **Add Links**: Add cross-references and links between related documents
4. **Update DOC_PLAN.md**: Document new connections and update the documentation plan

This review and linking process will be performed:
- After completing each documentation phase
- When adding significant new content
- Periodically (quarterly) to ensure documentation cohesion

### Current Linking Status

The following connections have been identified and linked:

- Architecture Overview links to:
  - KraftShader documentation
  - GlEnv documentation
  - Input System documentation
  - Pipeline Running Flow documentation
  - Buffer Management documentation
  - Pipeline DSL Introduction
  - Android Views documentation
  - Jetpack Compose documentation

- KraftShader links to:
  - GlUniformDelegate documentation
  - Texture Inputs documentation
  - Built-in Shaders documentation

- GlUniformDelegate links to:
  - KraftShader documentation

- Overview (Introduction) links to:
  - Installation and Quick Start Guide
  - Why KraftShade documentation

- Installation links to:
  - Quick Start Guide
  - Basic Concepts
  - First Effect documentation

## Style Guidelines

- Use clear, concise language
- Include code examples for all concepts
- Provide diagrams for complex concepts
- Include screenshots and visual examples where appropriate
- Maintain consistent terminology throughout
- Link related concepts for easy navigation
- Include troubleshooting sections for common issues

## Maintenance Plan

- Regular reviews to ensure accuracy
- Updates for new features and changes
- Community feedback incorporation
- Version-specific documentation

## Contributing to Documentation

Guidelines for contributing to the KraftShade documentation:

1. Follow the existing structure outlined in this document
2. Maintain consistent formatting and style
3. Include code examples where appropriate
4. Add links to related documentation
5. Update this plan when adding new sections or making significant changes

## AI Documentation Guidelines

This section provides guidelines for AI agents assisting with KraftShade documentation.

### Core Principles

- **Code-First Approach**: Always check the actual code implementation before documenting any feature or API. Documentation must accurately reflect the current implementation, not theoretical or planned features.

- **Verify API Usage**: Confirm the actual way APIs are used by examining the codebase. Look for:
  - Method signatures and parameters
  - Return types and values
  - Exception handling patterns
  - Usage patterns in test cases and examples

- **No Fabrication**: Do not make up or assume the existence of:
  - DSLs that aren't in the codebase
  - Classes or methods that don't exist
  - Parameters or options not supported by the implementation
  - Features mentioned in comments but not actually implemented

- **Find Real Examples**: Source examples directly from the repository:
  - Prioritize examples from test cases and demo applications
  - Reference actual implementation code when appropriate
  - Ensure examples are complete and functional
  - Verify examples work with the current API version

- **Visual Documentation**: Use diagrams to enhance understanding:
  - Use Mermaid graphs for:
    - Architecture overviews
    - Pipeline flows
    - Process sequences
    - Decision trees for complex operations
  - **Important Note**: Do not use class diagrams. Instead, provide clear and precise textual descriptions of components and their relationships.
  - **Color Consistency**: Use the following color scheme for all diagrams to maintain visual consistency:
    - Core Components: `fill:#c73,stroke:#333,stroke-width:2px,font-size:24px,font-weight:bold,white-space: nowrap`
    - View Components: `fill:#27c,stroke:#333,stroke-width:2px,font-size:24px,font-weight:bold,white-space: nowrap`
    - DSL Layer: `fill:#3c3,stroke:#333,stroke-width:2px,font-size:24px,font-weight:bold,white-space: nowrap`
    - Pipeline System: `fill:#93c,stroke:#933,stroke-width:4px,stroke-dasharray: 5 2`
  - Choose the appropriate diagram type:
    ```mermaid
    flowchart TD
      A[Choose Diagram Type] --> B{What are you documenting?}
      B -->|Process Flow| C[Flowchart]
      B -->|Sequence of Operations| E[Sequence Diagram]
      B -->|State Transitions| F[State Diagram]
    ```
  - For component relationships that would traditionally use class diagrams, provide structured textual descriptions that clearly explain:
    - Component names and purposes
    - Hierarchical relationships (inheritance/implementation)
    - Compositional relationships
    - Dependencies between components
    - Key methods and properties (when relevant)

### Documentation Workflow

1. **Research Phase**:
   - Examine relevant code files
   - Review tests and examples
   - Identify key classes, methods, and patterns
   - Note any discrepancies between code and existing docs

2. **Verification Phase**:
   - Confirm behavior through test cases
   - Verify parameter types and constraints
   - Check for edge cases and error handling
   - Identify dependencies and requirements

3. **Documentation Phase**:
   - Structure content logically
   - Include verified code examples
   - Add appropriate diagrams
   - Link to related documentation
   - Include troubleshooting guidance

4. **Review Phase**:
   - Check accuracy against code
   - Ensure completeness
   - Verify diagram correctness
   - Confirm terminology consistency

### Best Practices

- Document both "how" and "why" - explain implementation details and the reasoning behind them
- Include common pitfalls and their solutions
- Provide progressive examples from basic to advanced usage
- Use consistent terminology that matches the codebase
- Highlight performance considerations and optimization opportunities
- Include version information when documenting evolving APIs