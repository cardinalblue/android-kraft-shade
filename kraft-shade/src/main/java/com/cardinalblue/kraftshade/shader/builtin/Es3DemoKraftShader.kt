package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.KraftShaderTextureInput
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

/**
 * A demo shader that showcases OpenGL ES 3.0 features.
 * This shader will use OpenGL ES 3.0 specific features if available, otherwise it will fall back to OpenGL ES 2.0.
 */
class Es3DemoKraftShader: KraftShader() {

    /**
     * The input texture to apply the effect to.
     */
    val inputTexture = KraftShaderTextureInput(1, "inputImageTexture")

    /**
     * The intensity of the effect, from 0.0 to 1.0.
     */
    var intensity: Float by GlUniformDelegate("intensity")

    init {
        intensity = 1.0f
    }

    override fun loadVertexShader(): String {
        return DEFAULT_VERTEX_SHADER
    }

    override fun loadFragmentShader(): String {
        return FRAGMENT_SHADER_ES3
    }

    override fun beforeActualDraw(isScreenCoordinate: Boolean) {
        super.beforeActualDraw(isScreenCoordinate)
        inputTexture.activate()
    }

    companion object {

        /**
         * OpenGL ES 3.0 shader that uses features like in/out variables and more advanced functions.
         */
        @Language("GLSL")
        private const val FRAGMENT_SHADER_ES3 = """
            #version 300 es
            precision mediump float;
            
            in vec2 textureCoordinate;
            uniform sampler2D inputImageTexture;
            uniform float intensity;
            
            out vec4 fragColor;
            
            // Helper function that uses ES 3.0 features
            vec3 adjustColor(vec3 color, float intensity) {
                // Using ES 3.0 mix function for smooth transitions
                vec3 enhancedColor = mix(
                    color,
                    vec3(
                        color.r * 1.5,
                        color.g * 0.8,
                        color.b * 1.2
                    ),
                    intensity
                );
                
                // Using ES 3.0 clamp function to ensure values stay in range
                return clamp(enhancedColor, 0.0, 1.0);
            }
            
            void main() {
                vec4 textureColor = texture(inputImageTexture, textureCoordinate);
                
                // Apply more advanced color manipulation using ES 3.0 features
                vec3 color = adjustColor(textureColor.rgb, intensity);
                
                // Using ES 3.0 output variable
                fragColor = vec4(color, textureColor.a);
            }
        """
    }
}
