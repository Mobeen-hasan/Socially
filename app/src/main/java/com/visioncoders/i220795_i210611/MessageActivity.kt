package com.visioncoders.i220795_i210611

import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MessageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()

    private var chatId: String? = null
    private var otherUserId: String? = null

    private lateinit var toolbarUsername: TextView
    private lateinit var toolbarStatus: TextView
    private var statusListener: ValueEventListener? = null
    private var statusRef: DatabaseReference? = null
    private var callListener: ValueEventListener? = null
    private var callRef: DatabaseReference? = null

    private lateinit var contentObserver: ContentObserver
    private var lastScreenshotTime: Long = 0
    private val screenshotCooldown = 2500L

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { sendImageMessage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val toolbar: Toolbar = findViewById(R.id.message_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        toolbarUsername = findViewById(R.id.toolbar_username)
        toolbarStatus = findViewById(R.id.toolbar_status)

        chatId = intent.getStringExtra("CHAT_ID")
        otherUserId = intent.getStringExtra("OTHER_USER_ID")

        messagesRecyclerView = findViewById(R.id.messages_recyclerview)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        messagesRecyclerView.layoutManager = layoutManager

        if (chatId != null) {
            messageAdapter = MessageAdapter(this, messageList, chatId!!)
            messagesRecyclerView.adapter = messageAdapter
            loadMessages()
            listenForIncomingCalls()
        } else {
            getOrCreateChat()
        }

        setupUserInfoAndStatusListener()
        setupScreenshotObserver()

        findViewById<Button>(R.id.send_message_button).setOnClickListener { sendTextMessage() }
        findViewById<Button>(R.id.send_image_button).setOnClickListener { galleryLauncher.launch("image/*") }
        findViewById<ImageView>(R.id.video_call_button).setOnClickListener { startCall(true) }
        findViewById<ImageView>(R.id.voice_call_button).setOnClickListener { startCall(false) }
    }

    private fun startCall(isVideoCall: Boolean) {
        if (chatId != null) {
            val callData = mapOf("callerId" to auth.currentUser?.uid, "type" to if(isVideoCall) "video" else "voice")
            database.reference.child("calls").child(chatId!!).setValue(callData)

            val intent = Intent(this, VideoCallActivity::class.java).apply {
                putExtra("CHANNEL_ID", chatId)
                putExtra("IS_VIDEO_CALL", isVideoCall)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Chat not ready.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listenForIncomingCalls() {
        callRef = database.reference.child("calls").child(chatId!!)
        callListener = callRef?.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val callData = snapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                if (callData != null) {
                    val callerId = callData["callerId"] as String?
                    if (callerId != null && callerId != auth.currentUser?.uid) {
                        val isVideoCall = (callData["type"] as String?) == "video"
                        val intent = Intent(this@MessageActivity, VideoCallActivity::class.java).apply {
                            putExtra("CHANNEL_ID", chatId)
                            putExtra("IS_VIDEO_CALL", isVideoCall)
                        }
                        startActivity(intent)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupUserInfoAndStatusListener() {
        otherUserId?.let {
            val userRef = database.reference.child("users").child(it)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    toolbarUsername.text = snapshot.child("username").getValue(String::class.java) ?: "User"
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            statusRef = database.reference.child("status").child(it)
            statusListener = statusRef?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.child("state").getValue(String::class.java)
                    toolbarStatus.text = if (status == "online") "Online" else "Offline"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun getOrCreateChat() {
        val currentUid = auth.currentUser?.uid ?: return
        val otherUid = otherUserId ?: return
        val participants = listOf(currentUid, otherUid).sorted()
        val potentialChatId = participants.joinToString("_")

        val chatRef = database.reference.child("chats").child(potentialChatId)
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatId = potentialChatId
                if (!snapshot.exists()) {
                    val newChat = Chat(chatId!!, participants)
                    chatRef.setValue(newChat)
                }
                messageAdapter = MessageAdapter(this@MessageActivity, messageList, chatId!!)
                messagesRecyclerView.adapter = messageAdapter
                loadMessages()
                listenForIncomingCalls()
            }
            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun loadMessages() {
        val messagesRef = database.reference.child("messages").child(chatId!!)
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (msgSnapshot in snapshot.children) {
                    msgSnapshot.getValue(Message::class.java)?.let { messageList.add(it) }
                }
                messageAdapter.notifyDataSetChanged()
                messagesRecyclerView.scrollToPosition(messageList.size - 1)
            }
            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun sendTextMessage() {
        val editText = findViewById<EditText>(R.id.message_edittext)
        val text = editText.text.toString().trim()
        if (text.isNotEmpty()) {
            sendMessage(text, "", false)
            editText.text.clear()
        }
    }

    private fun sendImageMessage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                sendMessage("", Base64.encodeToString(bytes, Base64.DEFAULT), false)
            }
        } catch (e: Exception) {
            Log.e("MessageActivity", "Error sending image", e)
        }
    }

    private fun sendScreenshotNotification() {
        sendMessage("Screenshot taken", "", true)
    }

    private fun sendMessage(text: String, imageData: String, isScreenshot: Boolean) {
        val currentUid = auth.currentUser?.uid ?: return
        if (chatId == null) return

        val messagesRef = database.reference.child("messages").child(chatId!!)
        val messageId = messagesRef.push().key ?: return
        val message = Message(messageId, currentUid, text, imageData, System.currentTimeMillis(), isScreenshot)
        messagesRef.child(messageId).setValue(message)

        if (!isScreenshot) {
            val lastMessageText = if (text.isNotEmpty()) text else "[Image]"
            database.reference.child("chats").child(chatId!!).updateChildren(mapOf(
                "lastMessage" to lastMessageText,
                "lastMessageTimestamp" to System.currentTimeMillis()
            ))
        }
    }

    private fun setupScreenshotObserver() {
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastScreenshotTime > screenshotCooldown) {
                    sendScreenshotNotification()
                    lastScreenshotTime = currentTime
                }
            }
        }
        contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        statusListener?.let { statusRef?.removeEventListener(it) }
        callListener?.let { callRef?.removeEventListener(it) }
        contentResolver.unregisterContentObserver(contentObserver)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}