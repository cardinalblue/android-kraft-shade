package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import org.intellij.lang.annotations.Language

/**
 * A shader that applies color dodge blend mode to two input textures.
 *
 * ## Effect Description
 * Color dodge blend mode creates a brightening effect by decreasing contrast and
 * lightening colors. It divides the base color by the inverted overlay color,
 * which "dodges" or avoids the darkness of the overlay. This produces intense
 * brightening effects, especially with lighter overlay colors.
 *
 * ## Main Purpose
 * - **Dramatic brightening**: Create intense highlight effects and light sources
 * - **Glow effects**: Simulate light emission and luminous objects
 * - **High-key imagery**: Create bright, airy, overexposed looks
 * - **Light ray effects**: Enhance sunbeams, lens flares, and light streaks
 *
 * ## Usage Examples
 * - Creating glowing light effects and halos around objects
 * - Enhancing bright skies and clouds for ethereal looks
 * - Simulating light leaks and vintage film exposure effects
 * - Adding bright highlights to metallic surfaces
 * - Creating dreamy, soft-focus portrait effects
 * - Enhancing fire, lightning, and other light sources
 *
 * ## Technical Details
 * Uses complex alpha-aware blending with conditional color mixing:
 * - Black overlay (0.0) produces no change to the base
 * - White overlay (1.0) produces pure white output
 * - Mid-tones create varying degrees of brightening
 * - Includes proper alpha channel handling and clamping to prevent overflow
 * - Uses `step()` and `mix()` functions for smooth color transitions
 *
 * @see TwoTextureInputKraftShader
 * @see ColorBurnBlendKraftShader for the opposite darkening effect
 */
class ColorDodgeBlendKraftShader: TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = COLOR_DODGE_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val COLOR_DODGE_BLEND_FRAGMENT_SHADER = """
    precision mediump float;
    
    varying highp vec2 textureCoordinate;
    varying highp vec2 textureCoordinate2;
    
    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;
    
    void main() {
        vec4 base = texture2D(inputImageTexture, textureCoordinate);
        vec4 overlay = texture2D(inputImageTexture2, textureCoordinate2);
        
        vec3 baseOverlayAlphaProduct = vec3(overlay.a * base.a);
        vec3 rightHandProduct = overlay.rgb * (1.0 - base.a) + base.rgb * (1.0 - overlay.a);
        
        vec3 firstBlendColor = baseOverlayAlphaProduct + rightHandProduct;
        vec3 overlayRGB = clamp((overlay.rgb / clamp(overlay.a, 0.01, 1.0)) * step(0.0, overlay.a), 0.0, 0.99);
        
        vec3 secondBlendColor = (base.rgb * overlay.a) / (1.0 - overlayRGB) + rightHandProduct;
        
        vec3 colorChoice = step((overlay.rgb * base.a + base.rgb * overlay.a), baseOverlayAlphaProduct);
        
        gl_FragColor = vec4(mix(firstBlendColor, secondBlendColor, colorChoice), 1.0);
    }
"""