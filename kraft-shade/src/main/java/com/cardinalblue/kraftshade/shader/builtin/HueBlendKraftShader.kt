package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import org.intellij.lang.annotations.Language

/**
 * A shader that applies hue blend mode to two input textures using HSL color space operations.
 *
 * ## Effect Description
 * Hue blend mode takes the hue (color) from the overlay texture while preserving the
 * saturation and luminance (brightness) of the base texture. This creates a color
 * replacement effect where the original image's lighting, shadows, and intensity are
 * maintained, but the colors are replaced with those from the overlay. The result
 * combines the hue characteristics of the overlay with the tonal qualities of the base.
 *
 * ## Main Purpose
 * - **Color replacement**: Change colors while maintaining original lighting and detail
 * - **Selective colorization**: Apply new color schemes to maintain image structure
 * - **Artistic recoloring**: Transform color palettes for creative effects
 * - **Photo enhancement**: Adjust color balance without affecting exposure or contrast
 *
 * ## Usage Examples
 * - **Seasonal color changes**: Transform summer foliage to autumn colors
 * - **Fashion photography**: Change clothing colors while preserving fabric texture
 * - **Architectural visualization**: Test different color schemes for buildings
 * - **Product photography**: Show items in multiple color variations
 * - **Artistic effects**: Create color-shifted versions of photographs
 * - **Color correction**: Fix color casts while maintaining tonal balance
 *
 * ## Technical Details
 * Uses sophisticated HSL (Hue, Saturation, Luminance) color space manipulation:
 * 
 * ### Core Algorithm
 * ```glsl
 * result = setlum(setsat(overlay.rgb, sat(base.rgb)), lum(base.rgb))
 * ```
 * 
 * ### Function Breakdown
 * - **`lum(color)`**: Calculates luminance using weights (0.3, 0.59, 0.11)
 * - **`sat(color)`**: Calculates saturation as max - min channel values
 * - **`setsat(color, s)`**: Sets saturation while preserving hue relationships
 * - **`setlum(color, l)`**: Sets luminance while maintaining hue and saturation
 * - **`clipcolor(color)`**: Clamps values to valid color range [0,1]
 *
 * ### Process Flow
 * 1. Extract saturation from base image
 * 2. Apply base saturation to overlay hue (`setsat`)
 * 3. Extract luminance from base image  
 * 4. Apply base luminance to result (`setlum`)
 * 5. Clip values to prevent color overflow
 * 6. Blend with alpha compositing
 *
 * ## Mathematical Behavior
 * - **Pure hue transfer**: Only hue information comes from overlay
 * - **Preserved structure**: Maintains base image's lighting and detail
 * - **Color space accuracy**: Uses proper HSL conversion algorithms
 * - **Gamut handling**: Clips colors to valid RGB range
 * - **Alpha awareness**: Proper transparency blending
 *
 * ## Visual Characteristics
 * - **Natural appearance**: Maintains realistic lighting and shadows
 * - **Color consistency**: Preserves color relationships within the image
 * - **Detail preservation**: Keeps texture and structural information
 * - **Smooth transitions**: No harsh color boundaries or artifacts
 * - **Realistic results**: Colors appear naturally integrated
 *
 * ## Comparison with Other Blend Modes
 * - **More natural than Color**: Better preserves tonal relationships
 * - **More selective than Overlay**: Only affects hue, not contrast
 * - **Complementary to Saturation blend**: Works on different color components
 * - **Foundation for Color mode**: Color blend uses similar but extended operations
 *
 * ## Creative Applications
 * - **Digital makeup**: Change lipstick or eye colors naturally
 * - **Interior design**: Visualize rooms in different color schemes
 * - **Automotive**: Show cars in various paint colors
 * - **Nature photography**: Enhance or change natural colors selectively
 * - **Graphic design**: Create color variations of logos or designs
 *
 * @see TwoTextureInputKraftShader
 * @see ColorBlendKraftShader for hue + saturation blending
 * @see SaturationBlendKraftShader for saturation-only blending
 * @see LightnessBlendKraftShader for luminance-only blending
 */
class HueBlendKraftShader: TwoTextureInputKraftShader() {
    override fun loadFragmentShader(): String = HUE_BLEND_FRAGMENT_SHADER
}

@Language("GLSL")
private const val HUE_BLEND_FRAGMENT_SHADER = """
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
    
    highp float sat(lowp vec3 c) {
        lowp float n = min(min(c.r, c.g), c.b);
        lowp float x = max(max(c.r, c.g), c.b);
        return x - n;
    }
    
    lowp float mid(lowp float cmin, lowp float cmid, lowp float cmax, highp float s) {
        return ((cmid - cmin) * s) / (cmax - cmin);
    }
    
    lowp vec3 setsat(lowp vec3 c, highp float s) {
        if (c.r > c.g) {
            if (c.r > c.b) {
                if (c.g > c.b) {
                    /* g is mid, b is min */
                    c.g = mid(c.b, c.g, c.r, s);
                    c.b = 0.0;
                } else {
                    /* b is mid, g is min */
                    c.b = mid(c.g, c.b, c.r, s);
                    c.g = 0.0;
                }
                c.r = s;
            } else {
                /* b is max, r is mid, g is min */
                c.r = mid(c.g, c.r, c.b, s);
                c.b = s;
                c.g = 0.0;
            }
        } else if (c.r > c.b) {
            /* g is max, r is mid, b is min */
            c.r = mid(c.b, c.r, c.g, s);
            c.g = s;
            c.b = 0.0;
        } else if (c.g > c.b) {
            /* g is max, b is mid, r is min */
            c.b = mid(c.r, c.b, c.g, s);
            c.g = s;
            c.r = 0.0;
        } else if (c.b > c.g) {
            /* b is max, g is mid, r is min */
            c.g = mid(c.r, c.g, c.b, s);
            c.b = s;
            c.r = 0.0;
        } else {
            c = vec3(0.0);
        }
        return c;
    }
    
    void main() {
        highp vec4 baseColor = texture2D(inputImageTexture, textureCoordinate);
        highp vec4 overlayColor = texture2D(inputImageTexture2, textureCoordinate2);
        
        gl_FragColor = vec4(baseColor.rgb * (1.0 - overlayColor.a) + setlum(setsat(overlayColor.rgb, sat(baseColor.rgb)), lum(baseColor.rgb)) * overlayColor.a, baseColor.a);
    }
"""