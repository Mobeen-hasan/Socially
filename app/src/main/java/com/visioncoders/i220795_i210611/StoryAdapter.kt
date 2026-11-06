package com.visioncoders.i220795_i210611

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StoryAdapter(private val storyList: List<Story>) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storyImage: ShapeableImageView = itemView.findViewById(R.id.profile_image)
        val storyUsername: TextView = itemView.findViewById(R.id.story_username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.story_item, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storyList[position]

        // Fetch username and profile picture from Firebase
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(story.userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(FullUser::class.java)
                if (user != null) {
                    holder.storyUsername.text = user.username

                    // Set the user's profile picture, not the story image
                    if (user.profilePictureData.isNotEmpty()) {
                        try {
                            val imageBytes = Base64.decode(user.profilePictureData, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            holder.storyImage.setImageBitmap(decodedImage)
                        } catch (e: IllegalArgumentException) {
                            // Handle error, e.g., set a placeholder image
                            holder.storyImage.setImageResource(R.drawable.giyuu_1) // Placeholder
                        }
                    } else {
                        holder.storyImage.setImageResource(R.drawable.giyuu_1) // Placeholder
                    }
                } else {
                    holder.storyUsername.text = "User"
                    holder.storyImage.setImageResource(R.drawable.giyuu_1) // Placeholder
                }
            }

            override fun onCancelled(error: DatabaseError) {
                holder.storyUsername.text = "User"
                holder.storyImage.setImageResource(R.drawable.giyuu_1) // Placeholder
            }
        })

        // The click listener correctly opens the story viewer
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ViewStoryActivity::class.java).apply {
                putExtra("USER_ID", story.userId)
                putExtra("STORY_ID", story.storyId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return storyList.size
    }
}
