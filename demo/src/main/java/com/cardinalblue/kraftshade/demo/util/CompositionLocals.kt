package com.cardinalblue.kraftshade.demo.util

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController

val LocalNavController = compositionLocalOf<NavHostController> {
    error("No LocalNavigator provided")
}
