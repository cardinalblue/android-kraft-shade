package com.cardinalblue.kraftshade.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.cardinalblue.kraftshade.demo.shader.DrawCircleKraftShader
import com.cardinalblue.kraftshade.model.GlColor
import com.cardinalblue.kraftshade.widget.KraftEffectTextureView

class TraditionalViewActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_ROUTE = "route"
        private const val EXTRA_TITLE = "title"

        fun createIntent(context: Context, route: String, title: String): Intent {
            return Intent(context, TraditionalViewActivity::class.java).apply {
                putExtra(EXTRA_ROUTE, route)
                putExtra(EXTRA_TITLE, title)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traditional_view)
        
        val route = intent.getStringExtra(EXTRA_ROUTE) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Traditional View Sample"
        
        // Set title
        findViewById<TextView>(R.id.titleText).text = title
        
        // Get the content container
        val contentContainer = findViewById<FrameLayout>(R.id.contentContainer)
        
        // Add sample content based on route
        when (route) {
            "basic_shader_traditional" -> addBasicShaderContent(contentContainer)
            else -> addPlaceholderContent(contentContainer, title)
        }
    }
    
    private fun addBasicShaderContent(container: FrameLayout) {
        val contentView = layoutInflater.inflate(R.layout.basic_shader_traditional_content, container, false)
        container.addView(contentView)
        
        // Get the KraftShade container and add KraftEffectTextureView
        val kraftShadeContainer = contentView.findViewById<FrameLayout>(R.id.kraftShadeContainer)
        
        // Create and configure KraftEffectTextureView
        val kraftEffectTextureView = KraftEffectTextureView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        // Clear existing content and add our KraftEffectTextureView
        kraftShadeContainer.removeAllViews()
        kraftShadeContainer.addView(kraftEffectTextureView)
        
        // Set up the effect using pipeline DSL
        kraftEffectTextureView.setEffect { targetBuffer ->
            pipeline(targetBuffer) {
                graphSteps(
                    targetBuffer
                ) {
                    val shader = DrawCircleKraftShader(
                        color = GlColor.Red,
                        backgroundColor = GlColor.Green,
                        scale = 0.6f
                    )
                    step(shader, graphTargetBuffer)
                }
            }
        }
    }
    
    private fun addPlaceholderContent(container: FrameLayout, title: String) {
        val textView = TextView(this).apply {
            text = "Sample content for: $title"
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }
        container.addView(textView)
    }
}