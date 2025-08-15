package com.cardinalblue.kraftshade.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ComposableSampleScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CategoryTitle("Compose Samples")
        Text(
            text = "Explore various Kraftshade effects and shaders",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val composeSamples = Destination.entries.filter { it.sampleType == SampleType.Compose }
        val categorizedSamples = composeSamples.groupBy { it.category }

        Category.entries.forEach { category ->
            val samplesInCategory = categorizedSamples[category] ?: emptyList()
            if (samplesInCategory.isNotEmpty()) {
                CategoryTitle(category.displayName)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .height(((samplesInCategory.size / 2 + samplesInCategory.size % 2) * 80).dp)
                        .padding(bottom = 16.dp)
                ) {
                    items(samplesInCategory) { sample ->
                        GridOptionButton(sample)
                    }
                }
            }
        }
    }
}
