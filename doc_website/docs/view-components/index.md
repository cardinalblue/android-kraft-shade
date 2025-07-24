---
sidebar_position: 1
---

# View Components

KraftShade provides a set of view components for integrating OpenGL shader effects into your Android applications, with support for both traditional Android Views and Jetpack Compose.

## Choosing the Right View

When working with KraftShade, it's important to choose the right view component for your needs. Here's a guide to help you decide:

### For Android Views

1. **AnimatedKraftTextureView** - Use this when you need time-based animations that update with the display refresh rate (using Android's Choreographer). This is ideal for effects that change over time, like transitions or procedural animations.

2. **KraftEffectTextureView** - Use this when you need to apply shader effects to images or other content, but don't need time-based animations. This is suitable for static effects or effects that only change in response to user input.

3. **KraftTextureView** - This is the base class that the other views extend. **In most cases, you usually don't need to use this directly**. The specialized views above provide higher-level functionality that is more suitable for most use cases. Only use this if you need very custom OpenGL rendering that doesn't fit the patterns of the specialized views.

### For Jetpack Compose

1. **KraftShadeAnimatedView** - Use this when you need time-based animations in a Compose UI. This is the Compose equivalent of AnimatedKraftTextureView.

2. **KraftShadeEffectView** - Use this when you need to apply shader effects in a Compose UI without time-based animations. This is the Compose equivalent of KraftEffectTextureView.

3. **KraftShadeView** - This is the base Compose wrapper for KraftTextureView. **In most cases, you usually don't need to use this directly**. The specialized Compose views above provide higher-level functionality that is more suitable for most use cases. Only use this for very custom OpenGL rendering in Compose.

## Decision Flow

Here's a simple decision flow to help you choose the right view:

1. Are you using Jetpack Compose?
   - Yes → Go to step 2
   - No → Go to step 3

2. For Compose UIs:
   - Do you need time-based animations that update with the display refresh rate?
     - Yes → Use **KraftShadeAnimatedView**
     - No → Use **KraftShadeEffectView**

3. For traditional Android Views:
   - Do you need time-based animations that update with the display refresh rate?
     - Yes → Use **AnimatedKraftTextureView**
     - No → Use **KraftEffectTextureView**

## Android Views vs. Compose Integration

The Jetpack Compose components are wrappers around the Android View implementations, providing a more idiomatic Compose API. Here's how they correspond:

| Android View | Compose Wrapper |
|--------------|-----------------|
| KraftTextureView | KraftShadeView |
| KraftEffectTextureView | KraftShadeEffectView |
| AnimatedKraftTextureView | KraftShadeAnimatedView |

## Understanding Animation in KraftShade

KraftShade uses Android's [Choreographer](https://developer.android.com/reference/android/view/Choreographer) for frame-synchronized animations. This ensures that animations run smoothly at the device's display refresh rate (typically 60fps).

The animated views (AnimatedKraftTextureView and KraftShadeAnimatedView) provide:

- Built-in TimeInput for creating time-based animations
- Play/pause controls for animation
- Automatic frame skipping when rendering can't keep up
- Proper lifecycle management to conserve resources

## Next Steps

Explore the documentation for each view component to learn more about their specific features and usage:

### Android Views
- [KraftTextureView](android-views/kraft-texture-view.md)
- [KraftEffectTextureView](android-views/kraft-effect-texture-view.md)
- [AnimatedKraftTextureView](android-views/animated-kraft-texture-view.md)

### Jetpack Compose
- [KraftShadeView](jetpack-compose/kraft-shade-view.md)
- [KraftShadeEffectView](jetpack-compose/kraft-shade-effect-view.md)
- [KraftShadeAnimatedView](jetpack-compose/kraft-shade-animated-view.md)