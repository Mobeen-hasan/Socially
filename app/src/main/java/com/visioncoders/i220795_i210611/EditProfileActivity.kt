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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText

    private var imageBase64: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            profileImageView.setImageURI(it)
            try {
                val inputStream = contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                }
            } catch (e: Exception) {
                Log.e("EditProfileActivity", "Error converting image", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        profileImageView = findViewById(R.id.profile_image)
        nameEditText = findViewById(R.id.edit_name)
        usernameEditText = findViewById(R.id.edit_username)

        findViewById<TextView>(R.id.btn_change_photo).setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        findViewById<TextView>(R.id.btn_done).setOnClickListener {
            saveProfileChanges()
        }

        findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            finish()
        }

        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        val currentUid = auth.currentUser?.uid ?: return
        val userRef = database.reference.child("users").child(currentUid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(FullUser::class.java)
                if (user != null) {
                    nameEditText.setText(user.firstName)
                    usernameEditText.setText(user.username)
                    if (user.profilePictureData.isNotEmpty()) {
                        try {
                            val imageBytes = Base64.decode(user.profilePictureData, Base64.DEFAULT)
                            profileImageView.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                        } catch (e: Exception) {  }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun saveProfileChanges() {
        val currentUid = auth.currentUser?.uid ?: return
        val userRef = database.reference.child("users").child(currentUid)

        val updatedUserData = mutableMapOf<String, Any>()
        updatedUserData["firstName"] = nameEditText.text.toString().trim()
        updatedUserData["username"] = usernameEditText.text.toString().trim()
        if (imageBase64 != null) {
            updatedUserData["profilePictureData"] = imageBase64!!
        }

        userRef.updateChildren(updatedUserData).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
