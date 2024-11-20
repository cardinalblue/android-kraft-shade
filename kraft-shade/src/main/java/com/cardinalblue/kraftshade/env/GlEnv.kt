package com.cardinalblue.kraftshade.env

import android.graphics.SurfaceTexture
import com.cardinalblue.kraftshade.model.GlSize
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import com.cardinalblue.kraftshade.shader.buffer.PixelBuffer
import java.util.concurrent.Executors
import javax.microedition.khronos.egl.*
import javax.microedition.khronos.opengles.GL10

class GlEnv {
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    val egl10: EGL10 = EGLContext.getEGL() as EGL10
    val eglDisplay: EGLDisplay = egl10
        .eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        .also { display ->
            val version = IntArray(2)
            egl10.eglInitialize(display, version)
        }

    val eglConfig: EGLConfig = chooseEglConfig()

    val eglContext: EGLContext = run {
        egl10.eglCreateContext(
            eglDisplay,
            eglConfig,
            EGL10.EGL_NO_CONTEXT,
            intArrayOf(
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE,
            ),
        )
    }

    val gl10: GL10 = eglContext.gl as GL10

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

    fun createWindowSurface(surfaceTexture: SurfaceTexture): EGLSurface {
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

    fun createPixelBuffer(width: Int, height: Int): PixelBuffer {
        return PixelBuffer(width, height, this)
    }

    fun swapBuffers(eglSurface: EGLSurface): Boolean {
        return egl10.eglSwapBuffers(eglDisplay, eglSurface)
    }

    fun makeCurrent(surface: EGLSurface = EGL10.EGL_NO_SURFACE) {
        egl10.eglMakeCurrent(eglDisplay, surface, surface, eglContext)
    }

    suspend fun <T> use(block: suspend GlEnv.() -> T): T = withContext(dispatcher) {
        makeCurrent()
        block()
    }

    suspend fun terminate() = use {
        egl10.eglDestroyContext(eglDisplay, eglContext)
        egl10.eglTerminate(eglDisplay)
    }

    private companion object {
        const val EGL_CONTEXT_CLIENT_VERSION = 0x3098
        const val EGL_OPENGL_ES2_BIT = 4
    }
}
