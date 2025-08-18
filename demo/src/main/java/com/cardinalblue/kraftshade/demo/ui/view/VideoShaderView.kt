package com.cardinalblue.kraftshade.demo.ui.view

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.cardinalblue.kraftshade.demo.R

class VideoShaderView: TraditionViewContent {

    private var videoPathText: TextView? = null
    private var photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null

    override fun addContentTo(context: Context, container: FrameLayout) {
        val layoutInflater = LayoutInflater.from(context)
        val contentView = layoutInflater.inflate(R.layout.video_shader_traditional_content, container, false)
        container.addView(contentView)

        // Get UI components
        val selectVideoButton = contentView.findViewById<Button>(R.id.selectVideoButton)
        videoPathText = contentView.findViewById(R.id.videoPathText)

        // Setup photo picker launcher if context is ComponentActivity
        if (context is ComponentActivity) {
            setupPhotoPickerLauncher(context)
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
                handleVideoSelected(activity, uri)
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

    @Suppress("SetTextI18n")
    private fun handleVideoSelected(context: Context, uri: Uri) {
        // Display the video URI path
        videoPathText?.text = "Selected video: $uri"
        
        // You can also get the actual file path if needed
        val realPath = getRealPathFromURI(context, uri)
        if (realPath != null) {
            videoPathText?.text = "Selected video path:\n$realPath"
        }
        
        Toast.makeText(context, "Video selected successfully", Toast.LENGTH_SHORT).show()
    }

    private fun getRealPathFromURI(context: Context, uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val columnIndex = it.getColumnIndex(MediaStore.Video.Media.DATA)
                if (columnIndex >= 0 && it.moveToFirst()) {
                    it.getString(columnIndex)
                } else {
                    uri.toString()
                }
            }
        } catch (_: Exception) {
            uri.toString()
        }
    }
}