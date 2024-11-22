package com.example.lockedin.models

import android.media.Image

data class UserCommunity(
    var id: String =  "String",
    var points: Int = 0,
    var streak: Int = 0,
    var consistency: Float = 0f
)
