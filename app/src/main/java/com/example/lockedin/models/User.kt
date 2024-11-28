package com.example.lockedin.models

data class User(
    var userID: String = "",
    var username: String = "",
    var bio: String = "",
    var profilePic : String = "",
    var points: Int = 0,
    var communities: List<UserCommunity> =  emptyList(),
    var consistency: Float = 0f,
    var posts: List<Post> =  emptyList()
)
