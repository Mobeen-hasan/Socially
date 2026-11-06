package com.visioncoders.i220795_i210611

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class CloseStoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_close_story) // Same as xml file name

        val closeIcon: ImageView = findViewById(R.id.close)
        closeIcon.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val publishIcon: ImageView = findViewById(R.id.publish_story)
        publishIcon.setOnClickListener {
            val intent = Intent(this, PublishStoryActivity::class.java)
            startActivity(intent)
        }

    }
}