package com.cardinalblue.kraftshade.demo.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    ColumnScreen(
        modifier = Modifier.padding(16.dp)
    ) {
        GridOptionButton(Destination.ComposableSamples)
        GridOptionButton(Destination.TraditionalViewSamples)
    }
}

enum class Category(
    val displayName: String
) {
    BASIC("Basic Shaders"),
    EFFECTS("Effects"),
    COLOR("Color & Levels"),
    BLUR("Blur & Morphology"),
    OTHER("Other")
}
