package com.visioncoders.i220795_i210611

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var storiesRecyclerView: RecyclerView
    private lateinit var storyAdapter: StoryAdapter
    private val storyList = mutableListOf<Story>()

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

    private val followingList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupPresenceSystem()

        // Stories
        storiesRecyclerView = findViewById(R.id.stories_recyclerview)
        storiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        storyAdapter = StoryAdapter(storyList)
        storiesRecyclerView.adapter = storyAdapter

        // Posts
        postsRecyclerView = findViewById(R.id.posts_recyclerview)
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(postList)
        postsRecyclerView.adapter = postAdapter

        fetchFollowingAndLoadFeeds()

        // Click listeners
        findViewById<ImageView>(R.id.search_icon).setOnClickListener {
            startActivity(Intent(this, SearchMain::class.java))
        }
        findViewById<ImageView>(R.id.dm_icon).setOnClickListener {
            startActivity(Intent(this, DmActivity::class.java))
        }
        findViewById<ImageView>(R.id.like_icon).setOnClickListener {
            startActivity(Intent(this, FollowingActivity::class.java))
        }
        findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profile_icon).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<ImageView>(R.id.post_icon).setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
        findViewById<ImageView>(R.id.camera_icon).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        findViewById<ImageView>(R.id.logout_icon).setOnClickListener {
            database.reference.child("status").child(auth.currentUser!!.uid).setValue(null) // Mark as offline on logout
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupPresenceSystem() {
        val currentUid = auth.currentUser?.uid ?: return
        val userStatusRef = database.reference.child("status").child(currentUid)
        val presenceData = mapOf("state" to "online", "last_changed" to ServerValue.TIMESTAMP)

        userStatusRef.onDisconnect().setValue(mapOf("state" to "offline", "last_changed" to ServerValue.TIMESTAMP))
        userStatusRef.setValue(presenceData)
    }

    private fun fetchFollowingAndLoadFeeds() {
        val currentUid = auth.currentUser?.uid ?: return
        val followingRef = database.reference.child("users").child(currentUid).child("following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followingList.clear()
                for (userSnapshot in snapshot.children) {
                    userSnapshot.key?.let { followingList.add(it) }
                }
                followingList.add(currentUid)

                fetchStories()
                fetchPosts()
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    private fun fetchStories() {
        val storiesRef = database.reference.child("stories")
        storiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val twentyFourHoursAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000
                val userStoryMap = mutableMapOf<String, MutableList<Story>>()

                for (storySnapshot in snapshot.children) {
                    val story = storySnapshot.getValue(Story::class.java)
                    if (story != null && story.timestamp > twentyFourHoursAgo && followingList.contains(story.userId)) {
                        userStoryMap.getOrPut(story.userId) { mutableListOf() }.add(story)
                    }
                }

                storyList.clear()
                for ((_, userStories) in userStoryMap) {
                    userStories.sortByDescending { it.timestamp }
                    storyList.add(userStories.first())
                }
                storyAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchPosts() {
        val postsRef = database.reference.child("posts").orderByChild("timestamp")
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null && followingList.contains(post.userId)) {
                        postList.add(post)
                    }
                }
                postList.reverse()
                postAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}