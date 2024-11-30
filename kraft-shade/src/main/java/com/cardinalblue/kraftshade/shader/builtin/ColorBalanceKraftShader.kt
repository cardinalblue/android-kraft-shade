package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

class ColorBalanceKraftShader(
    shadows: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f),
    midtones: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f),
    highlights: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f),
    preserveLuminosity: Boolean = true
) : TextureInputKraftShader() {
    var shadows: FloatArray by GlUniformDelegate("shadowsShift")
    var midtones: FloatArray by GlUniformDelegate("midtonesShift")
    var highlights: FloatArray by GlUniformDelegate("highlightsShift")
    private var _preserveLuminosity: Int by GlUniformDelegate("preserveLuminosity")
    var preserveLuminosity: Boolean
        get() = _preserveLuminosity != 0
        set(value) {
            _preserveLuminosity = if (value) 1 else 0
        }

    init {
        this.shadows = shadows
        this.midtones = midtones
        this.highlights = highlights
        this.preserveLuminosity = preserveLuminosity
    }

    override fun loadFragmentShader(): String = COLOR_BALANCE_FRAGMENT_SHADER
}

@Language("GLSL")
private const val COLOR_BALANCE_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;
uniform sampler2D inputImageTexture;
uniform lowp vec3 shadowsShift;
uniform lowp vec3 midtonesShift;
uniform lowp vec3 highlightsShift;
uniform int preserveLuminosity;

lowp vec3 RGBToHSL(lowp vec3 color)
{
    lowp vec3 hsl;
    lowp float fmin = min(min(color.r, color.g), color.b);    //Min. value of RGB
    lowp float fmax = max(max(color.r, color.g), color.b);    //Max. value of RGB
    lowp float delta = fmax - fmin;             //Delta RGB value

    hsl.z = (fmax + fmin) / 2.0; // Luminance

    if (delta == 0.0)		//This is a gray, no chroma...
    {
        hsl.x = 0.0;	// Hue
        hsl.y = 0.0;	// Saturation
    }
    else                                    //Chromatic data...
    {
        if (hsl.z < 0.5)
            hsl.y = delta / (fmax + fmin); // Saturation
        else
            hsl.y = delta / (2.0 - fmax - fmin); // Saturation

        lowp float deltaR = (((fmax - color.r) / 6.0) + (delta / 2.0)) / delta;
        lowp float deltaG = (((fmax - color.g) / 6.0) + (delta / 2.0)) / delta;
        lowp float deltaB = (((fmax - color.b) / 6.0) + (delta / 2.0)) / delta;

        if (color.r == fmax )
            hsl.x = deltaB - deltaG; // Hue
        else if (color.g == fmax)
            hsl.x = (1.0 / 3.0) + deltaR - deltaB; // Hue
        else if (color.b == fmax)
            hsl.x = (2.0 / 3.0) + deltaG - deltaR; // Hue

        if (hsl.x < 0.0)
            hsl.x += 1.0; // Hue
        else if (hsl.x > 1.0)
            hsl.x -= 1.0; // Hue
    }

    return hsl;
}

lowp float HueToRGB(lowp float f1, lowp float f2, lowp float hue)
{
    if (hue < 0.0)
        hue += 1.0;
    else if (hue > 1.0)
        hue -= 1.0;
    lowp float res;
    if ((6.0 * hue) < 1.0)
        res = f1 + (f2 - f1) * 6.0 * hue;
    else if ((2.0 * hue) < 1.0)
        res = f2;
    else if ((3.0 * hue) < 2.0)
        res = f1 + (f2 - f1) * ((2.0 / 3.0) - hue) * 6.0;
    else
        res = f1;
    return res;
}

lowp vec3 HSLToRGB(lowp vec3 hsl)
{
    lowp vec3 rgb;

    if (hsl.y == 0.0)
        rgb = vec3(hsl.z); // Luminance
    else
    {
        lowp float f2;

        if (hsl.z < 0.5)
            f2 = hsl.z * (1.0 + hsl.y);
        else
            f2 = (hsl.z + hsl.y) - (hsl.y * hsl.z);

        lowp float f1 = 2.0 * hsl.z - f2;

        rgb.r = HueToRGB(f1, f2, hsl.x + (1.0/3.0));
        rgb.g = HueToRGB(f1, f2, hsl.x);
        rgb.b = HueToRGB(f1, f2, hsl.x - (1.0/3.0));
    }

    return rgb;
}

lowp float RGBToL(lowp vec3 color)
{
    lowp float fmin = min(min(color.r, color.g), color.b);    //Min. value of RGB
    lowp float fmax = max(max(color.r, color.g), color.b);    //Max. value of RGB

    return (fmax + fmin) / 2.0; // Luminance
}

void main()
{
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    lowp vec3 lightness = textureColor.rgb;

    const lowp float a = 0.25;
    const lowp float b = 0.333;
    const lowp float scale = 0.7;

    lowp vec3 shadows = shadowsShift * (clamp((lightness - b) / -a + 0.5, 0.0, 1.0) * scale);
    lowp vec3 midtones = midtonesShift * (clamp((lightness - b) / a + 0.5, 0.0, 1.0) *
        clamp((lightness + b - 1.0) / -a + 0.5, 0.0, 1.0) * scale);
    lowp vec3 highlights = highlightsShift * (clamp((lightness + b - 1.0) / a + 0.5, 0.0, 1.0) * scale);

    mediump vec3 newColor = textureColor.rgb + shadows + midtones + highlights;
    newColor = clamp(newColor, 0.0, 1.0);

    if (preserveLuminosity != 0) {
        lowp vec3 newHSL = RGBToHSL(newColor);
        lowp float oldLum = RGBToL(textureColor.rgb);
        textureColor.rgb = HSLToRGB(vec3(newHSL.x, newHSL.y, oldLum));
        gl_FragColor = textureColor;
    } else {
        gl_FragColor = vec4(newColor.rgb, textureColor.w);
    }
}
"""
