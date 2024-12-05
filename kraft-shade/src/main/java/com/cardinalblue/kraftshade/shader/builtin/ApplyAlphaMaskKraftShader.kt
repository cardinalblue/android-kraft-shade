package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlColorChannel
import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TwoTextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

/**
 * A shader that applies an alpha mask from a second texture to the first texture.
 * The mask can be taken from any channel (R, G, B, or A) of the second texture.
 * The alpha can also be reversed.
 */
class ApplyAlphaMaskKraftShader(
    reverseAlpha: Boolean = false,
    maskChannel: GlColorChannel = GlColorChannel.A,
) : TwoTextureInputKraftShader() {
    private var internalReverseAlpha: Float by GlUniformDelegate("reverseAlpha")
    private var internalMaskChannel: Float by GlUniformDelegate("maskChannel")

    var reverseAlpha: Boolean = reverseAlpha
        set(value) {
            field = value
            internalReverseAlpha = if (value) 1.0f else 0.0f
        }

    var maskChannel: GlColorChannel = maskChannel
        set(value) {
            field = value
            internalMaskChannel = value.ordinal.toFloat()
        }

    init {
        this.reverseAlpha = reverseAlpha
        this.maskChannel = maskChannel
    }

    override fun loadFragmentShader(): String = APPLY_ALPHA_MASK_FRAGMENT_SHADER
}

@Language("GLSL")
private const val APPLY_ALPHA_MASK_FRAGMENT_SHADER = """
    precision highp float;

    varying vec2 textureCoordinate;
    varying vec2 texture2Coordinate;
    uniform sampler2D inputImageTexture;
    uniform sampler2D inputImageTexture2;

    uniform lowp float reverseAlpha;
    uniform lowp float maskChannel;

    void main() {
        vec4 inputColor = texture2D(inputImageTexture, textureCoordinate);
        vec4 maskColor = texture2D(inputImageTexture2, texture2Coordinate);
        float maskValue = maskColor.r * step(maskChannel, 0.0) + 
                         maskColor.g * step(maskChannel, 1.0) * step(1.0, maskChannel) + 
                         maskColor.b * step(maskChannel, 2.0) * step(2.0, maskChannel) + 
                         maskColor.a * step(3.0, maskChannel);

        maskValue = mix(maskValue, 1.0 - maskValue, reverseAlpha);

        gl_FragColor = vec4(inputColor.rgb, maskValue);
    }
"""
