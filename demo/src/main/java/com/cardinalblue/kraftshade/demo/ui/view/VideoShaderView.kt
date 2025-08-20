package com.cardinalblue.kraftshade.demo.ui.view

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Button
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
import com.cardinalblue.kraftshade.shader.builtin.RGBKraftShader
import com.cardinalblue.kraftshade.widget.KraftVideoEffectTextureView

class VideoShaderView: TraditionViewContent, DefaultLifecycleObserver {
    private var kraftVideoEffectTextureView: KraftVideoEffectTextureView? = null
    private var photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var brightnessLabel: TextView? = null
    private var brightnessSlider: SeekBar? = null

    var brightness: Float = 0.3f
    private var videoRotation: Float = 0f

    override fun addContentTo(context: Context, container: FrameLayout) {
        val layoutInflater = LayoutInflater.from(context)
        val contentView = layoutInflater.inflate(R.layout.video_shader_traditional_content, container, false)
        container.addView(contentView)

        // Get UI components
        val selectVideoButton = contentView.findViewById<Button>(R.id.selectVideoButton)
        kraftVideoEffectTextureView = contentView.findViewById(R.id.kraftVideoEffectTextureView)
        brightnessLabel = contentView.findViewById(R.id.brightnessLabel)
        brightnessSlider = contentView.findViewById(R.id.brightnessSlider)

        // Setup brightness slider
        setupBrightnessSlider()

        // Setup photo picker launcher and lifecycle observer if context is ComponentActivity
        if (context is ComponentActivity) {
            setupPhotoPickerLauncher(context)
            context.lifecycle.addObserver(this)
        }

        // Set button click listener
        selectVideoButton.setOnClickListener {
            if (context is ComponentActivity) {
                openPhotoPicker()
            } else {
                Toast.makeText(context, "Cannot access picker from this context", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBrightnessSlider() {
        brightnessSlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    brightness = (progress / 100.0f) - 1
                    
                    // Update label
                    brightnessLabel?.text = "Brightness: %.2f".format(brightness)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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
        // Get video orientation from metadata
        videoRotation = getVideoRotation(uri)
        
        // Start playing the video automatically
        kraftVideoEffectTextureView?.startPlayback(uri)
        applyEffect()
        
        Toast.makeText(kraftVideoEffectTextureView?.context, "Video selected successfully (rotation: ${videoRotation}°)", Toast.LENGTH_SHORT).show()
    }
    
    private fun getVideoRotation(uri: Uri): Float {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(kraftVideoEffectTextureView?.context, uri)
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            rotation?.toFloatOrNull() ?: 0f
        } catch (e: Exception) {
            0f
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    private fun applyEffect() {
        val kraftVideoEffectTextureView = kraftVideoEffectTextureView ?: return

        kraftVideoEffectTextureView.setEffectWithPipeline(
            videoRotation = { videoRotation },
        ) { inputTexture, targetBuffer ->
            serialSteps(inputTexture, targetBuffer) {
                step(RGBKraftShader(brightness, 0.5f, 0f))
                step(BrightnessKraftShader(brightness)) {
                    it.brightness = this@VideoShaderView.brightness
                }
            }
        }
    }

    // Lifecycle methods
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        kraftVideoEffectTextureView?.resumePlayback()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        kraftVideoEffectTextureView?.pausePlayback()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        kraftVideoEffectTextureView?.stopAndRelease()
        kraftVideoEffectTextureView = null
        photoPickerLauncher = null
    }
}