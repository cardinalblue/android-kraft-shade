package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import org.intellij.lang.annotations.Language

class VibranceKraftShader(vibrance: Float = 0f) : TextureInputKraftShader() {

    var vibrance: Float by GlUniformDelegate("vibrance")

    init {
        this.vibrance = vibrance
    }

    override fun loadFragmentShader(): String {
        return VIBRANCE_FRAGMENT_SHADER
    }

}

@Language("GLSL")
private const val VIBRANCE_FRAGMENT_SHADER = """
    varying highp vec2 textureCoordinate;
    
    uniform sampler2D inputImageTexture;
    uniform lowp float vibrance;
    
    void main() {
        lowp vec4 color = texture2D(inputImageTexture, textureCoordinate);
        lowp float average = (color.r + color.g + color.b) / 3.0;
        lowp float mx = max(color.r, max(color.g, color.b));
        lowp float amt = (mx - average) * (-vibrance * 3.0);
        color.rgb = mix(color.rgb, vec3(mx), amt);
        gl_FragColor = color;
    }
"""