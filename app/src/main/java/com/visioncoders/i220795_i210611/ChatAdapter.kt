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

class ChatAdapter(private val chatList: List<Chat>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.chat_profile_image)
        val username: TextView = itemView.findViewById(R.id.chat_username)
        val lastMessage: TextView = itemView.findViewById(R.id.chat_last_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

        val otherParticipantId = chat.participants.firstOrNull { it != currentUid }
        if (otherParticipantId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(otherParticipantId)
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

        holder.lastMessage.text = chat.lastMessage

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("CHAT_ID", chat.chatId)
            intent.putExtra("OTHER_USER_ID", otherParticipantId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = chatList.size
}
