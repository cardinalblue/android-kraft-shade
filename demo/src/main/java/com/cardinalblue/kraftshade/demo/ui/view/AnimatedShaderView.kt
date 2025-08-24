package com.cardinalblue.kraftshade.demo.ui.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import com.cardinalblue.kraftshade.demo.R
import com.cardinalblue.kraftshade.demo.util.loadBitmapFromAsset
import com.cardinalblue.kraftshade.pipeline.input.bounceBetween
import com.cardinalblue.kraftshade.shader.buffer.asTexture
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader
import com.cardinalblue.kraftshade.widget.AnimatedKraftTextureView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnimatedShaderView: TraditionViewContent {

    override fun addContentTo(context: Context, container: FrameLayout) {
        val layoutInflater = LayoutInflater.from(context)
        val contentView = layoutInflater.inflate(R.layout.animated_shader_traditional_content, container, false)
        container.addView(contentView)

        // Get the KraftShade container and control button
        val kraftShadeContainer = contentView.findViewById<FrameLayout>(R.id.kraftShadeContainer)
        val playPauseButton = contentView.findViewById<Button>(R.id.playPauseButton)

        // Create and configure AnimatedKraftTextureView
        val animatedKraftTextureView = AnimatedKraftTextureView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Clear existing content and add our AnimatedKraftTextureView
        kraftShadeContainer.removeAllViews()
        kraftShadeContainer.addView(animatedKraftTextureView)

        // Set up the animated effect
        CoroutineScope(Dispatchers.Main).launch {
            animatedKraftTextureView.setEffectAndPlay { windowSurface, timeInput ->
                val bitmap = context.loadBitmapFromAsset("sample/cat.jpg")
                
                val saturationInput = timeInput
                    .bounceBetween(0f, 1f)

                pipeline(windowSurface) {
                    serialSteps(bitmap.asTexture(), windowSurface) {
                        step(SaturationKraftShader()) { shader ->
                            shader.saturation = saturationInput.get()
                        }
                    }
                }
            }
        }

        // Set up play/pause button functionality
        var isPlaying = true
        playPauseButton.setOnClickListener {
            if (isPlaying) {
                animatedKraftTextureView.stop()
                playPauseButton.text = "Play"
            } else {
                animatedKraftTextureView.play()
                playPauseButton.text = "Pause"
            }
            isPlaying = !isPlaying
        }
    }
}