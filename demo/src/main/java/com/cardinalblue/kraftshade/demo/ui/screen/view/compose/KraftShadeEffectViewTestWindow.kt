package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.components.CollapsibleSection
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
    var monochrome by remember { mutableFloatStateOf(0f) }
    var colorMatrixIntensity by remember { mutableFloatStateOf(0f) }
    var directionalSobelMixture by remember { mutableFloatStateOf(0f) }
    var laplacianMixture by remember { mutableFloatStateOf(0f) }
    var laplacianMagnitudeMixture by remember { mutableFloatStateOf(0f) }
    var whiteBalanceTemperature by remember { mutableFloatStateOf(5000f) }
    var whiteBalanceTint by remember { mutableFloatStateOf(0f) }
    var shadows by remember { mutableFloatStateOf(0f) }
    var highlights by remember { mutableFloatStateOf(1f) }
    var redMultiplier by remember { mutableFloatStateOf(1f) }
    var greenMultiplier by remember { mutableFloatStateOf(1f) }
    var blueMultiplier by remember { mutableFloatStateOf(1f) }
    var shadowsR by remember { mutableFloatStateOf(0f) }
    var shadowsG by remember { mutableFloatStateOf(0f) }
    var shadowsB by remember { mutableFloatStateOf(0f) }
    var midtonesR by remember { mutableFloatStateOf(0f) }
    var midtonesG by remember { mutableFloatStateOf(0f) }
    var midtonesB by remember { mutableFloatStateOf(0f) }
    var highlightsR by remember { mutableFloatStateOf(0f) }
    var highlightsG by remember { mutableFloatStateOf(0f) }
    var highlightsB by remember { mutableFloatStateOf(0f) }
    var preserveLuminosity by remember { mutableStateOf(true) }

    var colorAdjustmentExpanded by remember { mutableStateOf(true) }
    var rgbControlsExpanded by remember { mutableStateOf(true) }
    var colorBalanceExpanded by remember { mutableStateOf(true) }
    var effectsExpanded by remember { mutableStateOf(true) }

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
            CollapsibleSection(
                title = "Color Adjustment",
                expanded = colorAdjustmentExpanded,
                onExpandedChange = { colorAdjustmentExpanded = it }
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
                ParameterSlider(
                    label = "Brightness",
                    value = brightness,
                    onValueChange = { 
                        brightness = it
                        state.requestRender()
                    },
                    valueRange = -1f..1f
                )
                ParameterSlider(
                    label = "Contrast",
                    value = contrast,
                    onValueChange = { 
                        contrast = it
                        state.requestRender()
                    },
                    valueRange = 0f..2f
                )
                ParameterSlider(
                    label = "Gamma",
                    value = gamma,
                    onValueChange = { 
                        gamma = it
                        state.requestRender()
                    },
                    valueRange = 0.5f..2f
                )
                ParameterSlider(
                    label = "Monochrome",
                    value = monochrome,
                    onValueChange = {
                        monochrome = it
                        state.requestRender()
                    },
                    valueRange = 0f..1f
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            CollapsibleSection(
                title = "RGB Controls",
                expanded = rgbControlsExpanded,
                onExpandedChange = { rgbControlsExpanded = it }
            ) {
                ParameterSlider(
                    label = "Red Multiplier",
                    value = redMultiplier,
                    onValueChange = { 
                        redMultiplier = it
                        state.requestRender()
                    },
                    valueRange = 0f..2f
                )
                ParameterSlider(
                    label = "Green Multiplier",
                    value = greenMultiplier,
                    onValueChange = { 
                        greenMultiplier = it
                        state.requestRender()
                    },
                    valueRange = 0f..2f
                )
                ParameterSlider(
                    label = "Blue Multiplier",
                    value = blueMultiplier,
                    onValueChange = { 
                        blueMultiplier = it
                        state.requestRender()
                    },
                    valueRange = 0f..2f
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            CollapsibleSection(
                title = "Color Balance",
                expanded = colorBalanceExpanded,
                onExpandedChange = { colorBalanceExpanded = it }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Preserve Luminosity")
                    Switch(
                        checked = preserveLuminosity,
                        onCheckedChange = {
                            preserveLuminosity = it
                            state.requestRender()
                        }
                    )
                }

                Text("Shadows")
                ParameterSlider(
                    label = "R",
                    value = shadowsR,
                    onValueChange = { 
                        shadowsR = it
                        state.requestRender()
                    },
                    valueRange = -1f..1f
                )
                ParameterSlider(
                    label = "G",
                    value = shadowsG,
                    onValueChange = { 
                        shadowsG = it
                        state.requestRender()
                    },
                    valueRange = -1f..1f
                )
                ParameterSlider(
                    label = "B",
                    value = shadowsB,
                    onValueChange = { 
                        shadowsB = it
                        state.requestRender()
                    },
                    valueRange = -1f..1f
                )

                Text("Midtones")
                ParameterSlider(
                    label = "R",
                    value = midtonesR,
                    onValueChange = { 
                        midtonesR = it
                        state.requestRender()
                    },
                    valueRange = -1f..1f
                )
                ParameterSlider(
                    label = "G",
                    value = midtonesG,
                    onValueChange = { 
                        midtonesG = it
                        state.requestRender()
                    },
                    valueRange = -1f..1f
                )
                ParameterSlider(
                    label = "B",
                    value = midtonesB,
                    onValueChange = { 
                        midtonesB = it
                        state.requestRender()
                    },
                    valueRange = -1f..1f
                )

                Text("Highlights")
                ParameterSlider(
                    label = "R",
                    value = highlightsR,
                    onValueChange = { 
                        highlightsR = it
                        state.requestRender()
                    },
                    valueRange = -1f..1f
                )
                ParameterSlider(
                    label = "G",
                    value = highlightsG,
                    onValueChange = { 
                        highlightsG = it
                        state.requestRender()
                    },
                    valueRange = -1f..1f
                )
                ParameterSlider(
                    label = "B",
                    value = highlightsB,
                    onValueChange = { 
                        highlightsB = it
                        state.requestRender()
                    },
                    valueRange = -1f..1f
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            CollapsibleSection(
                title = "Effects",
                expanded = effectsExpanded,
                onExpandedChange = { effectsExpanded = it }
            ) {
                ParameterSlider(
                    label = "Pixel Size",
                    value = pixelSize,
                    onValueChange = { 
                        pixelSize = it
                        state.requestRender()
                    },
                    valueRange = 1f..20f
                )
                ParameterSlider(
                    label = "Directional Sobel Mixture",
                    value = directionalSobelMixture,
                    onValueChange = { 
                        directionalSobelMixture = it
                        state.requestRender()
                    },
                    valueRange = 0f..1f
                )
                ParameterSlider(
                    label = "Laplacian Mixture",
                    value = laplacianMixture,
                    onValueChange = { 
                        laplacianMixture = it
                        state.requestRender()
                    },
                    valueRange = 0f..1f
                )
                ParameterSlider(
                    label = "Laplacian Magnitude Mixture",
                    value = laplacianMagnitudeMixture,
                    onValueChange = { 
                        laplacianMagnitudeMixture = it
                        state.requestRender()
                    },
                    valueRange = 0f..1f
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        state.setEffect { windowSurface ->
            val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
            aspectRatio = bitmap.width.toFloat() / bitmap.height

            pipeline(windowSurface) {
                serialSteps(
                    inputTexture = bitmap.asTexture(),
                    targetBuffer = windowSurface,
                ) {
                    step(SaturationKraftShader()) { shader ->
                        shader.saturation = saturation
                    }

                    step(HueKraftShader()) { shader ->
                        shader.setHueInDegree(hue)
                    }

                    step(BrightnessKraftShader()) { shader ->
                        shader.brightness = brightness
                    }

                    step(ContrastKraftShader()) { shader ->
                        shader.contrast = contrast
                    }

                    step(PixelationKraftShader()) { shader ->
                        shader.pixel = pixelSize
                    }

                    step(GammaKraftShader()) { shader ->
                        shader.gamma = gamma
                    }

                    step(MonochromeKraftShader()) { shader ->
                        shader.intensity = monochrome
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
                        )
                    ) { shader ->
                        shader.intensity = colorMatrixIntensity
                    }

                    stepWithMixture(
                        DirectionalSobelEdgeDetectionKraftShader(),
                        sampledInput { directionalSobelMixture }
                    )

                    stepWithMixture(
                        LaplacianKraftShader(),
                        sampledInput { laplacianMixture }
                    )

                    stepWithMixture(
                        LaplacianMagnitudeKraftShader(),
                        sampledInput { laplacianMagnitudeMixture }
                    )

                    step(WhiteBalanceKraftShader()) { shader ->
                        shader.temperature = whiteBalanceTemperature
                        shader.tint = whiteBalanceTint
                    }

                    step(HighlightShadowKraftShader()) { shader ->
                        shader.shadows = shadows
                        shader.highlights = highlights
                    }

                    step(RGBKraftShader()) { shader ->
                        shader.red = redMultiplier
                        shader.green = greenMultiplier
                        shader.blue = blueMultiplier
                    }

                    step(ColorBalanceKraftShader()) { shader ->
                        shader.shadows = floatArrayOf(shadowsR, shadowsG, shadowsB)
                        shader.midtones = floatArrayOf(midtonesR, midtonesG, midtonesB)
                        shader.highlights = floatArrayOf(highlightsR, highlightsG, highlightsB)
                        shader.preserveLuminosity = preserveLuminosity
                    }
                }
            }
        }
    }
}
