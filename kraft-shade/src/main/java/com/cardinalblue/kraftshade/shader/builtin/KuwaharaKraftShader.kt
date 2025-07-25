package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

/**
 * Kuwahara image abstraction, drawn from the work of Kyprianidis, et. al. in their publication
 * "Anisotropic Kuwahara Filtering on the GPU" within the GPU Pro collection. This produces an oil-painting-like
 * image, but it is extremely computationally expensive, so it can take seconds to render a frame.
 * This might be best used for still images.
 */
class KuwaharaKraftShader : TextureInputKraftShader() {
    /**
     * The radius to sample from when creating the brush-stroke effect, with a default of 3.
     * The larger the radius, the slower the filter.
     */
    var radius: Int by GlUniformDelegate("radius")

    override fun loadFragmentShader(): String = KUWAHARA_FRAGMENT_SHADER

    init {
        radius = 3
    }
}

@Language("glsl")
const val KUWAHARA_FRAGMENT_SHADER = """
precision highp float;

uniform sampler2D inputImageTexture;
uniform int radius;

varying vec2 textureCoordinate;

const vec2 src_size = vec2 (1.0 / 768.0, 1.0 / 1024.0);

void main() {
    vec2 uv = textureCoordinate;
    float n = float((radius + 1) * (radius + 1));
    int i; int j;
    vec3 m0 = vec3(0.0); vec3 m1 = vec3(0.0); vec3 m2 = vec3(0.0); vec3 m3 = vec3(0.0);
    vec3 s0 = vec3(0.0); vec3 s1 = vec3(0.0); vec3 s2 = vec3(0.0); vec3 s3 = vec3(0.0);
    vec3 c;

    for (j = -radius; j <= 0; ++j)  {
        for (i = -radius; i <= 0; ++i)  {
            c = texture2D(inputImageTexture, uv + vec2(i,j) * src_size).rgb;
            m0 += c;
            s0 += c * c;
        }
    }

    for (j = -radius; j <= 0; ++j)  {
        for (i = 0; i <= radius; ++i)  {
            c = texture2D(inputImageTexture, uv + vec2(i,j) * src_size).rgb;
            m1 += c;
            s1 += c * c;
        }
    }

    for (j = 0; j <= radius; ++j)  {
        for (i = 0; i <= radius; ++i)  {
            c = texture2D(inputImageTexture, uv + vec2(i,j) * src_size).rgb;
            m2 += c;
            s2 += c * c;
        }
    }

    for (j = 0; j <= radius; ++j)  {
        for (i = -radius; i <= 0; ++i)  {
            c = texture2D(inputImageTexture, uv + vec2(i,j) * src_size).rgb;
            m3 += c;
            s3 += c * c;
        }
    }

    float min_sigma2 = 1e+2;
    m0 /= n;
    s0 = abs(s0 / n - m0 * m0);

    float sigma2 = s0.r + s0.g + s0.b;
    if (sigma2 < min_sigma2) {
        min_sigma2 = sigma2;
        gl_FragColor = vec4(m0, 1.0);
    }

    m1 /= n;
    s1 = abs(s1 / n - m1 * m1);

    sigma2 = s1.r + s1.g + s1.b;
    if (sigma2 < min_sigma2) {
        min_sigma2 = sigma2;
        gl_FragColor = vec4(m1, 1.0);
    }

    m2 /= n;
    s2 = abs(s2 / n - m2 * m2);

    sigma2 = s2.r + s2.g + s2.b;
    if (sigma2 < min_sigma2) {
        min_sigma2 = sigma2;
        gl_FragColor = vec4(m2, 1.0);
    }

    m3 /= n;
    s3 = abs(s3 / n - m3 * m3);

    sigma2 = s3.r + s3.g + s3.b;
    if (sigma2 < min_sigma2) {
        min_sigma2 = sigma2;
        gl_FragColor = vec4(m3, 1.0);
    }
}
"""
