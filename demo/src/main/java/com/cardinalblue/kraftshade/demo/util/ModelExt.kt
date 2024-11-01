package com.cardinalblue.kraftshade.demo.util

import com.cardinalblue.kraftshade.model.Color

fun Color.asComposeColor(): androidx.compose.ui.graphics.Color {
    return androidx.compose.ui.graphics.Color(
        red = rFloat,
        green = gFloat,
        blue = bFloat,
        alpha = aFloat
    )
}