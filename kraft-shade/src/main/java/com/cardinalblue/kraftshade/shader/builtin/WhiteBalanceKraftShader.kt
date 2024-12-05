package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

class WhiteBalanceKraftShader(
    temperature: Float = 5000.0f,
    tint: Float = 0.0f,
) : TextureInputKraftShader() {

    private var internalTemperature: Float by GlUniformDelegate("temperature")

    /**
     * Temperature adjustment value. Default is 5000.0.
     * Values below 5000 give a cooler look, values above 5000 give a warmer look.
     */
    var temperature: Float = temperature
        set(value) {
            field = value
            internalTemperature = if (value < 5000) {
                0.0004f * (value - 5000.0f)
            } else {
                0.00006f * (value - 5000.0f)
            }
        }

    private var internalTint: Float by GlUniformDelegate("tint")

    /**
     * Tint adjustment value, from -100 to 100. Default is 0.0.
     */
    var tint: Float = tint
        set(value) {
            field = value
            internalTint = value / 100.0f
        }

    init {
        this.temperature = temperature
        this.tint = tint
    }

    override fun loadFragmentShader(): String = WHITE_BALANCE_FRAGMENT_SHADER
}

@Language("GLSL")
private const val WHITE_BALANCE_FRAGMENT_SHADER = """
varying highp vec2 textureCoordinate;
uniform sampler2D inputImageTexture;
uniform lowp float temperature;
uniform lowp float tint;

const lowp vec3 warmFilter = vec3(0.93, 0.54, 0.0);

const mediump mat3 RGBtoYIQ = mat3(
    0.299, 0.587, 0.114,
    0.596, -0.274, -0.322,
    0.212, -0.523, 0.311
);

const mediump mat3 YIQtoRGB = mat3(
    1.0, 0.956, 0.621,
    1.0, -0.272, -0.647,
    1.0, -1.105, 1.702
);

void main() {
    lowp vec4 source = texture2D(inputImageTexture, textureCoordinate);

    mediump vec3 yiq = RGBtoYIQ * source.rgb; //adjusting tint
    yiq.b = clamp(yiq.b + tint*0.5226*0.1, -0.5226, 0.5226);
    lowp vec3 rgb = YIQtoRGB * yiq;

    lowp vec3 processed = vec3(
        (rgb.r < 0.5 ? (2.0 * rgb.r * warmFilter.r) : (1.0 - 2.0 * (1.0 - rgb.r) * (1.0 - warmFilter.r))), //adjusting temperature
        (rgb.g < 0.5 ? (2.0 * rgb.g * warmFilter.g) : (1.0 - 2.0 * (1.0 - rgb.g) * (1.0 - warmFilter.g))), 
        (rgb.b < 0.5 ? (2.0 * rgb.b * warmFilter.b) : (1.0 - 2.0 * (1.0 - rgb.b) * (1.0 - warmFilter.b)))
    );

    gl_FragColor = vec4(mix(rgb, processed, temperature), source.a);
}
"""
