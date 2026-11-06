package com.visioncoders.i220795_i210611

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CommentActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()

    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)


        findViewById<ImageView>(R.id.back).setOnClickListener { finish() }


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        postId = intent.getStringExtra("POST_ID")

        if (postId == null) {
            finish()
            return
        }

        commentsRecyclerView = findViewById(R.id.comments_recyclerview)
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(commentList)
        commentsRecyclerView.adapter = commentAdapter

        loadPostInfo()
        loadComments()

        findViewById<Button>(R.id.send_comment_button).setOnClickListener {
            postComment()
        }
    }

    private fun loadPostInfo() {
        val postRef = database.reference.child("posts").child(postId!!)
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(Post::class.java)
                if (post != null) {
                    findViewById<TextView>(R.id.post_caption_comment).text = post.caption
                    try {
                        val imageBytes = Base64.decode(post.imageData, Base64.DEFAULT)
                        findViewById<ImageView>(R.id.post_image_comment).setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                    } catch (e: Exception) { /* Handle error */ }
                }
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    private fun loadComments() {
        val commentsRef = database.reference.child("comments").child(postId!!)
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    if (comment != null) {
                        commentList.add(comment)
                    }
                }
                commentAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    private fun postComment() {
        val commentEditText = findViewById<EditText>(R.id.comment_edittext)
        val commentText = commentEditText.text.toString().trim()
        val currentUser = auth.currentUser

        if (commentText.isEmpty() || currentUser == null) {
            return
        }

        val commentsRef = database.reference.child("comments").child(postId!!)
        val commentId = commentsRef.push().key ?: return

        val comment = Comment(
            commentId = commentId,
            postId = postId!!,
            userId = currentUser.uid,
            text = commentText,
            timestamp = System.currentTimeMillis()
        )

        commentsRef.child(commentId).setValue(comment).addOnSuccessListener {
            commentEditText.text.clear()
            Toast.makeText(this, "Comment posted!", Toast.LENGTH_SHORT).show()
        }
    }
}