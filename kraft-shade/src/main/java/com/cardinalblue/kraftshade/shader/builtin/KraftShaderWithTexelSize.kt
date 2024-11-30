package com.cardinalblue.kraftshade.shader.builtin

import com.cardinalblue.kraftshade.model.GlSizeF
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate

/**
 * @property texelSize This is going to be the uniform variable in your shader. You should implement
 * this property by [GlUniformDelegate]. You don't have to calculate the value based on resolution
 * of the buffer in [KraftShader.beforeActualDraw] on your own. Writing the value to the shader is
 * not meaningful since it's calculated in the rendering cycle.
 *
 * @property texelSizeRatio You can override it as var, so that it's configurable in your shader.
 */
interface KraftShaderWithTexelSize {
    var texelSize: GlSizeF
    val texelSizeRatio: GlSizeF get() = GlSizeF(1.0f, 1.0f)
}
