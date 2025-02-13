package com.cardinalblue.kraftshade.shader

import android.opengl.GLES20
import com.cardinalblue.kraftshade.OpenGlUtils.asFloatBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

abstract  class CustomVerticesTextureInputKraftShader: TextureInputKraftShader() {
    private lateinit var verticesBuffer: FloatBuffer
    private lateinit var textureCoordinatesBuffer: FloatBuffer
    private lateinit var indicesBuffer: ShortBuffer
    private var numberOfIndices: Int = 0

    protected fun setVerticesBuffer(vertices: FloatArray) {
        verticesBuffer = vertices.asFloatBuffer()
    }

    protected fun setTextureCoordinatesBuffer(textureCoordinates: FloatArray) {
        textureCoordinatesBuffer = textureCoordinates.asFloatBuffer()
    }

    protected fun setIndicesBuffer(indices: ShortArray) {
        numberOfIndices = indices.size
        indicesBuffer = ByteBuffer
            .allocateDirect(indices.size * Short.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indices)
            .apply { position(0) }
    }

    override fun beforeActualDraw() {
        super.beforeActualDraw()

        // set the custom texture coordinates
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate)
        GLES20.glVertexAttribPointer(
            glAttribTextureCoordinate,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            textureCoordinatesBuffer
        )
    }

    override fun actualDraw(isScreenCoordinate: Boolean) {
        GLES20.glEnableVertexAttribArray(glAttribPosition)
        GLES20.glVertexAttribPointer(
            glAttribPosition,
            3,
            GLES20.GL_FLOAT,
            false,
            0,
            verticesBuffer
        )
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numberOfIndices, GLES20.GL_UNSIGNED_SHORT, indicesBuffer)
        GLES20.glDisableVertexAttribArray(glAttribPosition)
    }

}