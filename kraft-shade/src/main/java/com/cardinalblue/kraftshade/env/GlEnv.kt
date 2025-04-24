package com.cardinalblue.kraftshade.env

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.PixelBuffer
import com.cardinalblue.kraftshade.util.KraftLogger
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.concurrent.Executors
import javax.microedition.khronos.opengles.GL10

/**
 * Manages the OpenGL ES environment and EGL context.
 * This class handles the initialization of EGL, creation of surfaces, and management of the GL context.
 */
class GlEnv(
    context: Context,
) {
    val appContext: Context = context.applicationContext

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val logger = KraftLogger("GlEnv")

    /**
     * The EGL display connection.
     * Initialized with the default display and version information.
     */
    val eglDisplay: EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY).also { display ->
        val version = IntArray(2)
        EGL14.eglInitialize(display, version, 0, version, 1)
        logger.i("EGL initialized with version ${version[0]}.${version[1]}")
    }

    /** The chosen EGL configuration that matches our requirements */
    val eglConfig: EGLConfig = chooseEglConfig().also {
        logger.d("EGL config chosen")
    }

    /**
     * The EGL context created with OpenGL ES 2.0 support.
     * This context is essential for all OpenGL operations.
     */
    val eglContext: EGLContext = EGL14.eglCreateContext(
        eglDisplay,
        eglConfig,
        EGL14.EGL_NO_CONTEXT,
        intArrayOf(
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        ), 0
    ).also { context ->
        if (context == EGL14.EGL_NO_CONTEXT) {
            val error = EGL14.eglGetError()
            logger.e("Failed to create EGL context, error: 0x${Integer.toHexString(error)}")
            throw RuntimeException("Failed to create EGL context")
        }
        logger.i("EGL context created")
    }

    /** The GL10 instance associated with our EGL context */
    /** The OpenGL ES version (2 or 3) */
    val glVersion: Int = 3
//    val gl10: GL10? = null // EGL14 doesn't provide direct GL access like EGL10 did

    private val dslScope: GlEnvDslScope by lazy { GlEnvDslScope(this) }

    private val deferredTasks: MutableList<suspend GlEnvDslScope.() -> Unit> = Collections.synchronizedList(mutableListOf())

    /**
     * Chooses an appropriate EGL configuration that matches our rendering requirements.
     *
     * @return The chosen EGL configuration
     * @throws IllegalStateException if no suitable configuration is found or if the selection process fails
     */
    private fun chooseEglConfig(): EGLConfig {
        val configSpec = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
        )

        val numConfig = IntArray(1)
        val configs = arrayOfNulls<EGLConfig>(1)
        if (!EGL14.eglChooseConfig(eglDisplay, configSpec, 0, configs, 0, 1, numConfig, 0)) {
            throw IllegalStateException("eglChooseConfig failed")
        }

        val numConfigs = numConfig[0]
        if (numConfigs <= 0) {
            throw IllegalStateException("No configs match configSpec")
        }

        return configs[0] ?: throw IllegalStateException("No config chosen")
    }

    init {
        post {
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        }
    }

    /**
     * The [task] posted here will be deferred to next [execute] call. It will be executed before
     * the block passed to [execute] is executed.
     */
    fun post(task: suspend GlEnvDslScope.() -> Unit) {
        deferredTasks.add(task)
    }

    private suspend fun executeDeferredTasks() {
        deferredTasks.forEach { task ->
            task(dslScope)
        }
        deferredTasks.clear()
    }

    /**
     * Creates a pixel buffer surface with the specified size.
     *
     * @param size The size of the pixel buffer surface
     * @return The created EGL surface
     */
    fun createPbufferSurface(size: GlSize): EGLSurface {
        return EGL14.eglCreatePbufferSurface(
            eglDisplay,
            eglConfig,
            intArrayOf(
                EGL14.EGL_WIDTH, size.width,
                EGL14.EGL_HEIGHT, size.height,
                EGL14.EGL_NONE
            ), 0
        )
    }

    /**
     * Creates a window surface from a SurfaceTexture.
     *
     * @param surfaceTexture The Android SurfaceTexture to create the surface from
     * @return The created EGL surface
     */
    fun createWindowSurface(surfaceTexture: SurfaceTexture): EGLSurface {
        logger.d("Creating window surface")
        return EGL14.eglCreateWindowSurface(
            eglDisplay,
            eglConfig,
            surfaceTexture,
            intArrayOf(
                EGL14.EGL_NONE
            ), 0
        )
    }

    /**
     * Creates a pixel buffer for off-screen rendering.
     *
     * @param width The width of the pixel buffer
     * @param height The height of the pixel buffer
     * @return A new PixelBuffer instance
     */
    fun createPixelBuffer(width: Int, height: Int): PixelBuffer {
        return PixelBuffer(width, height, this)
    }

    /**
     * Swaps the buffers for the specified EGL surface.
     * This is typically called after rendering to display the results.
     *
     * @param eglSurface The EGL surface to swap buffers for
     * @return true if the buffer swap was successful, false otherwise
     */
    fun swapBuffers(eglSurface: EGLSurface): Boolean {
        logger.v("Swapping buffers")
        return EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    /**
     * Makes the specified EGL surface and context current.
     *
     * @param surface The EGL surface to make current, defaults to EGL_NO_SURFACE
     */
    fun makeCurrent(surface: EGLSurface = EGL14.EGL_NO_SURFACE) {
        logger.v("Making surface current")
        EGL14.eglMakeCurrent(eglDisplay, surface, surface, eglContext)
    }

    /**
     * Executes the provided block with the GL context made current.
     * This function ensures all GL operations are performed on the correct thread.
     *
     * @param block The suspend function to execute within the GL context
     * @return The result of the block execution
     */
    suspend fun <T> execute(block: suspend GlEnvDslScope.() -> T): T = withContext(dispatcher) {
        makeCurrent()
        executeDeferredTasks()
        block(dslScope)
    }

    suspend fun <T> use(block: suspend GlEnvDslScope.() -> T): T {
        try {
            return execute(block)
        } catch (e: Exception) {
            throw e
        } finally {
            terminate()
        }
    }

    /**
     * Terminates the GL environment, cleaning up all resources.
     * This should be called when the GL environment is no longer needed.
     */
    suspend fun terminate() = execute {
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        EGL14.eglTerminate(eglDisplay)
        logger.d("EGL terminated")
        dispatcher.close()
    }

    private companion object {
        /** Version number of the EGL client API */
        const val EGL_CONTEXT_CLIENT_VERSION = EGL14.EGL_CONTEXT_CLIENT_VERSION
        /** Bit indicating OpenGL ES 2.0 support */
        const val EGL_OPENGL_ES2_BIT = EGL14.EGL_OPENGL_ES2_BIT
    }
}
