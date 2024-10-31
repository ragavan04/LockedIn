package com.example.lockedin.models

data class Member(
    val userId: String = "",
    val role: String = "",
    val joinedAt: Long = 0L,
)
