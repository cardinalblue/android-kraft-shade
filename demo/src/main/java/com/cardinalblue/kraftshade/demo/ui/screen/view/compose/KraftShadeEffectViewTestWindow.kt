package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.components.ParameterSlider
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.model.GlMat4
import com.cardinalblue.kraftshade.pipeline.input.sampledInput
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.*

@Composable
fun KraftShadeEffectViewTestWindow() {
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1.2f) }
    var pixelSize by remember { mutableFloatStateOf(1f) }
    var hue by remember { mutableFloatStateOf(0f) }
    var gamma by remember { mutableFloatStateOf(1.2f) }
    var colorMatrixIntensity by remember { mutableFloatStateOf(0f) }
    var directionalSobelMixture by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            KraftShadeEffectView(
                modifier = Modifier
                    .aspectRatio(aspectRatio),
                state = state
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ParameterSlider(
                label = "Saturation",
                value = saturation,
                onValueChange = { 
                    saturation = it
                    state.requestRender()
                },
                valueRange = 0f..2f
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSlider(
                label = "Brightness",
                value = brightness,
                onValueChange = { 
                    brightness = it
                    state.requestRender()
                },
                valueRange = -1f..1f
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSlider(
                label = "Contrast",
                value = contrast,
                onValueChange = { 
                    contrast = it
                    state.requestRender()
                },
                valueRange = 0f..4f
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSlider(
                label = "Pixel Size",
                value = pixelSize,
                onValueChange = { 
                    pixelSize = it
                    state.requestRender()
                },
                valueRange = 1f..100f
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSlider(
                label = "Hue",
                value = hue,
                onValueChange = { 
                    hue = it
                    state.requestRender()
                },
                valueRange = 0f..360f
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSlider(
                label = "Gamma",
                value = gamma,
                onValueChange = { 
                    gamma = it
                    state.requestRender()
                },
                valueRange = 0f..3f
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSlider(
                label = "Color Inversion",
                value = colorMatrixIntensity,
                onValueChange = { 
                    colorMatrixIntensity = it
                    state.requestRender()
                },
                valueRange = 0f..1f
            )

            Spacer(modifier = Modifier.height(8.dp))

            ParameterSlider(
                label = "Directional Sobel Mixture",
                value = directionalSobelMixture,
                onValueChange = {
                    directionalSobelMixture = it
                    state.requestRender()
                },
                valueRange = 0f..1f
            )
        }
    }

    LaunchedEffect(Unit) {
        state.setEffect { windowSurface ->
            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.width.toFloat() / bitmap.height

            pipeline(windowSurface.size) {
                serialSteps(
                    inputTexture = bitmap.asTexture(),
                    targetBuffer = windowSurface,
                ) {
                    step(
                        SaturationKraftShader(),
                        sampledInput { saturation }
                    ) { (saturation) ->
                        this.saturation = saturation.getCasted()
                    }

                    step(
                        HueKraftShader(),
                        sampledInput { hue }
                    ) { (hue) ->
                        this.setHueInDegree(hue.getCasted())
                    }

                    step(
                        BrightnessKraftShader(),
                        sampledInput { brightness }
                    ) { (brightness) ->
                        this.brightness = brightness.getCasted()
                    }

                    step(
                        ContrastKraftShader(),
                        sampledInput { contrast }
                    ) { (contrast) ->
                        this.contrast = contrast.getCasted()
                    }

                    step(
                        PixelationKraftShader(),
                        sampledInput { pixelSize }
                    ) { (pixelSize) ->
                        this.pixel = pixelSize.getCasted()
                    }

                    step(
                        GammaKraftShader(),
                        sampledInput { gamma }
                    ) { (gamma) ->
                        this.gamma = gamma.getCasted()
                    }

                    step(
                        ColorMatrixKraftShader(
                            colorMatrix = GlMat4(
                                -1f, 0f, 0f, 0f,
                                0f, -1f, 0f, 0f,
                                0f, 0f, -1f, 0f,
                                1f, 1f, 1f, 1f
                            ),
                            colorOffset = floatArrayOf(1f, 1f, 1f, 0f),
                        ),
                        sampledInput { colorMatrixIntensity }
                    ) { (intensity) ->
                        this.intensity = intensity.getCasted()
                    }

                    stepWithMixture(
                        DirectionalSobelEdgeDetectionKraftShader(),
                        sampledInput { directionalSobelMixture }
                    )
                }
            }
        }
    }
}
