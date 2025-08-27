package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import org.intellij.lang.annotations.Language
/**
 * A shader that applies color blend mode to two input textures.
 *
 * ## Effect Description
 * Color blend mode combines the hue and saturation of the overlay texture with the 
 * luminance (brightness) of the base texture. This creates a colorization effect 
 * that preserves the lighting and shadows of the base image while applying the 
 * color characteristics of the overlay.
 *
 * ## Main Purpose
 * - **Colorization**: Transform grayscale images into colored ones
 * - **Color grading**: Apply color tones while maintaining original lighting
 * - **Artistic effects**: Create stylized looks by blending color palettes
 * - **Photo enhancement**: Add warmth or coolness to images
 *
 * ## Usage Examples
 * - Converting black & white photos to sepia or other color tones
 * - Applying color filters to maintain detail and contrast
 * - Creating duotone effects with two contrasting colors
 * - Color matching between different image sources
 *
 * ## Technical Details
 * Uses luminance-based color blending that preserves the brightness information
 * from the base image while applying the color information from the overlay.
 * The algorithm uses the `setlum()` function to maintain proper luminance levels
 * while preventing color clipping.
 *
 * @see TwoTextureInputKraftShader
 */
class ColorBlendKraftShader: TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = COLOR_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val COLOR_BLEND_FRAGMENT_SHADER = """
    varying highp vec2 textureCoordinate;
    varying highp vec2 textureCoordinate2;
    
    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;
    
    highp float lum(lowp vec3 c) {
        return dot(c, vec3(0.3, 0.59, 0.11));
    }
    
    lowp vec3 clipcolor(lowp vec3 c) {
        highp float l = lum(c);
        lowp float n = min(min(c.r, c.g), c.b);
        lowp float x = max(max(c.r, c.g), c.b);
        
        if (n < 0.0) {
            c.r = l + ((c.r - l) * l) / (l - n);
            c.g = l + ((c.g - l) * l) / (l - n);
            c.b = l + ((c.b - l) * l) / (l - n);
        }
        if (x > 1.0) {
            c.r = l + ((c.r - l) * (1.0 - l)) / (x - l);
            c.g = l + ((c.g - l) * (1.0 - l)) / (x - l);
            c.b = l + ((c.b - l) * (1.0 - l)) / (x - l);
        }
        
        return c;
    }

    lowp vec3 setlum(lowp vec3 c, highp float l) {
        highp float d = l - lum(c);
        c = c + vec3(d);
        return clipcolor(c);
    }
    
    void main() {
        highp vec4 baseColor = texture2D(inputImageTexture, textureCoordinate);
        highp vec4 overlayColor = texture2D(inputImageTexture2, textureCoordinate2);

        gl_FragColor = vec4(baseColor.rgb * (1.0 - overlayColor.a) + setlum(overlayColor.rgb, lum(baseColor.rgb)) * overlayColor.a, baseColor.a);
    }
"""


