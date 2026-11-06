package com.visioncoders.i220795_i210611

import androidx.annotation.Keep

@Keep
data class Story(
    var storyId: String = "",
    var userId: String = "",
    var imageData: String = "",
    var timestamp: Long = 0
)
