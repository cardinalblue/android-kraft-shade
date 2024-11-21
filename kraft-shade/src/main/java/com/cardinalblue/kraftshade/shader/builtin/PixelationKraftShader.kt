package com.cardinalblue.kraftshade.shader.builtin

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

/**
 * A shader that applies a pixelation effect to the image.
 * The pixel parameter controls the size of the pixelation blocks.
 *
 * @param pixel The size of pixelation blocks, default is 1.0f
 */
class PixelationKraftShader(
    pixel: Float = 1.0f,
) : TextureInputKraftShader() {
    /**
     * The size of pixelation blocks.
     * Higher values create larger pixels.
     */
    var pixel: Float by GlUniformDelegate("pixel")

    init {
        this.pixel = pixel
    }

    override fun loadFragmentShader(): String = PIXELATION_FRAGMENT_SHADER
}

@Language("GLSL")
private const val PIXELATION_FRAGMENT_SHADER = """
precision highp float;

varying vec2 textureCoordinate;

uniform sampler2D inputImageTexture;
uniform float pixel;
uniform vec2 resolution;

void main()
{
  vec2 uv = textureCoordinate.xy;
  float dx = pixel / resolution.x;
  float dy = pixel / resolution.y;
  vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));
  vec3 tc = texture2D(inputImageTexture, coord).xyz;
  gl_FragColor = vec4(tc, 1.0);
}
"""
