package com.cardinalblue.kraftshade.demo.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TraditionalViewSampleScreen() {
    ColumnScreen {
        Text(
            text = "Traditional View Samples",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "Coming soon...",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}