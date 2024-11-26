package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlColor
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

/**
 * A shader that maps specific source colors to target colors. At most 8 set of mappings can be
 * added. If you wish to map more than 8 colors, you should use this shader twice or add another
 * instance of this shader to the pipeline.
 *
 * @param tolerance The tolerance for color matching (default is 0.1)
 * @property colorMappings Array to store color mapping data (max 8 pairs of source/target colors)
 * @property numMappings Number of active color mappings
 * @property tolerance Tolerance value for color matching
 */
class ColorMappingKraftShader(
    tolerance: Float = 0f
) : TextureInputKraftShader() {

    private var colorMappings: FloatArray by GlUniformDelegate("colorMappings")
    private var numMappings: Int by GlUniformDelegate("numMappings")
    var tolerance: Float by GlUniformDelegate("tolerance")

    init {
        this.tolerance = tolerance
        this.colorMappings = floatArrayOf()
        this.numMappings = 0
    }

    /**
     * Add a color mapping pair.
     * @param sourceColor The color to match in the input image
     * @param targetColor The color to replace it with
     * @return true if mapping was added, false if maximum mappings reached
     */
    fun addColorMapping(sourceColor: GlColor, targetColor: GlColor) {
        check(numMappings < 8) { "maximum number of color mappings reached" }

        val sourceVec4 = sourceColor.vec4
        val targetVec4 = targetColor.vec4

        val new = FloatArray(8)
        // Source color components
        new[0] = sourceVec4[0]
        new[1] = sourceVec4[1]
        new[2] = sourceVec4[2]
        new[3] = sourceVec4[3]

        // Target color components
        new[4] = targetVec4[0]
        new[5] = targetVec4[1]
        new[6] = targetVec4[2]
        new[7] = targetVec4[3]

        colorMappings += new
        numMappings++
    }

    /**
     * Clear all color mappings
     */
    fun clearMappings() {
        numMappings = 0
        colorMappings = floatArrayOf()
    }

    override fun loadFragmentShader(): String = COLOR_MAPPING_FRAGMENT_SHADER
}

@Language("GLSL")
private const val COLOR_MAPPING_FRAGMENT_SHADER = """
    precision mediump float;
    varying vec2 textureCoordinate;

    uniform sampler2D inputImageTexture;
    uniform float colorMappings[64]; // Max 8 mappings (8 * 8 floats)
    uniform int numMappings;         // Number of mappings (pairs of source/target)
    uniform float tolerance;         // Tolerance for matching

    void main() {
        vec4 inputColor = texture2D(inputImageTexture, textureCoordinate);
        vec4 outputColor = inputColor;

        for (int i = 0; i < 8; i++) { // Max 8 mappings
            if (i >= numMappings) break;

            // Read source and target colors
            vec4 sourceColor = vec4(
                colorMappings[i * 8 + 0],
                colorMappings[i * 8 + 1],
                colorMappings[i * 8 + 2],
                colorMappings[i * 8 + 3]
            );
            vec4 targetColor = vec4(
                colorMappings[i * 8 + 4],
                colorMappings[i * 8 + 5],
                colorMappings[i * 8 + 6],
                colorMappings[i * 8 + 7]
            );

            // Check if the input color matches the source color within the tolerance
            if (length(inputColor - sourceColor) <= tolerance) {
                outputColor = targetColor;
                break; // Match found, exit loop
            }
        }

        gl_FragColor = outputColor;
    }
"""
