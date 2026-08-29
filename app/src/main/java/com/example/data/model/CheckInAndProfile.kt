package com.example.data.model

import java.util.UUID

data class CheckInRecord(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "u_1",
    val placeName: String,
    val provinceName: String,
    val provinceCode: String,
    val timestamp: String,
    val formattedDate: String,
    val method: CheckInMethod = CheckInMethod.QUICK,
    val photoUrl: String? = null,
    val framedPhotoUrl: String? = null,
    val frameId: String? = null,
    val caption: String? = null,
    val trustScore: Float = 1.0f,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

enum class CheckInMethod {
    QUICK,
    PHOTO
}

data class LuminaFrame(
    val id: String,
    val name: String,
    val badgeLabel: String,
    val subtitle: String,
    val topTitle: String,
    val bottomTagline: String,
    val previewColor: Long,
    val provinceCode: String? = null,
    val isSeasonal: Boolean = false
)

data class AchievementBadge(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val isEarned: Boolean = false,
    val progressCurrent: Int = 0,
    val progressMax: Int = 1,
    val earnedDate: String? = null
)

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val isSocial: Boolean = true, // true: Hoạt động (Likes, comments), false: Của bạn (Hành trình, geofence, badges)
    val actorName: String? = null,
    val actorAvatarUrl: String? = null,
    val title: String,
    val message: String,
    val timeAgo: String,
    val previewUrl: String? = null,
    val isRead: Boolean = false,
    val actionType: String? = null
)

data class UserProfile(
    val id: String = "u_1",
    val username: String = "linh.travels",
    val displayName: String = "Thảo Linh",
    val avatarUrl: String,
    val tagTitle: String = "GEN Z EXPLORER",
    val bio: String = "Gen Z Explorer | Săn hoàng hôn & Foodtour Việt Nam 🇻🇳 ✨",
    val topCityName: String = "Hà Nội",
    val topCityCheckInCount: Int = 4,
    val visitedProvincesCount: Int = 5,
    val totalProvincesCount: Int = 34,
    val totalDistanceKm: Double = 12500.0,
    val tripsCompletedCount: Int = 18,
    val totalSavesCount: Int = 3420,
    val isCustomName: Boolean = false,
    val isCustomAvatar: Boolean = false
)

data class UserFollow(
    val id: String,
    val followerId: String,
    val followedId: String,
    val createdAt: Long = System.currentTimeMillis()
)
