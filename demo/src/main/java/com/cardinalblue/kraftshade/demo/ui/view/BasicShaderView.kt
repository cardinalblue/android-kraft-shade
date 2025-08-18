package com.cardinalblue.kraftshade.demo.ui.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.cardinalblue.kraftshade.demo.R
import com.cardinalblue.kraftshade.demo.shader.DrawCircleKraftShader
import com.cardinalblue.kraftshade.model.GlColor
import com.cardinalblue.kraftshade.widget.KraftEffectTextureView

class BasicShaderView: TraditionViewContent {

    override fun addContentTo(context: Context, container: FrameLayout) {
        val layoutInflater = LayoutInflater.from(context)
        val contentView = layoutInflater.inflate(R.layout.basic_shader_traditional_content, container, false)
        container.addView(contentView)

        // Get the KraftShade container and add KraftEffectTextureView
        val kraftShadeContainer = contentView.findViewById<FrameLayout>(R.id.kraftShadeContainer)

        // Create and configure KraftEffectTextureView
        val kraftEffectTextureView = KraftEffectTextureView(context).apply {
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
}