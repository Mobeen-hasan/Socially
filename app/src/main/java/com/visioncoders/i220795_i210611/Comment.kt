package com.visioncoders.i220795_i210611

import androidx.annotation.Keep

@Keep
data class Comment(
    var commentId: String = "",
    var postId: String = "",
    var userId: String = "",
    var text: String = "",
    var timestamp: Long = 0
)
