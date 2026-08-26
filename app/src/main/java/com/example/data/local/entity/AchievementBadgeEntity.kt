package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.AchievementBadge

@Entity(
    tableName = "badges",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["is_earned"])
    ]
)
data class AchievementBadgeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "icon_emoji")
    val iconEmoji: String,

    @ColumnInfo(name = "is_earned")
    val isEarned: Boolean = false,

    @ColumnInfo(name = "progress_current")
    val progressCurrent: Int = 0,

    @ColumnInfo(name = "progress_max")
    val progressMax: Int = 1,

    @ColumnInfo(name = "earned_date")
    val earnedDate: String? = null
) {
    fun toDomainModel(): AchievementBadge = AchievementBadge(
        id = id,
        code = code,
        name = name,
        description = description,
        iconEmoji = iconEmoji,
        isEarned = isEarned,
        progressCurrent = progressCurrent,
        progressMax = progressMax,
        earnedDate = earnedDate
    )

    companion object {
        fun fromDomainModel(model: AchievementBadge): AchievementBadgeEntity = AchievementBadgeEntity(
            id = model.id,
            code = model.code,
            name = model.name,
            description = model.description,
            iconEmoji = model.iconEmoji,
            isEarned = model.isEarned,
            progressCurrent = model.progressCurrent,
            progressMax = model.progressMax,
            earnedDate = model.earnedDate
        )
    }
}
