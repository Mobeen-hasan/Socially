package com.visioncoders.i220795_i210611

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentAdapter(private val commentList: List<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.commenter_profile_image)
        val username: TextView = itemView.findViewById(R.id.commenter_username)
        val commentText: TextView = itemView.findViewById(R.id.comment_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.commentText.text = comment.text

        // Fetch user info
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(comment.userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(FullUser::class.java)
                if (user != null) {
                    holder.username.text = user.username
                    if (user.profilePictureData.isNotEmpty()) {
                        try {
                            val imageBytes = Base64.decode(user.profilePictureData, Base64.DEFAULT)
                            holder.profileImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                        } catch (e: Exception) { /* Handle error */ }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getItemCount() = commentList.size
}
