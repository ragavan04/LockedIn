package com.example.lockedin.models

data class Member(
    val userId: String = "",
    val username : String = "",
    val profilePic: String = "",
    val role: String = "",
    val joinedAt: Long = 0L,
)
