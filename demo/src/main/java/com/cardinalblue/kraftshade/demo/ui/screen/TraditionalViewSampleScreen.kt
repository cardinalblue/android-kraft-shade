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

@Composable
fun TraditionalViewSampleScreen() {
    val uiComponents = remember {
        buildList {
            add(SampleScreenUiComponent.HeaderComponent(
                title = "Traditional View Samples",
                description = "Explore Kraftshade effects using traditional Android Views"
            ))
            val traditionalSamples = Destination.entries.filter { it.sampleType == SampleType.TraditionalView }
            val categorizedSamples = traditionalSamples.groupBy { it.category }
            Category.entries.forEach { category ->
                val samplesInCategory = categorizedSamples[category]
                if (samplesInCategory.isNullOrEmpty()) return@forEach
                add(SampleScreenUiComponent.CategoryTitle(category))
                samplesInCategory.forEach { sample ->
                    add(SampleScreenUiComponent.SampleComponent(sample))
                }
            }
        }
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