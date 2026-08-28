package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.CheckInMethod
import com.example.data.model.CheckInRecord
import java.util.UUID

@Entity(
    tableName = "check_ins",
    indices = [
        Index(value = ["province_code"]),
        Index(value = ["place_name"]),
        Index(value = ["user_id"])
    ]
)
data class CheckInEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "user_id")
    val userId: String = "u_1",

    @ColumnInfo(name = "place_name")
    val placeName: String,

    @ColumnInfo(name = "province_name")
    val provinceName: String,

    @ColumnInfo(name = "province_code")
    val provinceCode: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: String,

    @ColumnInfo(name = "formatted_date")
    val formattedDate: String,

    @ColumnInfo(name = "method")
    val method: CheckInMethod = CheckInMethod.QUICK,

    @ColumnInfo(name = "photo_url")
    val photoUrl: String? = null,

    @ColumnInfo(name = "framed_photo_url")
    val framedPhotoUrl: String? = null,

    @ColumnInfo(name = "frame_id")
    val frameId: String? = null,

    @ColumnInfo(name = "caption")
    val caption: String? = null,

    @ColumnInfo(name = "trust_score")
    val trustScore: Float = 1.0f,

    @ColumnInfo(name = "lat")
    val lat: Double = 0.0,

    @ColumnInfo(name = "lng")
    val lng: Double = 0.0,

    @ColumnInfo(name = "is_in_city_vault")
    val isInCityVault: Boolean = false
) {
    fun toDomainModel(): CheckInRecord = CheckInRecord(
        id = id,
        userId = userId,
        placeName = placeName,
        provinceName = provinceName,
        provinceCode = provinceCode,
        timestamp = timestamp,
        formattedDate = formattedDate,
        method = method,
        photoUrl = photoUrl,
        framedPhotoUrl = framedPhotoUrl,
        frameId = frameId,
        caption = caption,
        trustScore = trustScore,
        lat = lat,
        lng = lng
    )

    companion object {
        fun fromDomainModel(model: CheckInRecord, userId: String = model.userId, isInVault: Boolean = false): CheckInEntity = CheckInEntity(
            id = model.id,
            userId = if (model.userId.isNotBlank()) model.userId else userId,
            placeName = model.placeName,
            provinceName = model.provinceName,
            provinceCode = model.provinceCode,
            timestamp = model.timestamp,
            formattedDate = model.formattedDate,
            method = model.method,
            photoUrl = model.photoUrl,
            framedPhotoUrl = model.framedPhotoUrl,
            frameId = model.frameId,
            caption = model.caption,
            trustScore = model.trustScore,
            lat = model.lat,
            lng = model.lng,
            isInCityVault = isInVault
        )
    }
}
