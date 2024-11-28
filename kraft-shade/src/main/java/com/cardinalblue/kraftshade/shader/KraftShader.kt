package com.cardinalblue.kraftshade.shader

import android.opengl.GLES20
import android.util.Log
import androidx.annotation.CallSuper
import org.intellij.lang.annotations.Language
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import com.cardinalblue.kraftshade.util.SuspendAutoCloseable
import com.cardinalblue.kraftshade.util.KraftLogger
import java.util.LinkedList

typealias GlTask = () -> Unit

abstract class KraftShader : SuspendAutoCloseable {
    var debug: Boolean = false

    private var initialized = false
    private val runOnDraw = LinkedList<Pair<String?, GlTask>>()

    var glProgId = 0
        private set
    private var glAttribPosition = 0
    protected var glAttribTextureCoordinate = 0

    protected var resolution: FloatArray by GlUniformDelegate("resolution", required = false)
        private set

    private val logger = KraftLogger("KraftShader")

    open val debugName: String = this::class.simpleName ?: "Unknown"

    fun log(message: String) {
        if (!debug) return
        Log.d("KraftShader", "[${this.javaClass.simpleName}] $message")
    }

    open fun loadVertexShader(): String {
        return DEFAULT_VERTEX_SHADER
    }

    abstract fun loadFragmentShader(): String

    open fun init() {
        if (initialized) return
        logger.i("Initializing shader program for ${this::class.simpleName}")
        glProgId = OpenGlUtils.loadProgram(loadVertexShader(), loadFragmentShader())
        glAttribPosition = GLES20.glGetAttribLocation(glProgId, "position")
        glAttribTextureCoordinate = GLES20.glGetAttribLocation(glProgId, "inputTextureCoordinate")
        initialized = true
    }

    open fun draw(bufferSize: GlSize, isScreenCoordinate: Boolean) {
        init()
        GLES20.glUseProgram(glProgId)
        // it's fine if the shader doesn't include the definition of resolution
        resolution = bufferSize.vec2
        beforeActualDraw()
        runPendingOnDrawTasks()
        actualDraw(isScreenCoordinate)
        afterActualDraw()
    }

    /**
     * Setup the texture in this method
     */
    @CallSuper
    open fun beforeActualDraw() {
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate)
        GLES20.glVertexAttribPointer(
            glAttribTextureCoordinate,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            OpenGlUtils.glTextureBuffer
        )
    }

    fun actualDraw(isScreenCoordinate: Boolean) {
        val cubeBuffer = if (isScreenCoordinate) {
            OpenGlUtils.glVerticallyFlippedCubeBuffer
        } else {
            OpenGlUtils.glCubeBuffer
        }
        GLES20.glEnableVertexAttribArray(glAttribPosition)
        GLES20.glVertexAttribPointer(
            glAttribPosition,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            cubeBuffer
        )
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(glAttribPosition)
    }

    /**
     * Tear down the texture setup in this method
     */
    open fun afterActualDraw() {
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate)
    }

    suspend fun drawTo(buffer: GlBuffer) {
        buffer.draw {
            draw(buffer.size, buffer.isScreenCoordinate)
        }
    }

    private fun runPendingOnDrawTasks() {
        synchronized(runOnDraw) {
            runOnDraw.forEach { (key, task) ->
                task()
                if (key != null) {
                    log("runOnDraw: $key")
                }
            }
            runOnDraw.clear()
            runOnDraw.add(null to { init() })
        }
    }

    /**
     * If there is task with the same key, the old one will be replaced. If it's null, then the
     * replacement won't happen
     */
    fun runOnDraw(key: String? = null, task: GlTask) {
        synchronized(runOnDraw) {
            if (key != null) {
                val iterator = runOnDraw.iterator()
                while (iterator.hasNext()) {
                    val (oldKey, _) = iterator.next()
                    if (oldKey == key) {
                        iterator.remove()
                        break
                    }
                }
            }
            runOnDraw.add(key to task)
        }
    }

    fun destroy() {
        if (!initialized) return
        logger.i("Destroying shader program: ${this::class.simpleName}")
        GLES20.glDeleteProgram(glProgId)
        initialized = false
    }

    override suspend fun close() {
        destroy()
    }

    fun KraftShaderTextureInput.activate() {
        this.activate(this@KraftShader)
    }

    override fun toString(): String {
        return this::class.simpleName ?: "Unknown KraftShader"
    }

    companion object {
        val DEFAULT_VERTEX_SHADER: String get() = DEFAULT_VERTEX_SHADER_INTERNAL
        val DEFAULT_VERTEX_SHADER_WITHOUT_TEXTURE: String get() = DEFAULT_VERTEX_SHADER_WITHOUT_TEXTURE_INTERNAL
    }
}

@Language("GLSL")
private const val DEFAULT_VERTEX_SHADER_INTERNAL = """
    attribute vec4 position;
    attribute vec4 inputTextureCoordinate;
    varying vec2 textureCoordinate;

    uniform highp vec2 resolution;

    void main()
    {
        gl_Position = position;
        textureCoordinate = inputTextureCoordinate.xy;
    }
"""

@Language("GLSL")
private const val DEFAULT_VERTEX_SHADER_WITHOUT_TEXTURE_INTERNAL = """
    attribute vec4 position;
    void main()
    {
        gl_Position = position;
    }
"""
