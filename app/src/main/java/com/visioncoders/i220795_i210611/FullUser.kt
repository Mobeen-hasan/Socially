package com.visioncoders.i220795_i210611

import androidx.annotation.Keep

@Keep
data class FullUser(
    var uid: String = "",
    var username: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var dob: String = "",
    var email: String = "",
    var profilePictureData: String = "", // For Base64 profile image
    var followers: HashMap<String, Boolean> = HashMap(),
    var following: HashMap<String, Boolean> = HashMap(),
    var followRequests: HashMap<String, Boolean> = HashMap(), // Incoming requests
    var fcmToken: String = "" // New field for FCM token
)
