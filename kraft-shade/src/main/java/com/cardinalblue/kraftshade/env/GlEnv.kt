package com.cardinalblue.kraftshade.env

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLES30
import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.shader.buffer.PixelBuffer
import com.cardinalblue.kraftshade.util.KraftLogger
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.concurrent.Executors
import javax.microedition.khronos.egl.*
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

    /** The EGL10 instance used for all EGL operations */
    val egl10: EGL10 = EGLContext.getEGL() as EGL10

    /** 
     * The EGL display connection.
     * Initialized with the default display and version information.
     */
    val eglDisplay: EGLDisplay = egl10
        .eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        .also { display ->
            val version = IntArray(2)
            egl10.eglInitialize(display, version)
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
    val eglContext: EGLContext = egl10.eglCreateContext(
        eglDisplay,
        eglConfig,
        EGL10.EGL_NO_CONTEXT,
        intArrayOf(
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL10.EGL_NONE,
        ),
    ).also { context ->
        if (context == EGL10.EGL_NO_CONTEXT) {
            val error = egl10.eglGetError()
            logger.e("Failed to create EGL context, error: 0x${Integer.toHexString(error)}")
            throw RuntimeException("Failed to create EGL context")
        }
        logger.i("EGL context created")
    }

    /** The GL10 instance associated with our EGL context */
    val gl10: GL10 = eglContext.gl as GL10

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
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_NONE
        )

        val numConfig = IntArray(1)
        if (!egl10.eglChooseConfig(eglDisplay, configSpec, null, 0, numConfig)) {
            throw IllegalStateException("eglChooseConfig failed")
        }

        val numConfigs = numConfig[0]
        if (numConfigs <= 0) {
            throw IllegalStateException("No configs match configSpec")
        }

        val configs = arrayOfNulls<EGLConfig>(numConfigs)
        if (!egl10.eglChooseConfig(eglDisplay, configSpec, configs, numConfigs, numConfig)) {
            throw IllegalStateException("eglChooseConfig#2 failed")
        }

        return configs[0] ?: throw IllegalStateException("No config chosen")
    }

    init {
        post {
            GLES30.glEnable(GLES30.GL_BLEND)
            GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
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
        return egl10
            .eglCreatePbufferSurface(
                eglDisplay,
                eglConfig,
                intArrayOf(
                    EGL10.EGL_WIDTH, size.width,
                    EGL10.EGL_HEIGHT, size.height,
                    EGL10.EGL_NONE
                )
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
        return egl10
            .eglCreateWindowSurface(
                eglDisplay,
                eglConfig,
                surfaceTexture,
                intArrayOf(
                    EGL10.EGL_NONE
                ),
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
        return egl10.eglSwapBuffers(eglDisplay, eglSurface)
    }

    /**
     * Makes the specified EGL surface and context current.
     * 
     * @param surface The EGL surface to make current, defaults to EGL_NO_SURFACE
     */
    fun makeCurrent(surface: EGLSurface = EGL10.EGL_NO_SURFACE) {
        logger.v("Making surface current")
        egl10.eglMakeCurrent(eglDisplay, surface, surface, eglContext)
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
        egl10.eglDestroyContext(eglDisplay, eglContext)
        egl10.eglTerminate(eglDisplay)
        logger.d("EGL terminated")
        dispatcher.close()
    }

    private companion object {
        /** Version number of the EGL client API */
        const val EGL_CONTEXT_CLIENT_VERSION = 0x3098
        /** Bit indicating OpenGL ES 2.0 support */
        const val EGL_OPENGL_ES2_BIT = 4
    }
}
