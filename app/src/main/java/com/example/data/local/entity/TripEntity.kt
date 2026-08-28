package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Trip
import com.example.data.model.TripDay
import com.example.data.model.TripStatus
import com.example.data.model.WeatherSummary
import java.util.UUID

@Entity(
    tableName = "trips",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["status"]),
        Index(value = ["province_code"])
    ]
)
data class TripEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "user_id")
    val userId: String = "u_1",

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "destination")
    val destination: String,

    @ColumnInfo(name = "province_code")
    val provinceCode: String,

    @ColumnInfo(name = "cover_url")
    val coverUrl: String,

    @ColumnInfo(name = "start_date")
    val startDate: String,

    @ColumnInfo(name = "end_date")
    val endDate: String,

    @ColumnInfo(name = "duration_text")
    val durationText: String,

    @ColumnInfo(name = "status")
    val status: TripStatus = TripStatus.UPCOMING,

    @ColumnInfo(name = "total_estimated_cost_vnd")
    val totalEstimatedCostVnd: Long = 2400000,

    @ColumnInfo(name = "spots_count")
    val spotsCount: Int = 8,

    @ColumnInfo(name = "group_type")
    val groupType: String = "Cặp đôi",

    @ColumnInfo(name = "style_tags")
    val styleTags: List<String> = listOf("Chill sương mù", "Sống ảo", "Cà phê"),

    @ColumnInfo(name = "is_ai_generated")
    val isAiGenerated: Boolean = true,

    @ColumnInfo(name = "weather_summary")
    val weatherSummary: WeatherSummary = WeatherSummary(),

    @ColumnInfo(name = "days")
    val days: List<TripDay> = emptyList(),

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Trip = Trip(
        id = id,
        userId = userId,
        title = title,
        destination = destination,
        provinceCode = provinceCode,
        coverUrl = coverUrl,
        startDate = startDate,
        endDate = endDate,
        durationText = durationText,
        status = status,
        totalEstimatedCostVnd = totalEstimatedCostVnd,
        spotsCount = spotsCount,
        groupType = groupType,
        styleTags = styleTags,
        isAiGenerated = isAiGenerated,
        weatherSummary = weatherSummary,
        days = days
    )

    companion object {
        fun fromDomainModel(model: Trip, userId: String = model.userId): TripEntity = TripEntity(
            id = model.id,
            userId = if (model.userId.isNotBlank()) model.userId else userId,
            title = model.title,
            destination = model.destination,
            provinceCode = model.provinceCode,
            coverUrl = model.coverUrl,
            startDate = model.startDate,
            endDate = model.endDate,
            durationText = model.durationText,
            status = model.status,
            totalEstimatedCostVnd = model.totalEstimatedCostVnd,
            spotsCount = model.spotsCount,
            groupType = model.groupType,
            styleTags = model.styleTags,
            isAiGenerated = model.isAiGenerated,
            weatherSummary = model.weatherSummary,
            days = model.days
        )
    }
}
