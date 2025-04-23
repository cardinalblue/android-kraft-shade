package com.cardinalblue.kraftshade.shader

import android.opengl.GLES30
import androidx.annotation.CallSuper
import com.cardinalblue.kraftshade.OpenGlUtils
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.model.GlSizeF
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.builtin.KraftShaderWithTexelSize
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import com.cardinalblue.kraftshade.util.KraftLogger
import com.cardinalblue.kraftshade.util.SuspendAutoCloseable
import org.intellij.lang.annotations.Language
import java.util.LinkedList

typealias GlTask = () -> Unit

abstract class KraftShader : SuspendAutoCloseable {
    var debug: Boolean = false
    var clearColorBeforeDraw: Boolean = true

    private var initialized = false
    private val runOnDraw = LinkedList<Pair<String?, GlTask>>()

    var glProgId = 0
        private set
    protected var glAttribPosition = 0
    protected var glAttribTextureCoordinate = 0

    protected open var resolution: GlSize by GlUniformDelegate("resolution", required = false)

    open val debugName: String = this::class.simpleName ?: "Unknown"

    protected val logger = KraftLogger(this::class.simpleName ?: "KraftShader")

    open fun loadVertexShader(): String {
        return DEFAULT_VERTEX_SHADER
    }

    abstract fun loadFragmentShader(): String

    private fun loadShader(strSource: String, iType: Int): Int {
        val compiled = IntArray(1)
        val iShader = GLES30.glCreateShader(iType)
        GLES30.glShaderSource(iShader, strSource)
        GLES30.glCompileShader(iShader)
        GLES30.glGetShaderiv(iShader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val fragmentType: String = if (iType == GLES30.GL_FRAGMENT_SHADER) "fragment" else "vertex"
            throw RuntimeException("""
                Compilation failed - $debugName - $fragmentType
                ${GLES30.glGetShaderInfoLog(iShader)}
            """.trimIndent())
        }
        return iShader
    }

    private fun loadProgram(strVSource: String, strFSource: String): Int {
        val link = IntArray(1)
        val iVShader = loadShader(strVSource, GLES30.GL_VERTEX_SHADER)
        val iFShader = loadShader(strFSource, GLES30.GL_FRAGMENT_SHADER)
        val iProgId = GLES30.glCreateProgram()

        GLES30.glAttachShader(iProgId, iVShader)
        GLES30.glAttachShader(iProgId, iFShader)

        GLES30.glLinkProgram(iProgId)

        GLES30.glGetProgramiv(iProgId, GLES30.GL_LINK_STATUS, link, 0)
        if (link[0] <= 0) {
            logger.e("[$debugName] Linking Failed")
            throw RuntimeException("Failed to link program:\n${GLES30.glGetProgramInfoLog(iProgId)}")
        }
        GLES30.glDeleteShader(iVShader)
        GLES30.glDeleteShader(iFShader)
        return iProgId
    }

    open fun init() {
        if (initialized) return
        logger.i("Initializing shader program for ${this::class.simpleName}")
        glProgId = loadProgram(loadVertexShader(), loadFragmentShader())
        glAttribPosition = GLES30.glGetAttribLocation(glProgId, "position")
        glAttribTextureCoordinate = GLES30.glGetAttribLocation(glProgId, "inputTextureCoordinate")
        initialized = true
    }

    open fun draw(bufferSize: GlSize, isScreenCoordinate: Boolean) {
        init()
        GLES30.glUseProgram(glProgId)
        // it's fine if the shader doesn't include the definition of resolution
        resolution = bufferSize
        // resolution is now ready for read, so we can calculate the texel size in here
        updateTexelSize()
        beforeActualDraw(isScreenCoordinate)
        runPendingOnDrawTasks()
        actualDraw(isScreenCoordinate)
        afterActualDraw()
    }

    open fun updateTexelSize() {
        if (this is KraftShaderWithTexelSize) {
            texelSize = GlSizeF(
                texelSizeRatio.width / resolution.width,
                texelSizeRatio.height / resolution.height
            )
        }
    }

    /**
     * Setup the texture in this method
     */
    @CallSuper
    open fun beforeActualDraw(isScreenCoordinate: Boolean) {
        if (clearColorBeforeDraw) {
            GLES30.glClearColor(0f, 0f, 0f, 0f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        }

        GLES30.glEnableVertexAttribArray(glAttribTextureCoordinate)
        GLES30.glVertexAttribPointer(
            glAttribTextureCoordinate,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            OpenGlUtils.glTextureBuffer
        )
    }

    open fun actualDraw(isScreenCoordinate: Boolean) {
        val cubeBuffer = if (isScreenCoordinate) {
            OpenGlUtils.glVerticallyFlippedCubeBuffer
        } else {
            OpenGlUtils.glCubeBuffer
        }
        GLES30.glEnableVertexAttribArray(glAttribPosition)
        GLES30.glVertexAttribPointer(
            glAttribPosition,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            cubeBuffer
        )
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glDisableVertexAttribArray(glAttribPosition)
    }

    /**
     * Tear down the texture setup in this method
     */
    open fun afterActualDraw() {
        GLES30.glDisableVertexAttribArray(glAttribTextureCoordinate)
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
                    logger.v("runOnDraw: $key")
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

    protected fun resetBlendEquation() {
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
    }

    fun destroy() {
        if (!initialized) return
        logger.i("Destroying shader program: ${this::class.simpleName}")
        GLES30.glDeleteProgram(glProgId)
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
    #version 300 es
    in vec4 position;
    in vec4 inputTextureCoordinate;
    out vec2 textureCoordinate;

    uniform highp vec2 resolution;

    void main()
    {
        gl_Position = position;
        textureCoordinate = inputTextureCoordinate.xy;
    }
"""

@Language("GLSL")
private const val DEFAULT_VERTEX_SHADER_WITHOUT_TEXTURE_INTERNAL = """
    #version 300 es
    in vec4 position;
    void main()
    {
        gl_Position = position;
    }
"""
