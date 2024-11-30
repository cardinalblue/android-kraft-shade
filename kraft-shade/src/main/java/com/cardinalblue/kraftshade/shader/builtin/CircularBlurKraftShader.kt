package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

/**
 * A blur shader that creates a blur effect by sampling multiple points in a circular pattern. The
 * blur intensity and number of samples can be adjusted through the blurAmount and repeat parameters.
 *
 * The shader uses randomness in its sampling pattern for two important reasons:
 * 1. To reduce banding artifacts that would appear if samples were taken at fixed intervals
 * 2. To create a more natural-looking blur by introducing controlled variation in the sampling pattern
 *
 * Each iteration takes two samples with different random offsets to further smooth out the result
 * and hide any noise patterns that might emerge from the random number generation.
 *
 * shader taken from [link](https://www.shadertoy.com/view/4lXXWn)
 */
class CircularBlurKraftShader : TextureInputKraftShader() {
    private var internalAmount: Float by GlUniformDelegate("uBlurAmount")

    /**
     * The actual for the shader is quite small, so we scale it up by 10x to make it more
     * user-friendly.
     */
    var amount: Float
        set(value) { internalAmount = value * 0.1f }
        get() { return internalAmount * 10f }

    var repeat: Float by GlUniformDelegate("uRepeat")

    init {
        amount = 0.3f
        repeat = 30.0f
    }

    override fun loadFragmentShader(): String = CIRCULAR_BLUR_FRAGMENT_SHADER
}

@Language("GLSL")
private const val CIRCULAR_BLUR_FRAGMENT_SHADER = """
    precision highp float;

    varying vec2 textureCoordinate;
    uniform sampler2D inputImageTexture;

    uniform float uBlurAmount;
    uniform float uRepeat;

    float rand(vec2 co) {
        return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
    }

    void main() {
        vec2 uv = textureCoordinate;
        float alpha = texture2D(inputImageTexture, uv).a;
        vec4 blurred_image = vec4(0.);

        for (float i = 0.; i < uRepeat; i++) { 
            vec2 q = vec2(cos(degrees((i/uRepeat)*360.)), sin(degrees((i/uRepeat)*360.))) * (rand(vec2(i,uv.x+uv.y))+uBlurAmount); 
            vec2 uv2 = uv+(q*uBlurAmount);
            blurred_image += texture2D(inputImageTexture, uv2).rgba/2.;

            // One more sample to hide the noise
            q = vec2(cos(degrees((i/uRepeat)*360.)),sin(degrees((i/uRepeat)*360.))) * (rand(vec2(i+2.,uv.x+uv.y+24.))+uBlurAmount); 
            uv2 = uv+(q*uBlurAmount);
            blurred_image += texture2D(inputImageTexture, uv2).rgba/2.;
        }
        blurred_image /= uRepeat;

        gl_FragColor = blurred_image;
    }
"""
