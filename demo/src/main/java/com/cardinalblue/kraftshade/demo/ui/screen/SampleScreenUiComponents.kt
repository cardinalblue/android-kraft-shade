package com.cardinalblue.kraftshade.demo.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun Title(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
    )
}

sealed class SampleScreenUiComponent(val spanCount: Int) {
    @Composable
    abstract fun UserInterface()

    class HeaderComponent(
        val title: String,
        val description: String,
    ) : SampleScreenUiComponent(2) {
        @Composable
        override fun UserInterface() {
            Column {
                Title(title)
                Text(
                    text = description,
                    color = Color.Gray,
                    fontSize = 14.sp,
                )
            }
        }
    }

    class CategoryTitle(
        private val category: Category
    ) : SampleScreenUiComponent(2) {
        @Composable
        override fun UserInterface() {
            Title(category.displayName)
        }
    }

    class SampleComponent(
        private val sample: Destination
    ) : SampleScreenUiComponent(1) {
        @Composable
        override fun UserInterface() {
            GridOptionButton(sample)
        }
    }
}