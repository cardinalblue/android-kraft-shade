package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import org.intellij.lang.annotations.Language

/**
 * A shader that applies color burn blend mode to two input textures.
 *
 * ## Effect Description
 * Color burn blend mode creates a darkening effect by increasing contrast and
 * saturating colors. It divides the inverted base color by the overlay color,
 * then inverts the result. Areas where the overlay is darker become more pronounced,
 * while lighter areas in the overlay have less effect.
 *
 * ## Main Purpose
 * - **Dramatic darkening**: Create intense shadow effects and deep contrasts
 * - **Color saturation**: Enhance color intensity and richness
 * - **Vintage effects**: Simulate old film processing techniques
 * - **Artistic stylization**: Create moody, high-contrast imagery
 *
 * ## Usage Examples
 * - Adding dramatic shadows and depth to portraits
 * - Creating vintage film look with enhanced contrast
 * - Enhancing sunset/sunrise colors for more vibrant skies
 * - Darkening backgrounds to make subjects stand out
 * - Creating silhouette effects with strong backlighting
 *
 * ## Technical Details
 * Uses the formula: `1.0 - (1.0 - base) / overlay`
 * - White overlay (1.0) produces no change to the base
 * - Black overlay (0.0) produces pure black output
 * - Mid-tones create varying degrees of darkening and saturation
 * - Can produce very high contrast results with potential color clipping
 *
 * @see TwoTextureInputKraftShader
 * @see ColorDodgeBlendKraftShader for the opposite brightening effect
 */
class ColorBurnBlendKraftShader: TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = COLOR_BURN_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val COLOR_BURN_BLEND_FRAGMENT_SHADER = """
    varying highp vec2 textureCoordinate;
    varying highp vec2 textureCoordinate2;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;
    
    void main() {
        mediump vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
        mediump vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);
        mediump vec4 whiteColor = vec4(1.0);
        gl_FragColor = whiteColor - (whiteColor - textureColor) / textureColor2;
    }
"""