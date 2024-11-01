package com.cardinalblue.kraftshade.env

import android.graphics.SurfaceTexture
import com.cardinalblue.kraftshade.model.GlSize
import javax.microedition.khronos.egl.*
import javax.microedition.khronos.opengles.GL10

class ProtectedGlEnv internal constructor() {
    val egl10: EGL10 = EGLContext.getEGL() as EGL10
    val eglDisplay: EGLDisplay = egl10
        .eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        .also { display ->
            val version = IntArray(2)
            egl10.eglInitialize(display, version)
        }

    val eglConfig: EGLConfig = run {
        val attribList = intArrayOf(
            EGL10.EGL_DEPTH_SIZE, 0,
            EGL10.EGL_STENCIL_SIZE, 0,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_RENDERABLE_TYPE, 4,
            EGL10.EGL_NONE
        )

        // No error checking performed, minimum required code to elucidate logic
        // Expand on this logic to be more selective in choosing a configuration
        val numConfig = IntArray(1)
        egl10.eglChooseConfig(eglDisplay, attribList, null, 0, numConfig)
        val configSize = numConfig[0]
        val eglConfigs: Array<EGLConfig?> = arrayOfNulls(configSize)
        egl10.eglChooseConfig(eglDisplay, attribList, eglConfigs, configSize, numConfig)
        eglConfigs[0]!! // Best match is probably the first configuration
    }

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

    fun swapBuffers(eglSurface: EGLSurface): Boolean {
        return egl10.eglSwapBuffers(eglDisplay, eglSurface)
    }

    fun terminate() {
        egl10.eglDestroyContext(eglDisplay, eglContext)
        egl10.eglTerminate(eglDisplay)
    }

    fun makeCurrent(surface: EGLSurface = EGL10.EGL_NO_SURFACE) {
        egl10.eglMakeCurrent(eglDisplay, surface, surface, eglContext)
    }

    private companion object {
        const val EGL_CONTEXT_CLIENT_VERSION = 0x3098
    }
}
