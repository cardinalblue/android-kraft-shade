package com.cardinalblue.kraftshade

import android.opengl.GLES20
import android.util.Log
import com.cardinalblue.kraftshade.OpenGlUtils.asFloatBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

interface CustomAttributesKraftShaderInterface {
    // custom vertices, textureCoords, indices
    var verticesBuffer: FloatBuffer
    var textureCoordinatesBuffer: FloatBuffer
    var indicesBuffer: ShortBuffer
    var numberOfIndices: Int

    // for handling other custom attributes
    // EXCLUDING position and texture coordinates
    var attributeNames: List<String>
    var attributeLocationsMap: MutableMap<String, Int>
    var attributesSizeMap: MutableMap<String, Int>
    var attributesBufferMap: MutableMap<String, FloatBuffer>

    fun setupAttributes(programId: Int) {
        for (attribName in attributeNames) {
            val attribLocation = GLES20.glGetAttribLocation(programId, attribName)
            attributeLocationsMap[attribName] = attribLocation
        }
    }

    fun setVerticesBuffer(vertices: FloatArray ) {
        verticesBuffer = vertices.asFloatBuffer()
    }

    fun setTextureCoordinatesBuffer(textureCoordinates: FloatArray) {
        textureCoordinatesBuffer = textureCoordinates.asFloatBuffer()
    }

    fun setIndicesBuffer(indices: ShortArray ) {
        numberOfIndices = indices.size
        indicesBuffer = ByteBuffer
            .allocateDirect(indices.size * Short.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indices)
            .apply { position(0) }
    }

    fun setCustomAttributesBuffer(attribName: String, customBuffer: FloatArray) {
        attributesBufferMap[attribName] = customBuffer.asFloatBuffer()
    }

    // pass the attributes, besides position, to the GPU
    // call this function in the "beforeActualDraw" function
    fun passCustomAttributes(glAttribTextureCoordinate: Int) {
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

        // set other attributes
        for (attribName in attributeNames) {
            val location = attributeLocationsMap[attribName]
            val size = attributesSizeMap[attribName]
            val buffer = attributesBufferMap[attribName]
            if (location != null && size != null && buffer != null) {
                GLES20.glEnableVertexAttribArray(location)
                GLES20.glVertexAttribPointer(
                    location,
                    size,
                    GLES20.GL_FLOAT,
                    false,
                    0,
                    buffer
                )
            }
        }
    }

    // this function is used to pass the vertex coordinates to GPU
    // Call this function inside "actualDraw" function
    // before calling draw call (glDrawElements, glDrawArrays, etc)
    fun passVertexCoordinates(glAttribPosition: Int) {
        GLES20.glEnableVertexAttribArray(glAttribPosition)
        if (verticesBuffer == null) {
            Log.d("TAGTAG", "null vertices, abort")
            return
        }
        GLES20.glVertexAttribPointer(
            glAttribPosition,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            verticesBuffer
        )
    }

    // call this function in the "afterActualDraw" function
    fun disableAttributes() {
        for (attribName in attributeNames) {
            val location = attributeLocationsMap[attribName]
            if (location != null) {
                GLES20.glDisableVertexAttribArray(location)
            }
        }
    }
}