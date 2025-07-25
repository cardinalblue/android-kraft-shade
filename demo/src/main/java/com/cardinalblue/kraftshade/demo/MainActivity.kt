package com.cardinalblue.kraftshade.demo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cardinalblue.kraftshade.demo.ui.screen.Destination
import com.cardinalblue.kraftshade.demo.ui.theme.KraftShadeTheme
import com.cardinalblue.kraftshade.demo.util.LocalNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KraftShadeTheme(darkTheme = true) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }

    @Composable
    private fun AppNavigation() {
        val navController = rememberNavController()
        CompositionLocalProvider(LocalNavController provides navController) {
            NavHost(navController = navController, startDestination = Destination.Home.route) {
                for (destination in Destination.entries) {
                    composable(destination)
                }
            }
        }
    }
}

private fun NavGraphBuilder.composable(
    destination: Destination,
) {
    composable(destination.route, content = { destination.screen() })
}
