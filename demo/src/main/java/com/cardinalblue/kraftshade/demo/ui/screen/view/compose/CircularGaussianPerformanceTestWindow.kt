package com.cardinalblue.kraftshade.demo.ui.screen.view.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardinalblue.kraftshade.compose.KraftShadeEffectView
import com.cardinalblue.kraftshade.compose.rememberKraftShadeEffectState
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.pipeline.asEffectExecution
import com.cardinalblue.kraftshade.pipeline.input.sampledInput
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.CircularGaussianBlurKraftShader
import com.cardinalblue.kraftshade.util.DangerousKraftShadeApi
import com.cardinalblue.kraftshade.util.KraftLogger
import kotlin.time.Duration
import kotlin.time.measureTime

@Composable
fun CircularGaussianPerformanceTestWindow() {
    val logger = remember {
        KraftLogger("CGPerformance")
    }
    val state = rememberKraftShadeEffectState()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var finalResults: String? by remember { mutableStateOf(null) }

    var textCurrentRepeat by remember { mutableFloatStateOf(0f) }
    var textCurrentAmount by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // Load the image
        val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
        aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        val results = mutableListOf<String>()
        val repeats = listOf(20f, 30f, 60f)
        val amounts = listOf(0.2f, 0.3f, 0.6f, 1.0f)

        var currentRepeat: Float = repeats[0]
        var currentAmount: Float = amounts[0]

        suspend fun runPerformanceTest() {
            // Run the performance test
            repeats.forEach { repeat ->
                currentRepeat = repeat
                textCurrentRepeat = repeat
                amounts.forEach { amount ->
                    currentAmount = amount
                    textCurrentAmount = amount

                    var totalTime: Duration = Duration.ZERO
                    for (i in 1..5) {
                        val time = measureTime {
                            @OptIn(DangerousKraftShadeApi::class)
                            state.renderBlocking()
                        }
                        totalTime += time
                    }
                    val avgTime = totalTime / 5.0
                    val timeString = "%.2fms".format(avgTime.inWholeMicroseconds / 1000f)
                    val result = "[r: $repeat, a: $amount] used $timeString"
                    results.add(result)
                }
            }
            finalResults = "Performance test done\n${results.joinToString("\n")}"
                .also(logger::d)
        }

        state.setEffect(
            afterSet = { runPerformanceTest() }
        ) { windowSurface ->
            CircularGaussianBlurKraftShader()
                .apply {
                    setInputTexture(bitmap.asTexture())
                }
                .asEffectExecution(
                    sampledInput { currentRepeat },
                    sampledInput { currentAmount },
                    targetBuffer = windowSurface,
                ) { (repeatInput, amountInput) ->
                    repeat = repeatInput.getCasted()
                    amount = amountInput.getCasted()
                }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        KraftShadeEffectView(
            modifier = Modifier
                .aspectRatio(aspectRatio),
            state = state
        )

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            if (finalResults != null) {
                Text(
                    text = finalResults!!,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    text = "Running performance tests:\nrepeat = $textCurrentRepeat\namount = $textCurrentAmount",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
