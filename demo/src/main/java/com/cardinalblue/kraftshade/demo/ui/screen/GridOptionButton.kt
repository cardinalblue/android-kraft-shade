package com.cardinalblue.kraftshade.demo.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardinalblue.kraftshade.demo.util.LocalNavController

@Composable
fun GridOptionButton(
    destination: Destination,
    modifier: Modifier = Modifier,
) {
    val navController = LocalNavController.current
    Card(
        onClick = { navController.navigate(destination.route) },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = destination.title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
