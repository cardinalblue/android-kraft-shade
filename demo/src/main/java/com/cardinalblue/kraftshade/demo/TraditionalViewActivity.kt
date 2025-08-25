package com.cardinalblue.kraftshade.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.cardinalblue.kraftshade.demo.ui.view.BasicShaderView
import com.cardinalblue.kraftshade.demo.ui.view.AnimatedShaderView
import com.cardinalblue.kraftshade.demo.ui.view.VideoShaderView

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
            "basic_shader_traditional" -> BasicShaderView().addContentTo(this, contentContainer)
            "animated_shader_traditional" -> AnimatedShaderView().addContentTo(this, contentContainer)
            "video_shader_traditional" -> VideoShaderView().addContentTo(this, contentContainer)
            else -> addPlaceholderContent(contentContainer, title)
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