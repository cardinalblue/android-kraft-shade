package com.cardinalblue.kraftshade.demo.ui.screen.view.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ParameterSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text("$label (%.2f)".format(value))

        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}
