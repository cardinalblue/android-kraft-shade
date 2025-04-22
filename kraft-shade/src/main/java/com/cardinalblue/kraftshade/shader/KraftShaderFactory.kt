package com.cardinalblue.kraftshade.shader

import com.cardinalblue.kraftshade.env.GlEnv

/**
 * Factory class for creating KraftShader instances with the appropriate OpenGL ES version.
 * This class helps ensure that shaders are created with the correct version based on the device's capabilities.
 */
object KraftShaderFactory {
    /**
     * Creates a KraftShader instance with the appropriate OpenGL ES version based on the GlEnv.
     * 
     * @param glEnv The GlEnv instance that contains the OpenGL ES version information
     * @param shaderCreator A function that creates a KraftShader instance with the specified OpenGL ES version
     * @return A KraftShader instance with the appropriate OpenGL ES version
     */
    inline fun <reified T : KraftShader> createShader(
        glEnv: GlEnv,
        shaderCreator: (Int) -> T
    ): T {
        return shaderCreator(glEnv.glVersion)
    }

    /**
     * Creates a KraftShader instance with the appropriate OpenGL ES version based on the GlEnv.
     * This version is useful when you need to create a shader with additional parameters.
     * 
     * @param glEnv The GlEnv instance that contains the OpenGL ES version information
     * @param shaderCreator A function that creates a KraftShader instance with the specified OpenGL ES version and additional parameters
     * @param params Additional parameters to pass to the shader creator
     * @return A KraftShader instance with the appropriate OpenGL ES version
     */
    inline fun <reified T : KraftShader, P> createShader(
        glEnv: GlEnv,
        params: P,
        shaderCreator: (Int, P) -> T
    ): T {
        return shaderCreator(glEnv.glVersion, params)
    }

    /**
     * Creates a KraftShader instance with the appropriate OpenGL ES version based on the GlEnv.
     * This version is useful when you need to create a shader with multiple additional parameters.
     * 
     * @param glEnv The GlEnv instance that contains the OpenGL ES version information
     * @param shaderCreator A function that creates a KraftShader instance with the specified OpenGL ES version and additional parameters
     * @param param1 First additional parameter to pass to the shader creator
     * @param param2 Second additional parameter to pass to the shader creator
     * @return A KraftShader instance with the appropriate OpenGL ES version
     */
    inline fun <reified T : KraftShader, P1, P2> createShader(
        glEnv: GlEnv,
        param1: P1,
        param2: P2,
        shaderCreator: (Int, P1, P2) -> T
    ): T {
        return shaderCreator(glEnv.glVersion, param1, param2)
    }

    /**
     * Creates a KraftShader instance with the appropriate OpenGL ES version based on the GlEnv.
     * This version is useful when you need to create a shader with three additional parameters.
     * 
     * @param glEnv The GlEnv instance that contains the OpenGL ES version information
     * @param shaderCreator A function that creates a KraftShader instance with the specified OpenGL ES version and additional parameters
     * @param param1 First additional parameter to pass to the shader creator
     * @param param2 Second additional parameter to pass to the shader creator
     * @param param3 Third additional parameter to pass to the shader creator
     * @return A KraftShader instance with the appropriate OpenGL ES version
     */
    inline fun <reified T : KraftShader, P1, P2, P3> createShader(
        glEnv: GlEnv,
        param1: P1,
        param2: P2,
        param3: P3,
        shaderCreator: (Int, P1, P2, P3) -> T
    ): T {
        return shaderCreator(glEnv.glVersion, param1, param2, param3)
    }
}

/**
 * Extension function for GlEnv to create a KraftShader instance with the appropriate OpenGL ES version.
 * 
 * @param shaderCreator A function that creates a KraftShader instance with the specified OpenGL ES version
 * @return A KraftShader instance with the appropriate OpenGL ES version
 */
inline fun <reified T : KraftShader> GlEnv.createShader(
    noinline shaderCreator: (Int) -> T
): T {
    return KraftShaderFactory.createShader(this, shaderCreator)
}

/**
 * Extension function for GlEnv to create a KraftShader instance with the appropriate OpenGL ES version.
 * This version is useful when you need to create a shader with additional parameters.
 * 
 * @param params Additional parameters to pass to the shader creator
 * @param shaderCreator A function that creates a KraftShader instance with the specified OpenGL ES version and additional parameters
 * @return A KraftShader instance with the appropriate OpenGL ES version
 */
inline fun <reified T : KraftShader, P> GlEnv.createShader(
    params: P,
    noinline shaderCreator: (Int, P) -> T
): T {
    return KraftShaderFactory.createShader(this, params, shaderCreator)
}

/**
 * Extension function for GlEnv to create a KraftShader instance with the appropriate OpenGL ES version.
 * This version is useful when you need to create a shader with multiple additional parameters.
 * 
 * @param param1 First additional parameter to pass to the shader creator
 * @param param2 Second additional parameter to pass to the shader creator
 * @param shaderCreator A function that creates a KraftShader instance with the specified OpenGL ES version and additional parameters
 * @return A KraftShader instance with the appropriate OpenGL ES version
 */
inline fun <reified T : KraftShader, P1, P2> GlEnv.createShader(
    param1: P1,
    param2: P2,
    noinline shaderCreator: (Int, P1, P2) -> T
): T {
    return KraftShaderFactory.createShader(this, param1, param2, shaderCreator)
}

/**
 * Extension function for GlEnv to create a KraftShader instance with the appropriate OpenGL ES version.
 * This version is useful when you need to create a shader with three additional parameters.
 * 
 * @param param1 First additional parameter to pass to the shader creator
 * @param param2 Second additional parameter to pass to the shader creator
 * @param param3 Third additional parameter to pass to the shader creator
 * @param shaderCreator A function that creates a KraftShader instance with the specified OpenGL ES version and additional parameters
 * @return A KraftShader instance with the appropriate OpenGL ES version
 */
inline fun <reified T : KraftShader, P1, P2, P3> GlEnv.createShader(
    param1: P1,
    param2: P2,
    param3: P3,
    noinline shaderCreator: (Int, P1, P2, P3) -> T
): T {
    return KraftShaderFactory.createShader(this, param1, param2, param3, shaderCreator)
}
