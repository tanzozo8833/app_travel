package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.LuminaFrame

@Entity(tableName = "frames")
data class LuminaFrameEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "badge_label")
    val badgeLabel: String,

    @ColumnInfo(name = "subtitle")
    val subtitle: String,

    @ColumnInfo(name = "top_title")
    val topTitle: String,

    @ColumnInfo(name = "bottom_tagline")
    val bottomTagline: String,

    @ColumnInfo(name = "preview_color")
    val previewColor: Long,

    @ColumnInfo(name = "province_code")
    val provinceCode: String? = null,

    @ColumnInfo(name = "is_seasonal")
    val isSeasonal: Boolean = false
) {
    fun toDomainModel(): LuminaFrame = LuminaFrame(
        id = id,
        name = name,
        badgeLabel = badgeLabel,
        subtitle = subtitle,
        topTitle = topTitle,
        bottomTagline = bottomTagline,
        previewColor = previewColor,
        provinceCode = provinceCode,
        isSeasonal = isSeasonal
    )

    companion object {
        fun fromDomainModel(model: LuminaFrame): LuminaFrameEntity = LuminaFrameEntity(
            id = model.id,
            name = model.name,
            badgeLabel = model.badgeLabel,
            subtitle = model.subtitle,
            topTitle = model.topTitle,
            bottomTagline = model.bottomTagline,
            previewColor = model.previewColor,
            provinceCode = model.provinceCode,
            isSeasonal = model.isSeasonal
        )
    }
}
