package com.visioncoders.i220795_i210611

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DmActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatList = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dm)


        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }



        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        chatsRecyclerView = findViewById(R.id.chats_recyclerview)
        chatsRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(chatList)
        chatsRecyclerView.adapter = chatAdapter

        findViewById<Button>(R.id.new_message_button).setOnClickListener {
            startActivity(Intent(this, MessageActivity::class.java))
        }

        loadChats()
    }

    private fun loadChats() {
        val currentUid = auth.currentUser?.uid ?: return
        val chatsRef = database.reference.child("chats").orderByChild("lastMessageTimestamp")

        chatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (chatSnapshot in snapshot.children) {
                    val chat = chatSnapshot.getValue(Chat::class.java)
                    if (chat != null && chat.participants.contains(currentUid)) {
                        chatList.add(chat)
                    }
                }
                chatList.reverse() // Show newest chats first
                chatAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {  }
        })
    }
}