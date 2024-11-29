package com.example.lockedin.models

import android.media.Image

data class Community(
    val communityImage: String =  "String",
    val createdAt: Long =  0L,
    val description: String =  "String",
    val name: String =  "String",
    val ownerId: String =  "String",
    var memberId: List<Member> =  emptyList(),
    var bannedUsers: List<User> = emptyList(),
    var posts: List<Post> = emptyList(),
    var id: String =  "String",
    var notificationTime: String = ""
)
