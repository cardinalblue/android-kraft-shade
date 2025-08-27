package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import org.intellij.lang.annotations.Language

/**
 * A shader that applies darken blend mode to two input textures.
 *
 * ## Effect Description
 * Darken blend mode compares the base and overlay colors channel by channel and
 * selects the darker value for each channel. This creates a darkening effect where
 * only pixels that are darker in the overlay will affect the final result.
 * Light areas in the overlay become transparent, while dark areas darken the base.
 *
 * ## Main Purpose
 * - **Selective darkening**: Darken only specific areas without affecting lighter regions
 * - **Shadow enhancement**: Add natural-looking shadows and depth
 * - **Texture overlay**: Blend dark textures like scratches, dust, or grain
 * - **Composite darkening**: Combine multiple dark elements without over-darkening
 *
 * ## Usage Examples
 * - Adding realistic shadows to objects or scenes
 * - Overlaying dark textures like film grain or paper texture
 * - Creating depth by darkening background elements
 * - Applying dark vignettes that preserve bright highlights
 * - Combining multiple exposure brackets in HDR processing
 * - Adding weathering effects like dirt, rust, or age spots
 *
 * ## Technical Details
 * Uses the formula: `min(overlay.rgb * base.a, base.rgb * overlay.a)`
 * with proper alpha channel blending for transparent areas.
 * - White overlay (1.0) produces no change to the base
 * - Black overlay (0.0) produces pure black output
 * - Gray values darken proportionally to their darkness
 * - Preserves transparency and handles alpha blending correctly
 *
 * ## Comparison with Other Blend Modes
 * - Unlike **Color Burn**, this mode preserves more detail in highlights
 * - Unlike **Multiply**, this mode doesn't compound darkness as dramatically
 * - More predictable and linear than complex burn/dodge modes
 *
 * @see TwoTextureInputKraftShader
 * @see LightenBlendKraftShader for the opposite brightening effect
 * @see ColorBurnBlendKraftShader for more dramatic darkening
 */
class DarkenBlendKraftShader: TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = DARKEN_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val DARKEN_BLEND_FRAGMENT_SHADER = """
    varying highp vec2 textureCoordinate;
    varying highp vec2 textureCoordinate2;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;
    
    void main() {
        lowp vec4 base = texture2D(inputImageTexture, textureCoordinate);
        lowp vec4 overlayer = texture2D(inputImageTexture2, textureCoordinate2);
        
        gl_FragColor = vec4(min(overlayer.rgb * base.a, base.rgb * overlayer.a) + overlayer.rgb * (1.0 - base.a) + base.rgb * (1.0 - overlayer.a), 1.0);
    }
"""