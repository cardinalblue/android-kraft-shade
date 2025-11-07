package com.cardinalblue.kraftshade.demo.ui.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.cardinalblue.kraftshade.demo.R
import com.cardinalblue.kraftshade.shader.builtin.BrightnessKraftShader
import com.cardinalblue.kraftshade.shader.builtin.DrawTextureKraftShader
import com.cardinalblue.kraftshade.shader.builtin.SaturationKraftShader
import com.cardinalblue.kraftshade.widget.KraftVideoEffectTextureView

class VideoShaderView: TraditionViewContent, DefaultLifecycleObserver {
    private var kraftVideoEffectTextureView: KraftVideoEffectTextureView? = null
    private var photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var brightnessLabel: TextView? = null
    private var brightnessSlider: SeekBar? = null
    private var brightnessCheckBox: CheckBox? = null
    private var saturationLabel: TextView? = null
    private var saturationSlider: SeekBar? = null
    private var saturationCheckBox: CheckBox? = null
    private var playPauseButton: Button? = null

    var brightness: Float = 0.0f
    var saturation: Float = 1.0f

    override fun addContentTo(context: Context, container: FrameLayout) {
        val layoutInflater = LayoutInflater.from(context)
        val contentView = layoutInflater.inflate(R.layout.video_shader_traditional_content, container, false)
        container.addView(contentView)

        // Get UI components
        val selectVideoButton = contentView.findViewById<Button>(R.id.selectVideoButton)
        playPauseButton = contentView.findViewById(R.id.playPauseButton)
        kraftVideoEffectTextureView = contentView.findViewById(R.id.kraftVideoEffectTextureView)
        brightnessLabel = contentView.findViewById(R.id.brightnessLabel)
        brightnessSlider = contentView.findViewById(R.id.brightnessSlider)
        brightnessCheckBox = contentView.findViewById(R.id.brightnessCheckBox)
        saturationLabel = contentView.findViewById(R.id.saturationLabel)
        saturationSlider = contentView.findViewById(R.id.saturationSlider)
        saturationCheckBox = contentView.findViewById(R.id.saturationCheckBox)

        // Setup controls
        setupBrightnessSlider()
        setupSaturationSlider()
        setupCheckBoxes()

        // Setup photo picker launcher and lifecycle observer if context is ComponentActivity
        if (context is ComponentActivity) {
            setupPhotoPickerLauncher(context)
            context.lifecycle.addObserver(this)
        }

        // Set button click listeners
        selectVideoButton.setOnClickListener {
            if (context is ComponentActivity) {
                openPhotoPicker()
            } else {
                Toast.makeText(context, "Cannot access picker from this context", Toast.LENGTH_SHORT).show()
            }
        }
        
        playPauseButton?.setOnClickListener {
            togglePlayPause()
        }
    }

    private fun setupBrightnessSlider() {
        brightnessSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    brightness = (progress / 100.0f) - 1
                    
                    // Update label and apply effect
                    brightnessLabel?.text = "Brightness: %.2f".format(brightness)
                    kraftVideoEffectTextureView?.requestRender()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupSaturationSlider() {
        saturationSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    saturation = progress / 100.0f
                    
                    // Update label and apply effect
                    saturationLabel?.text = "Saturation: %.2f".format(saturation)
                    kraftVideoEffectTextureView?.requestRender()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupCheckBoxes() {
        brightnessCheckBox?.setOnCheckedChangeListener { _, _ ->
            applyEffect()
        }
        
        saturationCheckBox?.setOnCheckedChangeListener { _, _ ->
            applyEffect()
        }
    }

    private fun setupPhotoPickerLauncher(activity: ComponentActivity) {
        // Photo picker launcher for videos
        photoPickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                handleVideoSelected(uri)
            } else {
                Toast.makeText(activity, "No video selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openPhotoPicker() {
        photoPickerLauncher?.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }
    private fun handleVideoSelected(uri: Uri) {
        kraftVideoEffectTextureView?.setVideoUri(uri, autoPlay = false)
        applyEffect()
        
        // Enable play/pause button and update text
        playPauseButton?.let { button ->
            button.isEnabled = true
            button.text = "Play"
        }
    }

    private fun applyEffect() {
        val kraftVideoEffectTextureView = kraftVideoEffectTextureView ?: return

        kraftVideoEffectTextureView.setEffectWithPipeline { inputTexture, targetBuffer ->
            serialSteps(inputTexture, targetBuffer) {
                val hasSaturationEffect = saturationCheckBox?.isChecked == true
                val hasBrightnessEffect = brightnessCheckBox?.isChecked == true
                if (hasSaturationEffect) {
                    step(SaturationKraftShader(saturation)) {
                        it.saturation = this@VideoShaderView.saturation
                    }
                }

                if (hasBrightnessEffect) {
                    step(BrightnessKraftShader(brightness)) {
                        it.brightness = this@VideoShaderView.brightness
                    }
                }

                if (!hasSaturationEffect && !hasBrightnessEffect) {
                    // If no effects are selected, just use the DrawTextureKraftShader
                    step(DrawTextureKraftShader())
                }
            }
        }
    }

    private fun togglePlayPause() {
        val kraftVideoEffectTextureView = kraftVideoEffectTextureView ?: return
        val button = playPauseButton ?: return
        
        if (kraftVideoEffectTextureView.isPlaying()) {
            kraftVideoEffectTextureView.pausePlayback()
            button.text = "Play"
        } else {
            kraftVideoEffectTextureView.resumePlayback()
            button.text = "Pause"
        }
    }

    // Lifecycle methods
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        kraftVideoEffectTextureView?.onResume()
        playPauseButton?.text = "Pause"
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        kraftVideoEffectTextureView?.onPause()
        playPauseButton?.text = "Play"
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        kraftVideoEffectTextureView?.releaseMediaPlayer()
        kraftVideoEffectTextureView = null
        photoPickerLauncher = null
    }
}