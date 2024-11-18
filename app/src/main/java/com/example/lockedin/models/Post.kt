package com.example.lockedin.models

data class Post(
    val postId: String = "",
    val imageURL: String = "",
    val timePosted: Long = 0L,
    val userId: String = "",
    val username: String = "",
    val points: Int? = 0,
)
