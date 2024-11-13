package com.example.lockedin.models

data class User(
    val username: String = "",
    val points: Int = 0,
    var communities: List<UserCommunity> =  emptyList()
)
