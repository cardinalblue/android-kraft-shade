package com.cardinalblue.kraftshade.compose

import com.cardinalblue.kraftshade.model.GlColor

fun GlColor.asComposeColor(): androidx.compose.ui.graphics.Color {
    return androidx.compose.ui.graphics.Color(
        red = r,
        green = g,
        blue = b,
        alpha = a,
    )
}

fun androidx.compose.ui.graphics.Color.asGlColor(): GlColor {
    return GlColor(
        r = red,
        g = green,
        b = blue,
        a = alpha,
    )
}
