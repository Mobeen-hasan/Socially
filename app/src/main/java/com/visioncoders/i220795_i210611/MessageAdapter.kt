package com.visioncoders.i220795_i210611

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageAdapter(private val context: Context, private val messageList: List<Message>, private val chatId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_SCREENSHOT = 3
        private const val FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return when {
            message.isScreenshotNotification -> VIEW_TYPE_SCREENSHOT
            message.senderId == FirebaseAuth.getInstance().currentUser?.uid -> VIEW_TYPE_SENT
            else -> VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            else -> { // VIEW_TYPE_SCREENSHOT
                val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
                ScreenshotViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> {
                (holder as SentMessageViewHolder).bind(message)
                holder.itemView.setOnLongClickListener {
                    val timeDifference = System.currentTimeMillis() - message.timestamp
                    if (timeDifference <= FIVE_MINUTES_IN_MILLIS) {
                        showEditDeleteDialog(message)
                    }
                    true
                }
            }
            VIEW_TYPE_RECEIVED -> (holder as ReceivedMessageViewHolder).bind(message)
            VIEW_TYPE_SCREENSHOT -> (holder as ScreenshotViewHolder).bind(message)
        }
    }

    private fun showEditDeleteDialog(message: Message) {
        val options = arrayOf("Edit Message", "Delete Message")
        AlertDialog.Builder(context)
            .setTitle("Choose Action")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditDialog(message)
                    1 -> deleteMessage(message)
                }
            }
            .show()
    }

    private fun showEditDialog(message: Message) {
        val editText = EditText(context).apply { setText(message.text) }
        AlertDialog.Builder(context)
            .setTitle("Edit Message")
            .setView(editText)
            .setPositiveButton("Save") { dialog, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    editMessage(message, newText)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun editMessage(message: Message, newText: String) {
        val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId).child(message.messageId)
        messageRef.child("text").setValue(newText)
    }

    private fun deleteMessage(message: Message) {
        val messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId).child(message.messageId)
        messageRef.removeValue()
    }

    override fun getItemCount() = messageList.size

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text_sent)
        private val messageImage: ImageView = itemView.findViewById(R.id.message_image_sent)

        fun bind(message: Message) {
            if (message.text.isNotEmpty()) {
                messageText.visibility = View.VISIBLE
                messageText.text = message.text
            } else {
                messageText.visibility = View.GONE
            }

            if (message.imageData.isNotEmpty()) {
                messageImage.visibility = View.VISIBLE
                try {
                    val imageBytes = Base64.decode(message.imageData, Base64.DEFAULT)
                    messageImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                } catch (e: Exception) { /* Handle error */ }
            } else {
                messageImage.visibility = View.GONE
            }
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text_received)
        private val messageImage: ImageView = itemView.findViewById(R.id.message_image_received)

        fun bind(message: Message) {
            if (message.text.isNotEmpty()) {
                messageText.visibility = View.VISIBLE
                messageText.text = message.text
            } else {
                messageText.visibility = View.GONE
            }

            if (message.imageData.isNotEmpty()) {
                messageImage.visibility = View.VISIBLE
                try {
                    val imageBytes = Base64.decode(message.imageData, Base64.DEFAULT)
                    messageImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                } catch (e: Exception) { /* Handle error */ }
            } else {
                messageImage.visibility = View.GONE
            }
        }
    }

    class ScreenshotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)
        fun bind(message: Message) {
            val text = if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                "You took a screenshot"
            } else {
                "Someone took a screenshot"
            }
            textView.text = text
            itemView.setBackgroundColor(0x80FFDDDD.toInt()) // Correctly set background on the item view
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
    }
}