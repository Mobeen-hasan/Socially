package com.visioncoders.i220795_i210611

import androidx.annotation.Keep

@Keep
data class Chat(
    var chatId: String = "",
    var participants: List<String> = emptyList(),
    var lastMessage: String = "",
    var lastMessageTimestamp: Long = 0
)
