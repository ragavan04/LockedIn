package com.example.lockedin.models

data class User(
    var userID: String = "",
    var username: String = "",
    var points: Int = 0,
    var consistency: Float = 0f,
    var communities: List<UserCommunity> =  emptyList(),
    var posts: List<Post> =  emptyList()
)
