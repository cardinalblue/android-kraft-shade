package com.cardinalblue.kraftshade.demo.ui.screen.view.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                2.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(8.dp),
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onExpandedChange(!expanded) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title)
                IconButton(
                    onClick = { onExpandedChange(!expanded) },
                    modifier = Modifier.rotate(
                        animateFloatAsState(if (expanded) 180f else 0f).value
                    )
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = if (expanded) "Collapse" else "Expand")
                }
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}
