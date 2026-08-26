package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Province

@Entity(
    tableName = "provinces",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["region"])
    ]
)
data class ProvinceEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "short_name")
    val shortName: String,

    @ColumnInfo(name = "region")
    val region: String,

    @ColumnInfo(name = "center_lat")
    val centerLat: Double,

    @ColumnInfo(name = "center_lng")
    val centerLng: Double,

    @ColumnInfo(name = "cover_url")
    val coverUrl: String,

    @ColumnInfo(name = "stamp_symbol")
    val stampSymbol: String,

    @ColumnInfo(name = "highlight_description")
    val highlightDescription: String,

    @ColumnInfo(name = "suggested_places_count")
    val suggestedPlacesCount: Int = 5,

    @ColumnInfo(name = "visited_places_count")
    val visitedPlacesCount: Int = 0,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false
) {
    fun toDomainModel(): Province = Province(
        id = id,
        code = code,
        name = name,
        shortName = shortName,
        region = region,
        centerLat = centerLat,
        centerLng = centerLng,
        coverUrl = coverUrl,
        stampSymbol = stampSymbol,
        highlightDescription = highlightDescription,
        suggestedPlacesCount = suggestedPlacesCount,
        visitedPlacesCount = visitedPlacesCount,
        isCompleted = isCompleted
    )

    companion object {
        fun fromDomainModel(model: Province): ProvinceEntity = ProvinceEntity(
            id = model.id,
            code = model.code,
            name = model.name,
            shortName = model.shortName,
            region = model.region,
            centerLat = model.centerLat,
            centerLng = model.centerLng,
            coverUrl = model.coverUrl,
            stampSymbol = model.stampSymbol,
            highlightDescription = model.highlightDescription,
            suggestedPlacesCount = model.suggestedPlacesCount,
            visitedPlacesCount = model.visitedPlacesCount,
            isCompleted = model.isCompleted
        )
    }
}
