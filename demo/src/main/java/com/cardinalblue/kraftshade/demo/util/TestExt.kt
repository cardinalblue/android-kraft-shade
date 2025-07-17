package com.cardinalblue.kraftshade.demo.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.measureTime

inline fun measureRepeat(repeat: Int, action: () -> Unit): Duration {
    return measureTime {
        repeat(repeat) {
            action()
        }
    }
}

inline fun logMeasureRepeat(
    actionName: String,
    repeat: Int,
    action: () -> Unit,
) {
    val duration = measureRepeat(repeat, action)
    Log.d("Perf-Measure", "[$repeat] times of [$actionName] took ${duration.toHumanReadable()}")
}

fun Duration.toHumanReadable(): String {
    return when {
        inWholeMicroseconds < 10 -> {
            "${inWholeNanoseconds}ns"
        }
        inWholeMilliseconds < 10 -> {
            "${inWholeMicroseconds}μs"
        }
        inWholeSeconds < 10 -> {
            "${inWholeMilliseconds.toInt()}ms"
        }
        else -> {
            "${inWholeSeconds.toInt()}s"
        }
    }
}

suspend fun Context.loadBitmapFromAsset(
    assetPath: String
): Bitmap = withContext(Dispatchers.IO) {
    assets.open(assetPath).use { stream ->
        BitmapFactory.decodeStream(stream)
    }
}

val Bitmap.aspectRatio: Float
    get() = width.toFloat() / height
