package com.visioncoders.i220795_i210611

import androidx.annotation.Keep

@Keep
data class Message(
    var messageId: String = "",
    var senderId: String = "",
    var text: String = "",
    var imageData: String = "", // For Base64 image
    var timestamp: Long = 0,
    var isScreenshotNotification: Boolean = false // New flag
)
