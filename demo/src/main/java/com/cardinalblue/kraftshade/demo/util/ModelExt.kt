package com.cardinalblue.kraftshade.demo.util

import com.cardinalblue.kraftshade.model.GlColor

fun GlColor.asComposeColor(): androidx.compose.ui.graphics.Color {
    return androidx.compose.ui.graphics.Color(
        red = rFloat,
        green = gFloat,
        blue = bFloat,
        alpha = aFloat
    )
}