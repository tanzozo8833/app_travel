package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.NotificationItem
import java.util.UUID

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["is_social"]),
        Index(value = ["is_read"]),
        Index(value = ["created_at"])
    ]
)
data class NotificationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "user_id")
    val userId: String = "u_1",

    @ColumnInfo(name = "is_social")
    val isSocial: Boolean = true,

    @ColumnInfo(name = "actor_name")
    val actorName: String? = null,

    @ColumnInfo(name = "actor_avatar_url")
    val actorAvatarUrl: String? = null,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "time_ago")
    val timeAgo: String,

    @ColumnInfo(name = "preview_url")
    val previewUrl: String? = null,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "action_type")
    val actionType: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): NotificationItem = NotificationItem(
        id = id,
        isSocial = isSocial,
        actorName = actorName,
        actorAvatarUrl = actorAvatarUrl,
        title = title,
        message = message,
        timeAgo = timeAgo,
        previewUrl = previewUrl,
        isRead = isRead,
        actionType = actionType
    )

    companion object {
        fun fromDomainModel(model: NotificationItem, userId: String = "u_1", createdAt: Long = System.currentTimeMillis()): NotificationEntity = NotificationEntity(
            id = model.id,
            userId = userId,
            isSocial = model.isSocial,
            actorName = model.actorName,
            actorAvatarUrl = model.actorAvatarUrl,
            title = model.title,
            message = model.message,
            timeAgo = model.timeAgo,
            previewUrl = model.previewUrl,
            isRead = model.isRead,
            actionType = model.actionType,
            createdAt = createdAt
        )
    }
}
