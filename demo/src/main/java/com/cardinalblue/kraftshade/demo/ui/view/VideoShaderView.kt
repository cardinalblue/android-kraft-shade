package com.cardinalblue.kraftshade.demo.ui.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.cardinalblue.kraftshade.demo.R

class VideoShaderView: TraditionViewContent, DefaultLifecycleObserver {
    private var mediaPlayerTextureView: MediaPlayerTextureView? = null
    private var placeholderText: TextView? = null
    private var photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null

    override fun addContentTo(context: Context, container: FrameLayout) {
        val layoutInflater = LayoutInflater.from(context)
        val contentView = layoutInflater.inflate(R.layout.video_shader_traditional_content, container, false)
        container.addView(contentView)

        // Get UI components
        val selectVideoButton = contentView.findViewById<Button>(R.id.selectVideoButton)
        mediaPlayerTextureView = contentView.findViewById(R.id.mediaPlayerTextureView)
        placeholderText = contentView.findViewById(R.id.placeholderText)

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
        // Hide placeholder and show video view
        placeholderText?.visibility = View.GONE
        mediaPlayerTextureView?.visibility = View.VISIBLE
        
        // Start playing the video automatically
        mediaPlayerTextureView?.startPlayback(uri)
        
        Toast.makeText(mediaPlayerTextureView?.context, "Video selected successfully", Toast.LENGTH_SHORT).show()
    }

    // Lifecycle methods
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        mediaPlayerTextureView?.resumePlayback()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        mediaPlayerTextureView?.pausePlayback()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        mediaPlayerTextureView?.stopAndRelease()
        mediaPlayerTextureView = null
        placeholderText = null
        photoPickerLauncher = null
    }
}