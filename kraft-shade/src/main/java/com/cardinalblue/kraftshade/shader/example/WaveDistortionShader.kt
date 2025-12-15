package com.cardinalblue.kraftshade.shader.example

import com.cardinalblue.kraftshade.shader.MultiAttributeKraftShader
import org.intellij.lang.annotations.Language

/**
 * Example shader that demonstrates the use of multiple vertex attributes.
 * This shader creates a wave distortion effect using custom vertex attributes
 * for wave parameters that can vary per vertex.
 * 
 * Vertex attributes:
 * - position: vec4 (standard position)
 * - inputTextureCoordinate: vec4 (standard texture coordinate)
 * - waveParams: vec2 (custom attribute - x: amplitude, y: frequency)
 * - timeOffset: float (custom attribute - per-vertex time offset for animation)
 */
class WaveDistortionShader : MultiAttributeKraftShader() {
    
    override fun loadVertexShader(): String = WAVE_DISTORTION_VERTEX_SHADER
    
    override fun loadFragmentShader(): String = WAVE_DISTORTION_FRAGMENT_SHADER
    
    companion object {
        @Language("GLSL")
        private const val WAVE_DISTORTION_VERTEX_SHADER = """
            attribute vec4 position;
            attribute vec4 inputTextureCoordinate;
            attribute vec2 waveParams;      // x: amplitude, y: frequency
            attribute float timeOffset;      // per-vertex time offset
            
            varying vec2 textureCoordinate;
            varying vec2 vWaveParams;
            varying float vTimeOffset;
            
            uniform float time;              // global time for animation
            uniform mat4 transformMatrix;
            
            void main() {
                // Pass wave parameters to fragment shader
                vWaveParams = waveParams;
                vTimeOffset = timeOffset;
                
                // Apply wave distortion to position
                vec4 pos = position;
                float wave = sin((pos.x + time + timeOffset) * waveParams.y) * waveParams.x;
                pos.y += wave;
                
                gl_Position = pos;
                textureCoordinate = (transformMatrix * inputTextureCoordinate).xy;
            }
        """
        
        @Language("GLSL")
        private const val WAVE_DISTORTION_FRAGMENT_SHADER = """
            precision highp float;
            
            uniform sampler2D inputImageTexture;
            
            varying vec2 textureCoordinate;
            varying vec2 vWaveParams;
            varying float vTimeOffset;
            
            void main() {
                // Sample the texture with potential additional distortion based on wave params
                vec2 uv = textureCoordinate;
                
                // Optional: Add subtle color shift based on wave parameters
                vec4 color = texture2D(inputImageTexture, uv);
                
                // Tint based on wave amplitude
                float tintStrength = vWaveParams.x * 0.1;
                color.rgb = mix(color.rgb, vec3(0.5, 0.7, 1.0), tintStrength);
                
                gl_FragColor = color;
            }
        """
    }
}

/**
 * Example shader that uses multiple vertex attributes for particle effects.
 * Each vertex can represent a particle with its own properties.
 * 
 * Vertex attributes:
 * - position: vec4 (particle position)
 * - inputTextureCoordinate: vec4 (texture coordinate)
 * - particleColor: vec4 (per-particle color)
 * - particleSize: float (per-particle size)
 * - particleRotation: float (per-particle rotation angle)
 */
class ParticleShader : MultiAttributeKraftShader() {
    
    override fun loadVertexShader(): String = PARTICLE_VERTEX_SHADER
    
    override fun loadFragmentShader(): String = PARTICLE_FRAGMENT_SHADER
    
    companion object {
        @Language("GLSL")
        private const val PARTICLE_VERTEX_SHADER = """
            attribute vec4 position;
            attribute vec4 inputTextureCoordinate;
            attribute vec4 particleColor;
            attribute float particleSize;
            attribute float particleRotation;
            
            varying vec2 textureCoordinate;
            varying vec4 vParticleColor;
            
            uniform mat4 transformMatrix;
            uniform vec2 resolution;
            
            mat2 rotate2d(float angle) {
                float c = cos(angle);
                float s = sin(angle);
                return mat2(c, -s, s, c);
            }
            
            void main() {
                // Apply particle size
                vec4 pos = position;
                pos.xy *= particleSize;
                
                // Apply rotation
                pos.xy = rotate2d(particleRotation) * pos.xy;
                
                gl_Position = pos;
                gl_PointSize = particleSize * min(resolution.x, resolution.y) * 0.1;
                
                textureCoordinate = (transformMatrix * inputTextureCoordinate).xy;
                vParticleColor = particleColor;
            }
        """
        
        @Language("GLSL")
        private const val PARTICLE_FRAGMENT_SHADER = """
            precision highp float;
            
            uniform sampler2D inputImageTexture;
            
            varying vec2 textureCoordinate;
            varying vec4 vParticleColor;
            
            void main() {
                vec4 texColor = texture2D(inputImageTexture, textureCoordinate);
                
                // Multiply texture color with particle color
                gl_FragColor = texColor * vParticleColor;
            }
        """
    }
}