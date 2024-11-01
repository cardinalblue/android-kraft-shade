package com.cardinalblue.kraftshade.shader

import android.graphics.PointF
import android.opengl.GLES20
import java.nio.FloatBuffer

fun KraftShader.setInteger(location: Int, intValue: Int) {
    runOnDraw {
        GLES20.glUniform1i(location, intValue)
    }
}

fun KraftShader.setFloat(location: Int, floatValue: Float) {
    runOnDraw {
        GLES20.glUniform1f(location, floatValue)
    }
}

fun KraftShader.setFloatVec2(location: Int, arrayValue: FloatArray) {
    runOnDraw {
        GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue))
    }
}

fun KraftShader.setFloatVec2(location: Int, value1: Float, value2: Float) {
    runOnDraw {
        setFloatVec2(location, floatArrayOf(value1, value2))
    }
}

fun KraftShader.setFloatVec3(location: Int, arrayValue: FloatArray) {
    runOnDraw {
        GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue))
    }
}

fun KraftShader.setFloatVec4(location: Int, arrayValue: FloatArray) {
    runOnDraw {
        GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue))
    }
}

fun KraftShader.setFloatArray(location: Int, arrayValue: FloatArray) {
    runOnDraw {
        GLES20.glUniform1fv(location, arrayValue.size, FloatBuffer.wrap(arrayValue))
    }
}

fun KraftShader.setPoint(location: Int, point: PointF) {
    runOnDraw {
        val vec2 = FloatArray(2)
        vec2[0] = point.x
        vec2[1] = point.y
        GLES20.glUniform2fv(location, 1, vec2, 0)
    }
}

fun KraftShader.setUniformMatrix3f(location: Int, matrix: FloatArray) {
    runOnDraw {
        GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0)
    }
}

fun KraftShader.setUniformMatrix4f(location: Int, matrix: FloatArray) {
    runOnDraw {
        GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0)
    }
}
