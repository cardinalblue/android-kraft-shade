package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language

/**
 * A shader that applies dissolve (mix) blend mode to two input textures with controllable mixing ratio.
 *
 * ## Effect Description
 * Dissolve blend mode performs a linear interpolation between the base and overlay textures
 * based on a mixture percentage. This creates a smooth transition effect where the two
 * images are blended proportionally. At 0% mixture, only the base image is visible; at
 * 100% mixture, only the overlay is visible; at 50%, both images contribute equally.
 *
 * ## Main Purpose
 * - **Smooth transitions**: Create seamless blends between two images
 * - **Crossfade effects**: Animate transitions in slideshows or video
 * - **Double exposure**: Blend two images with custom opacity control
 * - **Composite imaging**: Layer images with precise control over visibility
 *
 * ## Usage Examples
 * - **Photo transitions**: Smooth crossfading between different images
 * - **Time-lapse blending**: Combine multiple exposures with varying weights
 * - **Artistic composites**: Create dreamy, ethereal double-exposure effects
 * - **UI animations**: Smooth transitions between different visual states
 * - **Scientific imaging**: Blend multiple data visualizations or measurements
 * - **Before/after comparisons**: Gradually reveal changes with slider control
 *
 * ## Technical Details
 * Uses the GLSL `mix()` function: `mix(base, overlay, mixturePercent)`
 * - **mixturePercent = 0.0**: Shows only the base texture
 * - **mixturePercent = 1.0**: Shows only the overlay texture
 * - **mixturePercent = 0.5**: Equal blend of both textures
 * - Values are linearly interpolated between the two extremes
 * - Preserves color accuracy and maintains proper alpha blending
 *
 * ## Shader Parameters
 * - **mixturePercent**: Float value (0.0 - 1.0) controlling blend ratio
 *   - Can be animated for dynamic transition effects
 *   - Provides precise control over blend intensity
 *
 * ## Visual Characteristics
 * - **Smooth blending**: No harsh edges or sudden transitions
 * - **Linear interpolation**: Predictable and mathematically precise
 * - **Alpha preservation**: Maintains transparency information
 * - **Color accuracy**: No color shifts or artifacts
 *
 * ## Comparison with Other Blend Modes
 * - **Simpler than Difference**: No color inversion or extreme contrast
 * - **More controlled than Add/Multiply**: Linear rather than mathematical operations
 * - **Foundation for other modes**: Many complex blends build upon this basic mixing
 * - **Animation-friendly**: Smooth parameter changes for transitions
 *
 * @param mixturePercent Initial mixing ratio (default: 0.5f for equal blend)
 * @see MixBlendKraftShader
 * @see TwoTextureInputKraftShader
 */
class DissolveBlendKraftShader(
    mixturePercent: Float = 0.5f
) : MixBlendKraftShader(mixturePercent) {
    
    override fun loadFragmentShader(): String = DISSOLVE_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val DISSOLVE_BLEND_FRAGMENT_SHADER = """
    varying highp vec2 textureCoordinate;
    varying highp vec2 textureCoordinate2;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;
    uniform lowp float mixturePercent;
    
    void main() {
        lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
        lowp vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);
        
        gl_FragColor = mix(textureColor, textureColor2, mixturePercent);
    }
"""