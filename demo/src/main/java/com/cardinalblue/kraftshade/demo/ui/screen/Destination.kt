package com.cardinalblue.kraftshade.demo.ui.screen

import androidx.compose.runtime.Composable
import com.cardinalblue.kraftshade.demo.ui.screen.animation.KraftShadeAnimatedViewTestWindow
import com.cardinalblue.kraftshade.demo.ui.screen.basic.BasicShaderScreen
import com.cardinalblue.kraftshade.demo.ui.screen.basic.BlendingExampleScreen
import com.cardinalblue.kraftshade.demo.ui.screen.blur.CircularBlurPerformanceTestWindow
import com.cardinalblue.kraftshade.demo.ui.screen.blur.CircularBlurTestWindow
import com.cardinalblue.kraftshade.demo.ui.screen.color.ColorBlendShaderScreen
import com.cardinalblue.kraftshade.demo.ui.screen.color.FalseColorShaderScreen
import com.cardinalblue.kraftshade.demo.ui.screen.color.LevelsShaderScreen
import com.cardinalblue.kraftshade.demo.ui.screen.color.LookUpTableShaderTestScreen
import com.cardinalblue.kraftshade.demo.ui.screen.dsl.KraftBitmapTestScreen
import com.cardinalblue.kraftshade.demo.ui.screen.effect.ColorfulCrosshatchTestScreen
import com.cardinalblue.kraftshade.demo.ui.screen.effect.CrosshatchTestScreen
import com.cardinalblue.kraftshade.demo.ui.screen.effect.EmbossShaderScreen
import com.cardinalblue.kraftshade.demo.ui.screen.effect.KuwaharaTestWindow
import com.cardinalblue.kraftshade.demo.ui.screen.effect.ToonEffectTestWindow
import com.cardinalblue.kraftshade.demo.ui.screen.media3.ExoPlayerWithKraftShadePipeline
import com.cardinalblue.kraftshade.demo.ui.screen.media3.ExoPlayerWithKraftShaders
import com.cardinalblue.kraftshade.demo.ui.screen.morphology.ErosionDilationTestScreen
import com.cardinalblue.kraftshade.demo.ui.screen.view.ResizeTestScreen
import com.cardinalblue.kraftshade.demo.ui.screen.view.TransparencyTestWindow
import com.cardinalblue.kraftshade.demo.ui.screen.view.compose.KraftShadeEffectViewTestWindow

enum class SampleType {
    Compose,
    TraditionalView,
    None
}

enum class Destination(
    val route: String,
    val title: String,
    val sampleType: SampleType,
    val category: Category = Category.OTHER,
    val screen: @Composable () -> Unit,
) {
    Home("home", "Home", sampleType = SampleType.None, category = Category.OTHER, screen = { HomeScreen() }),
    ComposableSamples("compose_samples", "Compose Samples", sampleType = SampleType.None, category = Category.OTHER, screen = { ComposableSampleScreen() }),
    TraditionalViewSamples("traditional_view_samples", "Traditional View Samples", sampleType = SampleType.None, category = Category.OTHER, screen = { TraditionalViewSampleScreen() }),
    ResizeTest("resize_test", "Resize Test", sampleType = SampleType.Compose, category = Category.OTHER, screen = { ResizeTestScreen() }),
    BasicShader("basic_shader", "Basic Shader", sampleType = SampleType.Compose, category = Category.BASIC, screen = { BasicShaderScreen() }),
    BlendingExample("blending_example", "Blending Example", sampleType = SampleType.Compose, category = Category.BASIC, screen = { BlendingExampleScreen() }),
    EmbossShader("emboss_shader", "Emboss Shader", sampleType = SampleType.Compose, category = Category.EFFECTS, screen = { EmbossShaderScreen() }),
    LookUpTableShader("look_up_table_shader", "Look Up Table Shader", sampleType = SampleType.Compose, category = Category.COLOR, screen = { LookUpTableShaderTestScreen() }),
    ColorBlendShader("color_blend_shader", "Color Blend Shader", sampleType = SampleType.Compose, category = Category.COLOR, screen = { ColorBlendShaderScreen() }),
    TransparencyTest("transparency_test", "Transparency Test", sampleType = SampleType.Compose, category = Category.OTHER, screen = { TransparencyTestWindow() }),
    KraftShadeAnimatedView("compose_animated", "Compose (animated)", sampleType = SampleType.Compose, category = Category.OTHER, screen = { KraftShadeAnimatedViewTestWindow() }),
    KraftShadeEffectView("compose_effect", "Compose (effect)", sampleType = SampleType.Compose, category = Category.OTHER, screen = { KraftShadeEffectViewTestWindow() }),
    CrosshatchShader("crosshatch_shader", "Crosshatch Shader", sampleType = SampleType.Compose, category = Category.EFFECTS, screen = { CrosshatchTestScreen() }),
    ColorfulCrosshatchShader("colorful_crosshatch_shader", "Colorful Crosshatch Shader", sampleType = SampleType.Compose, category = Category.EFFECTS, screen = { ColorfulCrosshatchTestScreen() }),
    KraftBitmap("kraft_bitmap", "Kraft Bitmap", sampleType = SampleType.Compose, category = Category.OTHER, screen = { KraftBitmapTestScreen() }),
    CircularBlurPerformance("circular_blur_performance", "Circular Blur Performance", sampleType = SampleType.Compose, category = Category.BLUR, screen = { CircularBlurPerformanceTestWindow() }),
    CircularBlur("circular_blur", "Circular Blur", sampleType = SampleType.Compose, category = Category.BLUR, screen = { CircularBlurTestWindow() }),
    ToonEffect("toon_effect", "Toon Effect", sampleType = SampleType.Compose, category = Category.EFFECTS, screen = { ToonEffectTestWindow() }),
    KuwaharaEffect("kuwahara_effect", "Kuwahara Effect", sampleType = SampleType.Compose, category = Category.EFFECTS, screen = { KuwaharaTestWindow() }),
    ErosionDilationShaderTest("erosion_dilation_shader_test", "Erosion/Dilation Shader Test", sampleType = SampleType.Compose, category = Category.BLUR, screen = { ErosionDilationTestScreen() }),
    LevelsShader("levels_shader", "Levels Shader", sampleType = SampleType.Compose, category = Category.COLOR, screen = { LevelsShaderScreen() }),
    FalseColorShader("false_color_shader", "False Color Shader", sampleType = SampleType.Compose, category = Category.COLOR, screen = { FalseColorShaderScreen() }),
    ExoPlayerWithKraftShaders("exo_player_with_kraft_shaders", "ExoPlayer with Kraft Shaders", sampleType = SampleType.Compose, category = Category.MEDIA3, screen = { ExoPlayerWithKraftShaders() }),
    ExoPlayerWithKraftShadePipeline("exo_player_with_kraftshade_pipeline", "ExoPlayer with KraftShade Pipeline", sampleType = SampleType.Compose, category = Category.MEDIA3, screen = { ExoPlayerWithKraftShadePipeline() }),
    // Traditional View Samples
    BasicShaderTraditional("basic_shader_traditional", "Basic Shader (Traditional)", sampleType = SampleType.TraditionalView, category = Category.BASIC, screen = { /* Activity will handle this */ }),
    AnimatedShaderTraditional("animated_shader_traditional", "Animated Shader (Traditional)", sampleType = SampleType.TraditionalView, category = Category.BASIC, screen = { /* Activity will handle this */ }),
    VideoShaderTraditional("video_shader_traditional", "Video Shader (Traditional)", sampleType = SampleType.TraditionalView, category = Category.BASIC, screen = { /* Activity will handle this */ }),
}