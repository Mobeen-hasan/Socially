package com.visioncoders.i220795_i210611

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ViewStoryActivity : AppCompatActivity() {

    private lateinit var storyImageView: ImageView
    private lateinit var progressContainer: LinearLayout
    private lateinit var previousButton: View
    private lateinit var nextButton: View

    private lateinit var database: FirebaseDatabase
    private var userStories = mutableListOf<Story>()
    private var currentStoryIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_story)

        storyImageView = findViewById(R.id.story_image_view)
        progressContainer = findViewById(R.id.story_progress_container)
        previousButton = findViewById(R.id.previous_story_button)
        nextButton = findViewById(R.id.next_story_button)
        findViewById<ImageView>(R.id.close_story_button).setOnClickListener { finish() }

        database = FirebaseDatabase.getInstance()

        val userId = intent.getStringExtra("USER_ID")
        val clickedStoryId = intent.getStringExtra("STORY_ID")

        if (userId == null || clickedStoryId == null) {
            Toast.makeText(this, "Could not load stories.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchUserStories(userId, clickedStoryId)

        previousButton.setOnClickListener { showPreviousStory() }
        nextButton.setOnClickListener { showNextStory() }
    }

    private fun fetchUserStories(userId: String, clickedStoryId: String) {
        val storiesRef = database.reference.child("stories").orderByChild("userId").equalTo(userId)
        storiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userStories.clear()
                for (storySnapshot in snapshot.children) {
                    val story = storySnapshot.getValue(Story::class.java)
                    if (story != null) {
                        userStories.add(story)
                    }
                }
                // Sort by timestamp and find starting index
                userStories.sortBy { it.timestamp }
                currentStoryIndex = userStories.indexOfFirst { it.storyId == clickedStoryId }
                if (currentStoryIndex != -1) {
                    setupProgressBars()
                    displayCurrentStory()
                }
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    private fun setupProgressBars() {
        progressContainer.removeAllViews()
        for (i in userStories.indices) {
            val progressBar = View(this)
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            params.marginEnd = 4
            progressBar.layoutParams = params
            progressBar.setBackgroundResource(R.drawable.story_progress_inactive)
            progressContainer.addView(progressBar)
        }
    }

    private fun displayCurrentStory() {
        if (userStories.isEmpty()) return

        val story = userStories[currentStoryIndex]
        try {
            val imageBytes = Base64.decode(story.imageData, Base64.DEFAULT)
            storyImageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
        } catch (e: Exception) { /* Handle error */ }

        updateProgressBars()
    }

    private fun updateProgressBars() {
        for (i in 0 until progressContainer.childCount) {
            val progressBar = progressContainer.getChildAt(i)
            progressBar.setBackgroundResource(
                if (i <= currentStoryIndex) R.drawable.story_progress_active else R.drawable.story_progress_inactive
            )
        }
    }

    private fun showNextStory() {
        if (currentStoryIndex < userStories.size - 1) {
            currentStoryIndex++
            displayCurrentStory()
        } else {
            finish() // Close when last story is finished
        }
    }

    private fun showPreviousStory() {
        if (currentStoryIndex > 0) {
            currentStoryIndex--
            displayCurrentStory()
        }
    }
}
