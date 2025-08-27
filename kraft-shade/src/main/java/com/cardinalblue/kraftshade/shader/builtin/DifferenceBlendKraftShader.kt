package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import org.intellij.lang.annotations.Language

/**
 * A shader that applies difference blend mode to two input textures.
 *
 * ## Effect Description
 * Difference blend mode calculates the absolute difference between the base and overlay
 * colors for each channel. This creates a high-contrast effect that highlights the
 * differences between the two images. Identical pixels result in black, while completely
 * different pixels produce bright, often inverted colors.
 *
 * ## Main Purpose
 * - **Change detection**: Highlight differences between two similar images
 * - **Artistic effects**: Create psychedelic, high-contrast visual effects
 * - **Image comparison**: Visually identify variations between image versions
 * - **Creative blending**: Generate unique color combinations and patterns
 *
 * ## Usage Examples
 * - **Motion detection**: Compare frames to detect movement or changes
 * - **Before/after comparisons**: Visualize differences in photo editing
 * - **Artistic photography**: Create surreal, high-contrast effects
 * - **Quality control**: Detect variations in manufacturing or printing
 * - **Scientific imaging**: Highlight changes in medical or research imagery
 * - **Creative design**: Generate unique textures and color patterns
 *
 * ## Technical Details
 * Uses the formula: `abs(overlay.rgb - base.rgb)`
 * - Identical colors (base == overlay) produce black (0.0)
 * - Maximum difference (black vs white) produces white (1.0)
 * - Mid-range differences create varying intensities
 * - Color relationships are inverted and often unexpected
 * - Alpha channel is preserved from the base image
 *
 * ## Visual Characteristics
 * - **High contrast**: Creates stark differences and bold effects
 * - **Color inversion**: Similar to negative film effects
 * - **Symmetrical**: Order of inputs affects color but not intensity
 * - **Analytical**: Excellent for detecting subtle changes
 *
 * ## Comparison with Other Blend Modes
 * - Unlike **Exclusion**, produces more extreme contrast
 * - More analytical than **Overlay** or **Soft Light**
 * - Complementary to **Addition** blend mode
 * - Creates opposite effect to **Multiply** mode
 *
 * @see TwoTextureInputKraftShader
 * @see ExclusionBlendKraftShader for a softer difference effect
 */
class DifferenceBlendKraftShader: TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = DIFFERENCE_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val DIFFERENCE_BLEND_FRAGMENT_SHADER = """
    varying highp vec2 textureCoordinate;
    varying highp vec2 textureCoordinate2;

    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;
    
    void main() {
        mediump vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
        mediump vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);
        gl_FragColor = vec4(abs(textureColor2.rgb - textureColor.rgb), textureColor.a);
    }
"""