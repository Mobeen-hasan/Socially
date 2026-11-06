package com.visioncoders.i220795_i210611

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CelebrityActivityFollow: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_celeberity_follow) // Same as xml file name

        val closeIcon: ImageView = findViewById(R.id.back_arrow)
        closeIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        val followingIcon: TextView = findViewById(R.id.following_button)
        followingIcon.setOnClickListener {
            val intent = Intent(this, CelebrityActivity::class.java)
            startActivity(intent)
        }


        val homeIcon: ImageView = findViewById(R.id.home_icon)
        homeIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        val searchIcon: ImageView = findViewById(R.id.search_icon)
        // Set an OnClickListener on the search icon
        searchIcon.setOnClickListener {
            // Create the explicit intent to open the new activity
            val intent = Intent(this, SearchMain::class.java)
            startActivity(intent)
        }

        val likeIcon: ImageView = findViewById(R.id.like_icon)
        likeIcon.setOnClickListener {
            val intent = Intent(this, FollowingActivity::class.java)
            startActivity(intent)
        }

        val profileIcon2: de.hdodenhof.circleimageview.CircleImageView = findViewById(R.id.profile_icon)
        profileIcon2.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val postIcon: ImageView = findViewById(R.id.post_icon)
        postIcon.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }

    }
}