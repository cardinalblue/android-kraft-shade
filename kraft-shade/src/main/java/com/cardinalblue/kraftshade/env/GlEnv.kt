package com.cardinalblue.kraftshade.env

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import com.cardinalblue.kraftshade.shader.buffer.PixelBuffer
import java.util.concurrent.Executors
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLSurface

class GlEnv {
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val protectedGlEnv by lazy { ProtectedGlEnv()}

    suspend fun <T> use(block: suspend GlEnv.(env: ProtectedGlEnv) -> T) = withContext(dispatcher) {
        protectedGlEnv.makeCurrent()
        block(protectedGlEnv)
    }

    suspend fun createPixelBuffer(width: Int, height: Int) = use { env ->
        PixelBuffer(width, height, env)
    }

    suspend fun terminate() = use { env ->
        env.terminate()
    }
}
