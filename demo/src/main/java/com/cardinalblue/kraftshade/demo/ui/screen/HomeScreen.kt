package com.cardinalblue.kraftshade.demo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

@Composable
fun ColumnScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
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
