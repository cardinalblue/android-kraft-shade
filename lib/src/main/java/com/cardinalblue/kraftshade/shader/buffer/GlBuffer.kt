package com.cardinalblue.kraftshade.shader.buffer

import android.opengl.GLES20
import com.cardinalblue.kraftshade.model.GlSize

interface GlBuffer : AutoCloseable {
    val isScreenCoordinate: Boolean
    val size: GlSize
    fun beforeDraw()
    fun afterDraw()
    fun delete()

    fun drawTo(draw: GlBuffer.() -> Unit) {
        beforeDraw()
        GLES20.glViewport(0, 0, size.width, size.height)
        draw()
        afterDraw()
    }
}