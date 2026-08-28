package com.example.data.model

import java.util.UUID

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "u_1",
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String,
    val hasRecentCheckIn: Boolean = false, // Shows gold ring on avatar
    val locationLabel: String,
    val provinceCode: String = "LDG",
    val timeAgo: String = "2 giờ trước",
    val caption: String = "",
    val mediaUrls: List<String> = emptyList(),
    val aspectRatio: Float = 0.8f, // w / h for masonry layout
    val attachedTrip: Trip? = null,
    var likeCount: Int = 150,
    var commentCount: Int = 24,
    var saveCount: Int = 45,
    var shareCount: Int = 18,
    var isLiked: Boolean = false,
    var isSaved: Boolean = false,
    val tags: List<String> = emptyList()
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val userId: String = "u_1",
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String,
    val content: String,
    val timeAgo: String = "Vừa xong",
    var likeCount: Int = 0,
    var isLiked: Boolean = false,
    val replies: List<Comment> = emptyList()
)

data class SavedBoard(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val coverUrl: String,
    val postCount: Int = 0,
    val isPrivate: Boolean = true
)
