package com.visioncoders.i220795_i210611

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CameraActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var storyImagePreview: ImageView
    private var imageBase64: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        Log.d("CameraActivity", "Gallery result received.")
        imageBase64 = null

        if (uri == null) {
            Log.d("CameraActivity", "URI is null. User may have cancelled.")
            return@registerForActivityResult
        }

        storyImagePreview.setImageURI(uri)

        try {
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("CameraActivity", "InputStream is null!")
                Toast.makeText(this, "Error: Could not open image stream.", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            val bytes = inputStream.readBytes()
            inputStream.close()

            if (bytes.isEmpty()) {
                Log.e("CameraActivity", "Byte array is empty!")
                Toast.makeText(this, "Error: Could not read image data.", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            if (bytes.size > 5 * 1024 * 1024) { // 5MB Limit
                Log.e("CameraActivity", "Image is too large: ${bytes.size} bytes.")
                Toast.makeText(this, "Image is too large (Max 5MB).", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
            Log.d("CameraActivity", "Base64 encoding successful.")
            Toast.makeText(this, "Image processed and ready. Click Upload Story to post.", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.e("CameraActivity", "An exception occurred during image processing.", e)
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        storyImagePreview = findViewById(R.id.story_image_preview)
        val selectStoryButton: Button = findViewById(R.id.select_story_button)
        val uploadStoryButton: Button = findViewById(R.id.upload_story_button)
        val closeButton: ImageView = findViewById(R.id.close)

        closeButton.setOnClickListener { finish() }
        selectStoryButton.setOnClickListener { galleryLauncher.launch("image/*") }
        uploadStoryButton.setOnClickListener { uploadStory() }
    }

    private fun uploadStory() {
        if (imageBase64 != null) {
            Toast.makeText(this, "Uploading story...", Toast.LENGTH_SHORT).show()
            val currentUser = auth.currentUser ?: return

            val storyId = database.reference.child("stories").push().key ?: return

            val story = Story(storyId, currentUser.uid, imageBase64!!, System.currentTimeMillis())

            database.reference.child("stories").child(storyId).setValue(story)
                .addOnSuccessListener {
                    Toast.makeText(this, "Story uploaded successfully!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("CameraActivity", "Failed to upload story to database", e)
                    Toast.makeText(this, "Database Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "Please select an image that has been processed successfully first.", Toast.LENGTH_LONG).show()
        }
    }
}