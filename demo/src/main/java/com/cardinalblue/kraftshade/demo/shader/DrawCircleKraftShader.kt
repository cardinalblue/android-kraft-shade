package com.cardinalblue.kraftshade.demo.shader

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.model.GlColor
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

class DrawCircleKraftShader(
    color: GlColor = GlColor.Red,
    backgroundColor: GlColor = GlColor.Transparent,
    scale: Float = 1f,
) : KraftShader() {
    var color: GlColor by GlUniformDelegate("color")
    var backgroundColor: GlColor by GlUniformDelegate("bgColor")
    var scale: Float  by GlUniformDelegate("scale")

    init {
        this.color = color
        this.backgroundColor = backgroundColor
        this.scale = scale
    }

    override fun loadVertexShader(): String {
        return DEFAULT_VERTEX_SHADER_WITHOUT_TEXTURE
    }

    override fun loadFragmentShader(): String {
        return DRAW_CIRCLE_FRAGMENT_SHADER
    }

    fun setColor(r: Float, g: Float, b: Float, a: Float = 1f) {
        color = GlColor.normalizedRGBA(r, g, b, a)
    }

    fun setBackgroundColor(r: Float, g: Float, b: Float, a: Float = 1f) {
        backgroundColor = GlColor.normalizedRGBA(r, g, b, a)
    }
}

@Language("GLSL")
const val DRAW_CIRCLE_FRAGMENT_SHADER = """
uniform highp vec2 resolution;
uniform lowp vec4 color;
uniform lowp vec4 bgColor;
uniform lowp float scale;

void main()
{
   lowp float minor = min(resolution.x, resolution.y);
   if (distance(vec2(resolution.x, resolution.y) / 2.0, gl_FragCoord.xy) <= minor * scale / 2.0)
   {
       gl_FragColor = color;
   } else {
       gl_FragColor = bgColor;
   }
}
"""