package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "user_follows",
    indices = [
        Index(value = ["follower_id", "followed_id"], unique = true),
        Index(value = ["follower_id"]),
        Index(value = ["followed_id"]),
        Index(value = ["followed_handle"]),
        Index(value = ["created_at"])
    ]
)
data class UserFollowEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "follower_id")
    val followerId: String,

    @ColumnInfo(name = "followed_id")
    val followedId: String,

    @ColumnInfo(name = "followed_handle")
    val followedHandle: String = "",

    @ColumnInfo(name = "followed_name")
    val followedName: String = "",

    @ColumnInfo(name = "followed_avatar_url")
    val followedAvatarUrl: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
