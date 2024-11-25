package com.cardinalblue.kraftshade.demo.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cardinalblue.kraftshade.demo.ui.screen.basic_env.BasicShaderScreen
import com.cardinalblue.kraftshade.demo.ui.screen.basic_env.BlendingExampleScreen
import com.cardinalblue.kraftshade.demo.ui.screen.dsl.KraftBitmapTestScreen
import com.cardinalblue.kraftshade.demo.ui.screen.shaders.*
import com.cardinalblue.kraftshade.demo.ui.screen.view.TransparencyTestWindow
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.ColorfulCrosshatchTestScreen
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.CrosshatchTestScreen
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.KraftShadeAnimatedViewTestWindow
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.KraftShadeEffectViewTestWindow
import com.cardinalblue.kraftshade.demo.util.LocalNavController

@Composable
fun HomeScreen() {
    ColumnScreen {
        Destination.entries
            .filter { it.showInHomeScreen }
            .forEach {
                key(it) {
                    OptionButton(it)
                }
            }
    }
}

@Composable
private fun CategoryTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(8.dp)
    )
}

@Composable
private fun OptionButton(
    destination: Destination,
) {
    val navController = LocalNavController.current
    Button(onClick = { navController.navigate(destination) }) {
        Text(text = destination.title)
    }
}

enum class Destination(
    val route: String,
    val title: String,
    val showInHomeScreen: Boolean = true,
    val screen: @Composable () -> Unit,
) {
    Home("home", "Home", showInHomeScreen = false, screen = { HomeScreen() }),
    BasicShader("basic_shader", "Basic Shader", screen = { BasicShaderScreen() }),
    BlendingExample("blending_example", "Blending Example", screen = { BlendingExampleScreen() }),
    SaturationShader("saturation_shader", "Saturation Shader", screen = { SaturationShaderScreen() }),
    SimpleMixShader("simple_mix_blend_shader", "Simple Mix Blend Shader", screen = { SimpleMixBlendShaderScreen() }),
    EmbossShader("emboss_shader", "Emboss Shader", screen = { EmbossShaderScreen() }),
    LookUpTableShader("look_up_table_shader", "Look Up Table Shader", screen = { LookUpTableShaderTestScreen() }),
    TransparencyTest("transparency_test", "Transparency Test", screen = { TransparencyTestWindow() }),
    KraftShadeAnimatedView("compose_animated", "Compose (animated)", screen = { KraftShadeAnimatedViewTestWindow() }),
    KraftShadeEffectView("compose_effect", "Compose (effect)", screen = { KraftShadeEffectViewTestWindow() }),
    CrosshatchShader("crosshatch_shader", "Crosshatch Shader", screen = { CrosshatchTestScreen() }),
    ColorfulCrosshatchShader("colorful_crosshatch_shader", "Colorful Crosshatch Shader", screen = { ColorfulCrosshatchTestScreen() }),
    KraftBitmap("kraft_bitmap", "Kraft Bitmap", screen = { KraftBitmapTestScreen() }),
}

fun NavHostController.navigate(destination: Destination) {
    navigate(destination.route)
}
