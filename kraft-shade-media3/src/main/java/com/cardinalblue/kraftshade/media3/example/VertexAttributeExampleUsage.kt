//package com.cardinalblue.kraftshade.media3.example
//
//import android.content.Context
//import androidx.annotation.OptIn
//import androidx.compose.runtime.Composable
//import androidx.media3.common.util.UnstableApi
//import com.cardinalblue.kraftshade.OpenGlUtils
//import com.cardinalblue.kraftshade.OpenGlUtils.asFloatBuffer
//import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
//import com.cardinalblue.kraftshade.media3.KraftShadePipelineEffectWithAttributes
//import com.cardinalblue.kraftshade.media3.VideoEffectExecutionProvider
//import com.cardinalblue.kraftshade.pipeline.BufferReference
//import com.cardinalblue.kraftshade.pipeline.PipelineModifierWithoutInputTexture
//import com.cardinalblue.kraftshade.shader.VertexAttributeData
//import com.cardinalblue.kraftshade.shader.builtin.NormalBlendKraftShader
//import com.cardinalblue.kraftshade.shader.example.WaveDistortionShader
//import kotlin.math.sin
//
///**
// * Example usage of KraftShadePipelineEffectWithAttributes with SaveResultButtonForAnimation.
// * This demonstrates how to create a video effect with custom vertex attributes that change over time.
// */
//object VertexAttributeExampleUsage {
//
//    /**
//     * Creates a wave distortion effect that animates over time using custom vertex attributes.
//     *
//     * @param context The Android context
//     * @return A configured KraftShadePipelineEffectWithAttributes
//     */
//    @OptIn(UnstableApi::class)
//    fun createWaveDistortionEffect(context: Context): KraftShadePipelineEffectWithAttributes {
//        // Create the wave distortion shader
//        val waveShader = WaveDistortionShader()
//
//        // Define the vertex attribute provider that creates attributes based on presentation time
//        val vertexAttributeProvider: (Long) -> List<VertexAttributeData> = { presentationTimeUs ->
//            val time = presentationTimeUs / 1_000_000f // Convert to seconds
//
//            // Create wave parameters that vary over time
//            // 4 vertices for a quad, each with different wave parameters
//            val waveParamsData = floatArrayOf(
//                // Vertex 0: amplitude, frequency
//                0.1f * sin(time * 0.5f), 5.0f,
//                // Vertex 1
//                0.15f * sin(time * 0.7f), 4.0f,
//                // Vertex 2
//                0.12f * sin(time * 0.6f), 6.0f,
//                // Vertex 3
//                0.08f * sin(time * 0.8f), 5.5f
//            )
//
//            // Time offset for each vertex to create more complex animation
//            val timeOffsetData = floatArrayOf(
//                0.0f,
//                0.5f,
//                1.0f,
//                1.5f
//            )
//
//            listOf(
//                VertexAttributeData(
//                    name = "waveParams",
//                    size = 2,
//                    buffer = waveParamsData.asFloatBuffer()
//                ),
//                VertexAttributeData(
//                    name = "timeOffset",
//                    size = 1,
//                    buffer = timeOffsetData.asFloatBuffer()
//                )
//            )
//        }
//
//        // Create the video effect execution provider
//        val provider = VideoEffectExecutionProvider { buffer, timeInput, videoTexture ->
//            pipeline(buffer) {
//                // Apply the wave distortion shader
//                step(waveShader, buffer) { shader ->
//                    shader.setInputTexture(videoTexture)
//                    // Set the time uniform for animation
//                    shader.runOnDraw {
//                        shader.setUniforms(mapOf("time" to timeInput.get()))
//                    }
//                }
//            }
//        }
//
//        return KraftShadePipelineEffectWithAttributes(
//            context = context,
//            provider = provider,
//            vertexAttributeProvider = vertexAttributeProvider
//        )
//    }
//
//    /**
//     * Example of how to use with SaveResultButtonForAnimation in a Composable.
//     * This would be used in a screen similar to MovingBokehScreen.
//     */
//    @Composable
//    fun ExampleUsageInComposable(
//        context: Context,
//        sourceImageUri: android.net.Uri?,
//        imageSize: com.cardinalblue.kraftshade.model.GlSize?
//    ) {
//        // This is a conceptual example showing how to integrate with SaveResultButtonForAnimation
////        SaveResultButtonForAnimation(
////            fileNamePrefix = "waveDistortion",
////            imageSizeProvider = { imageSize },
////            durationMilliseconds = 5000, // 5 second animation
////            sourceImageUri = sourceImageUri,
////            createPipelineModifierFunction = { timeInput ->
////                // Create a pipeline modifier that uses the wave distortion effect
////                object : PipelineModifierWithoutInputTexture {
////                    override fun GraphPipelineSetupScope.addStep(targetBuffer: BufferReference) {
////                        val waveShader = WaveDistortionShader()
////
////                        step(waveShader, targetBuffer) { shader ->
////                            // The vertex attributes are automatically provided by the effect
////                            shader.setUniforms(mapOf("time" to timeInput.value))
////                        }
////                    }
////                }
////            },
////            lastStepBlendShader = NormalBlendKraftShader() // Or any blending shader
////        )
//
//    }
//
//    /**
//     * Creates a more complex example with particle effects using multiple vertex attributes.
//     */
//    @OptIn(UnstableApi::class)
//    fun createParticleEffect(
//        context: Context,
//        particleCount: Int = 100
//    ): KraftShadePipelineEffectWithAttributes {
//        // Pre-generate random particle data
//        val particlePositions = FloatArray(particleCount * 4) // x, y, z, w for each particle
//        val particleColors = FloatArray(particleCount * 4)    // r, g, b, a for each particle
//        val particleSizes = FloatArray(particleCount)
//        val particleRotations = FloatArray(particleCount)
//
//        // Initialize random particle data
//        for (i in 0 until particleCount) {
//            // Random positions
//            particlePositions[i * 4] = (Math.random() * 2.0 - 1.0).toFloat()     // x: -1 to 1
//            particlePositions[i * 4 + 1] = (Math.random() * 2.0 - 1.0).toFloat() // y: -1 to 1
//            particlePositions[i * 4 + 2] = 0f                                     // z
//            particlePositions[i * 4 + 3] = 1f                                     // w
//
//            // Random colors
//            particleColors[i * 4] = Math.random().toFloat()     // r
//            particleColors[i * 4 + 1] = Math.random().toFloat() // g
//            particleColors[i * 4 + 2] = Math.random().toFloat() // b
//            particleColors[i * 4 + 3] = 1f                       // a
//
//            // Random sizes and rotations
//            particleSizes[i] = (Math.random() * 0.5 + 0.5).toFloat() // 0.5 to 1.0
//            particleRotations[i] = (Math.random() * Math.PI * 2).toFloat()
//        }
//
//        val vertexAttributeProvider: (Long) -> List<VertexAttributeData> = { presentationTimeUs ->
//            val time = presentationTimeUs / 1_000_000f
//
//            // Update particle rotations based on time
//            val animatedRotations = FloatArray(particleCount) { i ->
//                particleRotations[i] + time * 0.5f
//            }
//
//            listOf(
//                VertexAttributeData(
//                    name = "position",
//                    size = 4,
//                    buffer = particlePositions.asFloatBuffer()
//                ),
//                VertexAttributeData(
//                    name = "particleColor",
//                    size = 4,
//                    buffer = particleColors.asFloatBuffer()
//                ),
//                VertexAttributeData(
//                    name = "particleSize",
//                    size = 1,
//                    buffer = particleSizes.asFloatBuffer()
//                ),
//                VertexAttributeData(
//                    name = "particleRotation",
//                    size = 1,
//                    buffer = animatedRotations.asFloatBuffer()
//                )
//            )
//        }
//
//        val provider = VideoEffectExecutionProvider { buffer, timeInput, videoTexture ->
//            pipeline(buffer) {
//                val particleShader = com.cardinalblue.kraftshade.shader.example.ParticleShader()
//
//                step(particleShader, buffer) { shader ->
//                    shader.setInputTexture(videoTexture)
//                }
//            }
//        }
//
//        return KraftShadePipelineEffectWithAttributes(
//            context = context,
//            provider = provider,
//            vertexAttributeProvider = vertexAttributeProvider
//        )
//    }
//}