package com.visioncoders.i220795_i210611

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostAdapter(private val postList: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.post_user_image)
        val username: TextView = itemView.findViewById(R.id.post_username)
        val postImage: ImageView = itemView.findViewById(R.id.post_image)
        val caption: TextView = itemView.findViewById(R.id.post_caption)
        val likeButton: ImageView = itemView.findViewById(R.id.like_button)
        val commentButton: ImageView = itemView.findViewById(R.id.comment_button)
        val likesCount: TextView = itemView.findViewById(R.id.likes_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Set Post Image
        try {
            val imageBytes = Base64.decode(post.imageData, Base64.DEFAULT)
            holder.postImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
        } catch (e: Exception) { /* Handle error */ }

        // Set Caption
        holder.caption.text = post.caption

        // Set User Info
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(post.userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(FullUser::class.java)
                if (user != null) {
                    holder.username.text = user.username
                    if (user.profilePictureData.isNotEmpty()) {
                        try {
                            val imageBytes = Base64.decode(user.profilePictureData, Base64.DEFAULT)
                            holder.userImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                        } catch (e: Exception) { /* Handle error */ }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Set Likes
        holder.likesCount.text = "${post.likes.size} likes"
        holder.likeButton.setImageResource(
            if (post.likes.containsKey(currentUser?.uid)) R.drawable.heart_full else R.drawable.like
        )

        // Like button click listener
        holder.likeButton.setOnClickListener {
            val postRef = FirebaseDatabase.getInstance().getReference("posts").child(post.postId)
            postRef.child("likes").child(currentUser!!.uid).get().addOnSuccessListener {
                if (it.exists()) {
                    postRef.child("likes").child(currentUser.uid).removeValue()
                } else {
                    postRef.child("likes").child(currentUser.uid).setValue(true)
                }
            }
        }

        // Comment button click listener
        holder.commentButton.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra("POST_ID", post.postId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}
