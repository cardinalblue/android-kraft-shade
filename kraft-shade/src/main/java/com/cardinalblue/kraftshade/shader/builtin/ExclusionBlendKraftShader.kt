package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import org.intellij.lang.annotations.Language

/**
 * A shader that applies exclusion blend mode to two input textures with alpha-aware blending.
 *
 * ## Effect Description
 * Exclusion blend mode creates a softer version of the Difference blend mode. It uses the
 * mathematical formula `(overlay * base.a + base * overlay.a - 2 * overlay * base)` plus
 * alpha compositing terms. This produces similar high-contrast effects to Difference but
 * with lower contrast and smoother transitions, making it more suitable for creative
 * blending where extreme contrast isn't desired.
 *
 * ## Main Purpose
 * - **Soft difference effects**: Create contrast without the harshness of pure difference
 * - **Creative blending**: Generate artistic color combinations with smooth transitions
 * - **Texture overlay**: Blend textures with controlled contrast enhancement
 * - **Color inversion**: Create inverted effects with softer characteristics
 *
 * ## Usage Examples
 * - **Artistic photography**: Create dreamy, high-contrast effects with smooth gradients
 * - **Texture blending**: Combine textures without harsh edges or extreme contrasts
 * - **Creative compositing**: Layer images for surreal but pleasant visual effects
 * - **Color grading**: Apply creative color treatments with controlled intensity
 * - **Digital art**: Generate unique color palettes and atmospheric effects
 * - **Background effects**: Create subtle but interesting background textures
 *
 * ## Technical Details
 * Uses the Porter-Duff exclusion formula with alpha compositing:
 * ```
 * result = (overlay.rgb * base.a + base.rgb * overlay.a - 2.0 * overlay.rgb * base.rgb) +
 *          overlay.rgb * (1.0 - base.a) + base.rgb * (1.0 - overlay.a)
 * ```
 * - **First term**: Core exclusion calculation with alpha weighting
 * - **Second term**: Overlay contribution in areas where base is transparent
 * - **Third term**: Base contribution in areas where overlay is transparent
 * - **Alpha preservation**: Maintains base image's alpha channel
 *
 * ## Mathematical Behavior
 * - **Identical colors**: Produce darker results (opposite of Addition)
 * - **Contrasting colors**: Create bright, high-contrast outputs
 * - **Mid-tone interactions**: Generate smooth color transitions
 * - **Alpha handling**: Proper compositing for semi-transparent pixels
 * - **Symmetrical**: Order of inputs affects final colors but maintains balance
 *
 * ## Visual Characteristics
 * - **Softer contrast**: Less extreme than Difference blend mode
 * - **Smooth gradients**: Creates pleasant transitions between colors
 * - **Color inversion**: Similar to negative effects but more controlled
 * - **Creative colors**: Generates unexpected but harmonious color combinations
 * - **Alpha-aware**: Proper transparency handling for layered effects
 *
 * ## Comparison with Other Blend Modes
 * - **Softer than Difference**: Less harsh contrast, smoother transitions
 * - **More controlled than Addition**: Better color balance and clipping prevention
 * - **Related to Screen**: Similar mathematical approach but different formula
 * - **Complementary to Multiply**: Creates opposite brightening effects
 *
 * ## Creative Applications
 * - **Double exposure**: Create ethereal double-exposure photography effects
 * - **Color pop**: Enhance specific colors while maintaining overall harmony
 * - **Atmospheric effects**: Generate fog, mist, or dream-like qualities
 * - **Pattern generation**: Create interesting repeating patterns and textures
 *
 * @see TwoTextureInputKraftShader
 * @see DifferenceBlendKraftShader for a harder contrast alternative
 * @see ScreenBlendKraftShader for another mathematical brightening approach
 */
class ExclusionBlendKraftShader: TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = EXCLUSION_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val EXCLUSION_BLEND_FRAGMENT_SHADER = """
    varying highp vec2 textureCoordinate;
    varying highp vec2 textureCoordinate2;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;
    
    void main() {
        mediump vec4 base = texture2D(inputImageTexture, textureCoordinate);
        mediump vec4 overlay = texture2D(inputImageTexture2, textureCoordinate2);
        
        // Dca = (Sca.Da + Dca.Sa - 2.Sca.Dca) + Sca.(1 - Da) + Dca.(1 - Sa)
        gl_FragColor = vec4((overlay.rgb * base.a + base.rgb * overlay.a - 2.0 * overlay.rgb * base.rgb) + overlay.rgb * (1.0 - base.a) + base.rgb * (1.0 - overlay.a), base.a);
    }
"""