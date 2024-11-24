package com.cardinalblue.kraftshade.demo.shader

import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.model.Color
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

class DrawCircleKraftShader(
    color: Color = Color.Red,
    backgroundColor: Color = Color.Transparent
) : KraftShader() {
    private var color: FloatArray by GlUniformDelegate("color")
    private var backgroundColor: FloatArray by GlUniformDelegate("bgColor")

    init {
        this.color = color.vec4
        this.backgroundColor = backgroundColor.vec4
    }

    override fun loadVertexShader(): String {
        return DEFAULT_VERTEX_SHADER_WITHOUT_TEXTURE
    }

    override fun loadFragmentShader(): String {
        return DRAW_CIRCLE_FRAGMENT_SHADER
    }

    fun setColor(r: Float, g: Float, b: Float, a: Float = 1f) {
        color = floatArrayOf(r, g, b, a)
    }

    fun setColor(color: Color) {
        this.color = color.vec4
    }

    fun setBackgroundColor(r: Float, g: Float, b: Float, a: Float = 1f) {
        backgroundColor = floatArrayOf(r, g, b, a)
    }

    fun setBackgroundColor(color: Color) {
        backgroundColor = color.vec4
    }
}

@Language("GLSL")
const val DRAW_CIRCLE_FRAGMENT_SHADER = """
uniform highp vec2 resolution;
uniform lowp vec4 color;
uniform lowp vec4 bgColor;

void main()
{
   lowp float minor = min(resolution.x, resolution.y);
   if (distance(vec2(resolution.x, resolution.y) / 2.0, gl_FragCoord.xy) <= minor / 2.0)
   {
       gl_FragColor = color;
   } else {
       gl_FragColor = bgColor;
   }
}
"""