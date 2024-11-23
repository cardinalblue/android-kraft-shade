package com.cardinalblue.kraftshade

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.cardinalblue.kraftshade.model.GlSize
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

object OpenGlUtils {
    const val NO_TEXTURE_ID: Int = -1

    val CUBE = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f,
    )

    val VERTICALLY_FLIPPED_CUBE = floatArrayOf(
        -1.0f, 1.0f,
        1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, -1.0f,
    )

    /**
     * For frame buffer objects or PixelBuffer
     */
    val glCubeBuffer: FloatBuffer = CUBE.asFloatBuffer()
        get() {
            field.position(0)
            return field
        }

    /**
     * For screen coordinate (WindowSurface)
     */
    val glVerticallyFlippedCubeBuffer: FloatBuffer = VERTICALLY_FLIPPED_CUBE.asFloatBuffer()
        get() {
            field.position(0)
            return field
        }

    val TEXTURE_NO_ROTATION: FloatArray = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
    )

    val TEXTURE_VERT_FLIP: FloatArray = floatArrayOf(
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
    )

    val glTextureBuffer = TEXTURE_VERT_FLIP.asFloatBuffer()
        get() {
            field.position(0)
            return field
        }

    fun FloatArray.asFloatBuffer(): FloatBuffer {
        return ByteBuffer
            .allocateDirect(size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(this)
            .apply { position(0) }
    }

    @JvmOverloads
    fun loadTexture(img: Bitmap, usedTexId: Int, recycle: Boolean = true): Int {
        val textures = IntArray(1)
        if (usedTexId == NO_TEXTURE_ID) {
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0)
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId)
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, img)
            textures[0] = usedTexId
        }
        if (recycle) {
            img.recycle()
        }
        return textures[0]
    }

    fun loadTexture(data: IntBuffer?, width: Int, height: Int, usedTexId: Int): Int {
        val textures = IntArray(1)
        if (usedTexId == NO_TEXTURE_ID) {
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data
            )
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId)
            GLES20.glTexSubImage2D(
                GLES20.GL_TEXTURE_2D, 0, 0, 0, width,
                height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data
            )
            textures[0] = usedTexId
        }
        return textures[0]
    }

    fun loadShader(strSource: String, iType: Int): Int {
        val compiled = IntArray(1)
        val iShader = GLES20.glCreateShader(iType)
        GLES20.glShaderSource(iShader, strSource)
        GLES20.glCompileShader(iShader)
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.d("Load Shader Failed", """
                 Compilation
                 ${GLES20.glGetShaderInfoLog(iShader)}
             """.trimIndent())
            return 0
        }
        return iShader
    }

    fun loadProgram(strVSource: String, strFSource: String): Int {
        val link = IntArray(1)
        val iVShader = loadShader(strVSource, GLES20.GL_VERTEX_SHADER)
        if (iVShader == 0) {
            Log.d("Load Program", "Vertex Shader Failed")
            return 0
        }
        val iFShader = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER)
        if (iFShader == 0) {
            Log.d("Load Program", "Fragment Shader Failed")
            return 0
        }

        val iProgId = GLES20.glCreateProgram()

        GLES20.glAttachShader(iProgId, iVShader)
        GLES20.glAttachShader(iProgId, iFShader)

        GLES20.glLinkProgram(iProgId)

        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0)
        if (link[0] <= 0) {
            Log.d("Load Program", "Linking Failed")
            return 0
        }
        GLES20.glDeleteShader(iVShader)
        GLES20.glDeleteShader(iFShader)
        return iProgId
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            throw RuntimeException(msg)
        }
    }

    fun createBitmapFromBuffer(size: GlSize): Bitmap {
        val pixels = IntArray(size.width * size.height)
        val buffer = IntBuffer.wrap(pixels)
        GLES20.glReadPixels(0, 0, size.width, size.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)
        return Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888).apply {
            copyPixelsFromBuffer(buffer)
        }
    }
}

inline fun <T> withFrameBufferRestored(action: () -> T): T {
    val fboBound = IntArray(size = 1)
    GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, fboBound, 0)
    try {
        return action()
    } finally {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboBound[0])
    }
}

inline fun <T> withViewPortRestored(action: () -> T): T {
    val viewPort = IntArray(size = 4)
    GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewPort, 0)
    try {
        return action()
    } finally {
        GLES20.glViewport(viewPort[0], viewPort[1], viewPort[2], viewPort[3])
    }
}
