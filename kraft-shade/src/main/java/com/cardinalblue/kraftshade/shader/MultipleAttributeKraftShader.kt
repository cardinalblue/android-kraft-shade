package com.cardinalblue.kraftshade.shader

import android.opengl.GLES30
import com.cardinalblue.kraftshade.OpenGlUtils.asFloatBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

data class VertexAttributeData2(
    val name: String,
    val size: Int,
    val type: Int = GLES30.GL_FLOAT,
    val normalized: Boolean = false,
    val stride: Int = 0,
    val buffer: FloatBuffer
)


abstract class MultipleAttributeKraftShader : TextureInputKraftShader() {
    private lateinit var verticesBuffer: FloatBuffer
    private lateinit var textureCoordinatesBuffer: FloatBuffer
    private lateinit var indicesBuffer: ShortBuffer
    private var numberOfIndices: Int = 0
    private var vertexDimensionSize: Int = 3

    protected var vertexAttributesList: List<VertexAttributeData2> = emptyList()
    protected var attributeLocations = mutableMapOf<String, Int>()

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

    protected fun setVertexAttributes(attributes: List<VertexAttributeData2>) {
        vertexAttributesList = attributes
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

        vertexAttributesList.forEach { attribute ->
            when (attribute.name) {
                "position" -> {
                    // handled in actualDraw
                }

                "inputTextureCoordinate" -> {
                    // handled above
                }

                else -> {
                    val location = attributeLocations[attribute.name]
                        ?: GLES30.glGetAttribLocation(glProgId, attribute.name).also {
                            if (it != -1) {
                                attributeLocations[attribute.name] = it
                            }
                        }

                    if (location != -1) {
                        GLES30.glEnableVertexAttribArray(location)
                        GLES30.glVertexAttribPointer(
                            location,
                            attribute.size,
                            attribute.type,
                            attribute.normalized,
                            attribute.stride,
                            attribute.buffer
                        )
                    }
                }
            }
        }
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

    override fun afterActualDraw() {
        super.afterActualDraw()

        vertexAttributesList.forEach { attribute ->
            if (attribute.name != "position" && attribute.name != "inputTextureCoordinate") {
                attributeLocations[attribute.name]?.let { location ->
                    if (location != -1) {
                        GLES30.glDisableVertexAttribArray(location)
                    }
                }
            }
        }
    }

}