package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Post
import com.example.data.model.Trip
import java.util.UUID

@Entity(
    tableName = "posts",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["province_code"]),
        Index(value = ["created_at"])
    ]
)
data class PostEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "user_id")
    val userId: String = "u_1",

    @ColumnInfo(name = "author_name")
    val authorName: String,

    @ColumnInfo(name = "author_handle")
    val authorHandle: String,

    @ColumnInfo(name = "author_avatar_url")
    val authorAvatarUrl: String,

    @ColumnInfo(name = "has_recent_check_in")
    val hasRecentCheckIn: Boolean = false,

    @ColumnInfo(name = "location_label")
    val locationLabel: String,

    @ColumnInfo(name = "province_code")
    val provinceCode: String = "LDG",

    @ColumnInfo(name = "time_ago")
    val timeAgo: String = "2 giờ trước",

    @ColumnInfo(name = "caption")
    val caption: String = "",

    @ColumnInfo(name = "media_urls")
    val mediaUrls: List<String> = emptyList(),

    @ColumnInfo(name = "aspect_ratio")
    val aspectRatio: Float = 0.8f,

    @ColumnInfo(name = "attached_trip_id")
    val attachedTripId: String? = null,

    @ColumnInfo(name = "like_count")
    val likeCount: Int = 150,

    @ColumnInfo(name = "comment_count")
    val commentCount: Int = 24,

    @ColumnInfo(name = "save_count")
    val saveCount: Int = 45,

    @ColumnInfo(name = "share_count")
    val shareCount: Int = 18,

    @ColumnInfo(name = "is_liked")
    val isLiked: Boolean = false,

    @ColumnInfo(name = "is_saved")
    val isSaved: Boolean = false,

    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(attachedTrip: Trip? = null): Post = Post(
        id = id,
        authorName = authorName,
        authorHandle = authorHandle,
        authorAvatarUrl = authorAvatarUrl,
        hasRecentCheckIn = hasRecentCheckIn,
        locationLabel = locationLabel,
        provinceCode = provinceCode,
        timeAgo = timeAgo,
        caption = caption,
        mediaUrls = mediaUrls,
        aspectRatio = aspectRatio,
        attachedTrip = attachedTrip,
        likeCount = likeCount,
        commentCount = commentCount,
        saveCount = saveCount,
        shareCount = shareCount,
        isLiked = isLiked,
        isSaved = isSaved,
        tags = tags
    )

    companion object {
        fun fromDomainModel(model: Post, userId: String = "u_1", createdAt: Long = System.currentTimeMillis()): PostEntity = PostEntity(
            id = model.id,
            userId = userId,
            authorName = model.authorName,
            authorHandle = model.authorHandle,
            authorAvatarUrl = model.authorAvatarUrl,
            hasRecentCheckIn = model.hasRecentCheckIn,
            locationLabel = model.locationLabel,
            provinceCode = model.provinceCode,
            timeAgo = model.timeAgo,
            caption = model.caption,
            mediaUrls = model.mediaUrls,
            aspectRatio = model.aspectRatio,
            attachedTripId = model.attachedTrip?.id,
            likeCount = model.likeCount,
            commentCount = model.commentCount,
            saveCount = model.saveCount,
            shareCount = model.shareCount,
            isLiked = model.isLiked,
            isSaved = model.isSaved,
            tags = model.tags,
            createdAt = createdAt
        )
    }
}
