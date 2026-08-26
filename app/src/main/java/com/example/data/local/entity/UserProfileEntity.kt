package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.UserProfile

@Entity(
    tableName = "user_profiles",
    indices = [
        Index(value = ["username"], unique = true)
    ]
)
data class UserProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "u_1",

    @ColumnInfo(name = "username")
    val username: String = "linh.travels",

    @ColumnInfo(name = "display_name")
    val displayName: String = "Thảo Linh",

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String,

    @ColumnInfo(name = "tag_title")
    val tagTitle: String = "GEN Z EXPLORER",

    @ColumnInfo(name = "bio")
    val bio: String = "Gen Z Explorer | Săn hoàng hôn & Foodtour Việt Nam 🇻🇳 ✨",

    @ColumnInfo(name = "top_city_name")
    val topCityName: String = "Hà Nội",

    @ColumnInfo(name = "top_city_check_in_count")
    val topCityCheckInCount: Int = 4,

    @ColumnInfo(name = "visited_provinces_count")
    val visitedProvincesCount: Int = 5,

    @ColumnInfo(name = "total_provinces_count")
    val totalProvincesCount: Int = 34,

    @ColumnInfo(name = "total_distance_km")
    val totalDistanceKm: Double = 12500.0,

    @ColumnInfo(name = "trips_completed_count")
    val tripsCompletedCount: Int = 18,

    @ColumnInfo(name = "total_saves_count")
    val totalSavesCount: Int = 3420,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false
) {
    fun toDomainModel(): UserProfile = UserProfile(
        id = id,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        tagTitle = tagTitle,
        bio = bio,
        topCityName = topCityName,
        topCityCheckInCount = topCityCheckInCount,
        visitedProvincesCount = visitedProvincesCount,
        totalProvincesCount = totalProvincesCount,
        totalDistanceKm = totalDistanceKm,
        tripsCompletedCount = tripsCompletedCount,
        totalSavesCount = totalSavesCount
    )

    companion object {
        fun fromDomainModel(model: UserProfile, isActive: Boolean = false): UserProfileEntity = UserProfileEntity(
            id = model.id,
            username = model.username,
            displayName = model.displayName,
            avatarUrl = model.avatarUrl,
            tagTitle = model.tagTitle,
            bio = model.bio,
            topCityName = model.topCityName,
            topCityCheckInCount = model.topCityCheckInCount,
            visitedProvincesCount = model.visitedProvincesCount,
            totalProvincesCount = model.totalProvincesCount,
            totalDistanceKm = model.totalDistanceKm,
            tripsCompletedCount = model.tripsCompletedCount,
            totalSavesCount = model.totalSavesCount,
            isActive = isActive
        )
    }
}
