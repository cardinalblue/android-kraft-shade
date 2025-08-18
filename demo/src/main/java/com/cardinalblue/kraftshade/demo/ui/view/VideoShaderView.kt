package com.cardinalblue.kraftshade.demo.ui.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.cardinalblue.kraftshade.demo.R

class VideoShaderView: TraditionViewContent, DefaultLifecycleObserver {
    private var videoView: VideoView? = null
    private var placeholderText: TextView? = null
    private var photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var currentVideoPosition: Int = 0
    private var wasPlayingWhenPaused: Boolean = false

    override fun addContentTo(context: Context, container: FrameLayout) {
        val layoutInflater = LayoutInflater.from(context)
        val contentView = layoutInflater.inflate(R.layout.video_shader_traditional_content, container, false)
        container.addView(contentView)

        // Get UI components
        val selectVideoButton = contentView.findViewById<Button>(R.id.selectVideoButton)
        this.videoView = contentView.findViewById(R.id.videoView)
        this.placeholderText = contentView.findViewById(R.id.placeholderText)

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

        // TODO: Add video shader implementation here
        // This is currently an empty placeholder view
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
        // Start playing the video automatically
        playVideo(uri)
    }

    // Lifecycle methods
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // Resume video if it was playing
        videoView?.let { vv ->
            if (wasPlayingWhenPaused && currentVideoPosition > 0) {
                vv.seekTo(currentVideoPosition)
                vv.start()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // Save video state
        videoView?.let { vv ->
            wasPlayingWhenPaused = vv.isPlaying
            currentVideoPosition = vv.currentPosition
            if (vv.isPlaying) {
                vv.pause()
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        // Clean up resources
        videoView?.stopPlayback()
        videoView = null
        placeholderText = null
        photoPickerLauncher = null
    }

    private fun playVideo(uri: Uri) {
        val videoView = videoView ?: return

        // Hide placeholder and show video view
        placeholderText?.visibility = View.GONE
        videoView.visibility = View.VISIBLE

        // Set video URI and prepare
        videoView.setVideoURI(uri)

        // Set completion listener to loop the video
        videoView.setOnCompletionListener {
            it.seekTo(0)
            it.start()
        }

        // Set prepared listener to start playback
        videoView.setOnPreparedListener { mediaPlayer ->
            videoView.start()
            // Optional: Set looping
            mediaPlayer.isLooping = true
        }

        // Set error listener
        videoView.setOnErrorListener { _, what, extra ->
            Toast.makeText(
                videoView.context,
                "Video playback error: $what, $extra",
                Toast.LENGTH_SHORT
            ).show()
            // Show placeholder again on error
            videoView.visibility = View.GONE
            placeholderText?.visibility = View.VISIBLE
            true
        }
    }
}