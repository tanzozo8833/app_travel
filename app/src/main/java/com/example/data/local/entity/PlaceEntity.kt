package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Place
import com.example.data.model.PlaceCategory

@Entity(
    tableName = "places",
    indices = [
        Index(value = ["province_id"]),
        Index(value = ["province_code"]),
        Index(value = ["category"]),
        Index(value = ["is_visited"])
    ]
)
data class PlaceEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "province_id")
    val provinceId: Int,

    @ColumnInfo(name = "province_code")
    val provinceCode: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: PlaceCategory,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "cover_url")
    val coverUrl: String,

    @ColumnInfo(name = "checkin_radius_meters")
    val checkinRadiusMeters: Int = 200,

    @ColumnInfo(name = "is_suggested")
    val isSuggested: Boolean = true,

    @ColumnInfo(name = "price_range_vnd")
    val priceRangeVnd: Long = 50000,

    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),

    @ColumnInfo(name = "rating")
    val rating: Double = 4.8,

    @ColumnInfo(name = "is_visited")
    val isVisited: Boolean = false,

    @ColumnInfo(name = "lat")
    val lat: Double = 0.0,

    @ColumnInfo(name = "lng")
    val lng: Double = 0.0
) {
    fun toDomainModel(): Place = Place(
        id = id,
        provinceId = provinceId,
        provinceCode = provinceCode,
        name = name,
        category = category,
        address = address,
        description = description,
        coverUrl = coverUrl,
        checkinRadiusMeters = checkinRadiusMeters,
        isSuggested = isSuggested,
        priceRangeVnd = priceRangeVnd,
        tags = tags,
        rating = rating,
        isVisited = isVisited
    )

    companion object {
        fun fromDomainModel(model: Place, lat: Double = 0.0, lng: Double = 0.0): PlaceEntity = PlaceEntity(
            id = model.id,
            provinceId = model.provinceId,
            provinceCode = model.provinceCode,
            name = model.name,
            category = model.category,
            address = model.address,
            description = model.description,
            coverUrl = model.coverUrl,
            checkinRadiusMeters = model.checkinRadiusMeters,
            isSuggested = model.isSuggested,
            priceRangeVnd = model.priceRangeVnd,
            tags = model.tags,
            rating = model.rating,
            isVisited = model.isVisited,
            lat = lat,
            lng = lng
        )
    }
}
