package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Comment
import java.util.UUID

@Entity(
    tableName = "comments",
    indices = [
        Index(value = ["post_id"]),
        Index(value = ["created_at"])
    ]
)
data class CommentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "post_id")
    val postId: String,

    @ColumnInfo(name = "author_name")
    val authorName: String,

    @ColumnInfo(name = "author_handle")
    val authorHandle: String,

    @ColumnInfo(name = "author_avatar_url")
    val authorAvatarUrl: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "time_ago")
    val timeAgo: String = "Vừa xong",

    @ColumnInfo(name = "like_count")
    val likeCount: Int = 0,

    @ColumnInfo(name = "is_liked")
    val isLiked: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Comment = Comment(
        id = id,
        postId = postId,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        content = content,
        timeAgo = timeAgo,
        likeCount = likeCount,
        isLiked = isLiked,
        replies = emptyList()
    )

    companion object {
        fun fromDomainModel(model: Comment, createdAt: Long = System.currentTimeMillis()): CommentEntity = CommentEntity(
            id = model.id,
            postId = model.postId,
            authorName = model.authorName,
            authorHandle = model.authorHandle,
            authorAvatarUrl = model.authorAvatarUrl,
            content = model.content,
            timeAgo = model.timeAgo,
            likeCount = model.likeCount,
            isLiked = model.isLiked,
            createdAt = createdAt
        )
    }
}
