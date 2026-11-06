package com.visioncoders.i220795_i210611

import androidx.annotation.Keep

@Keep
data class Post(
    var postId: String = "",
    var userId: String = "",
    var imageData: String = "",
    var caption: String = "",
    var timestamp: Long = 0,
    // Use a map to store the UIDs of users who liked the post
    var likes: HashMap<String, Boolean> = HashMap()
)
