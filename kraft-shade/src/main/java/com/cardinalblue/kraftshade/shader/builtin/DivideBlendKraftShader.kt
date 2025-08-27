package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import org.intellij.lang.annotations.Language

/**
 * A shader that applies divide blend mode to two input textures with per-channel division logic.
 *
 * ## Effect Description
 * Divide blend mode divides the base color by the overlay color for each RGB channel,
 * creating a brightening effect similar to Color Dodge but with different characteristics.
 * The algorithm includes complex alpha-aware calculations and conditional logic to prevent
 * division by zero and handle edge cases gracefully. This creates intense brightening
 * effects with unique color characteristics.
 *
 * ## Main Purpose
 * - **Mathematical brightening**: Create precise brightening based on division operations
 * - **Color correction**: Remove color casts by dividing by the inverse color
 * - **Scientific imaging**: Perform mathematical operations on image data
 * - **Artistic effects**: Generate unique color relationships through division
 *
 * ## Usage Examples
 * - **Removing color casts**: Divide by a color sample to neutralize tints
 * - **Brightening shadows**: Selectively brighten dark areas using division
 * - **Scientific analysis**: Perform ratio calculations between image channels
 * - **Artistic photography**: Create unique color effects through mathematical blending
 * - **HDR processing**: Combine exposures using division-based algorithms
 * - **Color grading**: Apply complex color corrections and adjustments
 *
 * ## Technical Details
 * Uses per-channel conditional division with alpha-aware blending:
 * - For each RGB channel, compares `base/overlay` with `base.a/overlay.a`
 * - If overlay alpha is 0 or ratio exceeds threshold: uses alpha blending formula
 * - Otherwise: performs division operation `(base * overlay.a²) / overlay`
 * - Includes proper alpha composition: `overlay.a + base.a - overlay.a * base.a`
 * - Prevents division by zero and handles transparency correctly
 *
 * ## Mathematical Behavior
 * - **White overlay (1.0)**: No change to base color
 * - **Black overlay (0.0)**: Creates maximum brightening effect
 * - **Mid-tones**: Create varying degrees of brightening
 * - **Alpha handling**: Complex per-channel alpha compositing
 * - **Edge cases**: Safe handling of zero values and extreme ratios
 *
 * ## Visual Characteristics
 * - **Intense brightening**: More aggressive than simple multiplication
 * - **Color shifts**: Can create unexpected but interesting color changes
 * - **Alpha preservation**: Maintains proper transparency composition
 * - **Channel independence**: Each RGB channel processed separately
 *
 * ## Comparison with Other Blend Modes
 * - **More complex than Color Dodge**: Uses different mathematical approach
 * - **Similar to Screen**: But with division instead of inverse multiplication
 * - **Mathematical precision**: More predictable than some artistic blend modes
 * - **Alpha-aware**: Better transparency handling than simpler division
 *
 * @see TwoTextureInputKraftShader
 * @see ColorDodgeBlendKraftShader for another brightening approach
 * @see ScreenBlendKraftShader for inverse multiplication brightening
 */
class DivideBlendKraftShader: TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = DIVIDE_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val DIVIDE_BLEND_FRAGMENT_SHADER = """
    varying highp vec2 textureCoordinate;
    varying highp vec2 textureCoordinate2;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;
    
    void main() {
        mediump vec4 base = texture2D(inputImageTexture, textureCoordinate);
        mediump vec4 overlay = texture2D(inputImageTexture2, textureCoordinate2);
        
        mediump float ra;
        if (overlay.a == 0.0 || ((base.r / overlay.r) > (base.a / overlay.a)))
            ra = overlay.a * base.a + overlay.r * (1.0 - base.a) + base.r * (1.0 - overlay.a);
        else
            ra = (base.r * overlay.a * overlay.a) / overlay.r + overlay.r * (1.0 - base.a) + base.r * (1.0 - overlay.a);
        
        mediump float ga;
        if (overlay.a == 0.0 || ((base.g / overlay.g) > (base.a / overlay.a)))
            ga = overlay.a * base.a + overlay.g * (1.0 - base.a) + base.g * (1.0 - overlay.a);
        else
            ga = (base.g * overlay.a * overlay.a) / overlay.g + overlay.g * (1.0 - base.a) + base.g * (1.0 - overlay.a);
        
        mediump float ba;
        if (overlay.a == 0.0 || ((base.b / overlay.b) > (base.a / overlay.a)))
            ba = overlay.a * base.a + overlay.b * (1.0 - base.a) + base.b * (1.0 - overlay.a);
        else
            ba = (base.b * overlay.a * overlay.a) / overlay.b + overlay.b * (1.0 - base.a) + base.b * (1.0 - overlay.a);

        mediump float a = overlay.a + base.a - overlay.a * base.a;
        
        gl_FragColor = vec4(ra, ga, ba, a);
    }
"""