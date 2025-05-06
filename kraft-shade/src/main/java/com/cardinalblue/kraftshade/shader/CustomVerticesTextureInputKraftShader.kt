package com.cardinalblue.kraftshade.shader

import android.opengl.GLES30
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
    private var vertexDimensionSize: Int = 3

    protected fun setVerticesBuffer(vertices: FloatArray, vertexDimensionSize: Int = 3) {
        verticesBuffer = vertices.asFloatBuffer()
        this.vertexDimensionSize = vertexDimensionSize
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

    override fun beforeActualDraw(isScreenCoordinate: Boolean) {
        super.beforeActualDraw(isScreenCoordinate)

        // set the custom texture coordinates
        GLES30.glEnableVertexAttribArray(glAttribTextureCoordinate)
        GLES30.glVertexAttribPointer(
            glAttribTextureCoordinate,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            textureCoordinatesBuffer
        )
    }

    override fun actualDraw(isScreenCoordinate: Boolean) {
        GLES30.glEnableVertexAttribArray(glAttribPosition)
        GLES30.glVertexAttribPointer(
            glAttribPosition,
            vertexDimensionSize,
            GLES30.GL_FLOAT,
            false,
            0,
            verticesBuffer
        )
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, numberOfIndices, GLES30.GL_UNSIGNED_SHORT, indicesBuffer)
        GLES30.glDisableVertexAttribArray(glAttribPosition)
    }

}
