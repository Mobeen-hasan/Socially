package com.visioncoders.i220795_i210611

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var profileImageView: ImageView
    private lateinit var usernameHeader: TextView
    private lateinit var fullName: TextView
    private lateinit var followersCount: TextView
    private lateinit var followingCount: TextView
    private lateinit var profileActionButton: Button
    private lateinit var messageButton: Button

    private lateinit var requests_button: Button

    private var displayedUserId: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadProfilePicture(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        profileImageView = findViewById(R.id.profile_image_view)
        usernameHeader = findViewById(R.id.username_header)
        fullName = findViewById(R.id.full_name)
        followersCount = findViewById(R.id.followers_count)
        followingCount = findViewById(R.id.following_count)
        profileActionButton = findViewById(R.id.profile_action_button)
        messageButton = findViewById(R.id.message_button)

        displayedUserId = intent.getStringExtra("USER_ID") ?: auth.currentUser?.uid

        if (displayedUserId == null) {
            finish()
            return
        }

        loadUserProfile(displayedUserId!!)
        setupBottomNavigation()

        profileImageView.setOnClickListener {
            if (displayedUserId == auth.currentUser?.uid) {
                galleryLauncher.launch("image/*")
            }
        }

        requests_button=findViewById<Button>(R.id.requests_button)
        requests_button.setOnClickListener {
            startActivity(Intent(this, FollowRequestsActivity::class.java))
        }

        followersCount.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("LIST_TYPE", "Followers")
            intent.putExtra("USER_ID", displayedUserId)
            startActivity(intent)
        }

        followingCount.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("LIST_TYPE", "Following")
            intent.putExtra("USER_ID", displayedUserId)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        findViewById<ImageView>(R.id.search_icon).setOnClickListener {
            startActivity(Intent(this, SearchMain::class.java))
        }
        findViewById<ImageView>(R.id.post_icon).setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
        findViewById<ImageView>(R.id.home_icon).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        findViewById<ImageView>(R.id.profile_icon).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<ImageView>(R.id.like_icon).setOnClickListener {
            startActivity(Intent(this, FollowingActivity::class.java))
        }


    }

    private fun loadUserProfile(userId: String) {
        val userRef = database.reference.child("users").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(FullUser::class.java)
                if (user != null) {
                    usernameHeader.text = user.username
                    fullName.text = "${user.firstName} ${user.lastName}"
                    followersCount.text = user.followers.size.toString()
                    followingCount.text = user.following.size.toString()

                    if (user.profilePictureData.isNotEmpty()) {
                        try {
                            val imageBytes = Base64.decode(user.profilePictureData, Base64.DEFAULT)
                            profileImageView.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                        } catch (e: Exception) { /* Handle error */ }
                    }
                    updateFollowAndMessageButtonsState(user)
                }
            }

            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    private fun updateFollowAndMessageButtonsState(user: FullUser) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == displayedUserId) {
            profileActionButton.text = "Edit Profile"
            profileActionButton.setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java))
            }
            messageButton.visibility = View.GONE
        } else {

            //this hdies the buttons when on other users profiles
            messageButton.visibility = View.VISIBLE
            requests_button.visibility=View.GONE

            messageButton.setOnClickListener {
                val intent = Intent(this, MessageActivity::class.java)
                intent.putExtra("OTHER_USER_ID", displayedUserId)
                startActivity(intent)
            }

            when {
                user.followers.containsKey(currentUid) -> {
                    profileActionButton.text = "Unfollow"
                    profileActionButton.setOnClickListener { unfollowUser() }
                }
                user.followRequests.containsKey(currentUid) -> {
                    profileActionButton.text = "Requested"
                    profileActionButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    profileActionButton.setTextColor(ContextCompat.getColor(this, R.color.maroon_dark))
                    profileActionButton.setOnClickListener { cancelFollowRequest() }
                }
                else -> {
                    profileActionButton.text = "Follow"
                    profileActionButton.setBackgroundColor(ContextCompat.getColor(this, R.color.maroon_dark))
                    profileActionButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                    profileActionButton.setOnClickListener { sendFollowRequest() }
                }
            }
        }
    }

    private fun sendFollowRequest() {
        val currentUid = auth.currentUser?.uid ?: return
        database.reference.child("users").child(displayedUserId!!).child("followRequests").child(currentUid).setValue(true)
    }

    private fun cancelFollowRequest() {
        val currentUid = auth.currentUser?.uid ?: return
        database.reference.child("users").child(displayedUserId!!).child("followRequests").child(currentUid).removeValue()
    }

    private fun unfollowUser() {
        val currentUid = auth.currentUser?.uid ?: return
        database.reference.child("users").child(displayedUserId!!).child("followers").child(currentUid).removeValue()
        database.reference.child("users").child(currentUid).child("following").child(displayedUserId!!).removeValue()
    }

    private fun uploadProfilePicture(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                val imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                val currentUid = auth.currentUser?.uid ?: return
                database.reference.child("users").child(currentUid).child("profilePictureData").setValue(imageBase64)
                    .addOnSuccessListener { Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show() }
            }
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error uploading profile picture", e)
        }
    }
}