package com.visioncoders.i220795_i210611

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UploadActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var featuredImage: ImageView
    private lateinit var captionInput: EditText
    private var imageBase64: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        Log.d("UploadActivity", "Gallery result received.")
        imageBase64 = null

        if (uri == null) {
            Log.d("UploadActivity", "URI is null. User may have cancelled.")
            return@registerForActivityResult
        }

        featuredImage.setImageURI(uri)

        try {
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("UploadActivity", "InputStream is null!")
                Toast.makeText(this, "Error: Could not open image stream.", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            val bytes = inputStream.readBytes()
            inputStream.close()

            if (bytes.isEmpty()) {
                Log.e("UploadActivity", "Byte array is empty!")
                Toast.makeText(this, "Error: Could not read image data.", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            if (bytes.size > 5 * 1024 * 1024) { // 5MB Limit
                Log.e("UploadActivity", "Image is too large: ${bytes.size} bytes.")
                Toast.makeText(this, "Image is too large (Max 5MB).", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }

            imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
            Log.d("UploadActivity", "Base64 encoding successful.")
            Toast.makeText(this, "Image processed and ready. Click Post to upload.", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.e("UploadActivity", "An exception occurred during image processing.", e)
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        featuredImage = findViewById(R.id.featured_image)
        captionInput = findViewById(R.id.caption_input)
        val cancelLink: TextView = findViewById(R.id.btn_cancel)
        val postButton: TextView = findViewById(R.id.btn_post)
        val galleryButton: TextView = findViewById(R.id.btn_gallery)

        cancelLink.setOnClickListener { finish() }
        galleryButton.setOnClickListener { galleryLauncher.launch("image/*") }
        postButton.setOnClickListener { uploadPost() }
    }

    private fun uploadPost() {
        val caption = captionInput.text.toString().trim()
        if (imageBase64 == null) {
            Toast.makeText(this, "Please select an image first.", Toast.LENGTH_LONG).show()
            return
        }
        if (caption.isEmpty()) {
            Toast.makeText(this, "Please enter a caption.", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(this, "Uploading post...", Toast.LENGTH_SHORT).show()
        val currentUser = auth.currentUser ?: return

        val postsRef = database.reference.child("posts")
        val postId = postsRef.push().key ?: return

        val post = Post(
            postId = postId,
            userId = currentUser.uid,
            imageData = imageBase64!!,
            caption = caption,
            timestamp = System.currentTimeMillis()
        )

        postsRef.child(postId).setValue(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Post uploaded successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("UploadActivity", "Failed to upload post to database", e)
                Toast.makeText(this, "Database Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}