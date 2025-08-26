package com.cardinalblue.kraftshade.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ComposableSampleScreen() {
    val uiComponents = remember {
        buildList {
            add(SampleScreenUiComponent.HeaderComponent(
                title = "Compose Samples",
                description = "Explore various KraftShade effects and shaders"
            ))
            val composeSamples = Destination.entries.filter { it.sampleType == SampleType.Compose }
            val categorizedSamples = composeSamples.groupBy { it.category }
            Category.entries.forEach { category ->
                val samples = categorizedSamples[category]
                if (samples.isNullOrEmpty()) return@forEach
                add(SampleScreenUiComponent.CategoryTitle(category))
                addAll(samples.map { SampleScreenUiComponent.SampleComponent(it) })
            }
        }.toImmutableList()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        items(
            items = uiComponents,
            span = { GridItemSpan(it.spanCount) }
        ) { component -> component.UserInterface() }
    }
}

