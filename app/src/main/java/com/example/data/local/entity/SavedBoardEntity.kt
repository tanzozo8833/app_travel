package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.SavedBoard
import java.util.UUID

@Entity(
    tableName = "saved_boards",
    indices = [
        Index(value = ["user_id"])
    ]
)
data class SavedBoardEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "user_id")
    val userId: String = "u_1",

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "cover_url")
    val coverUrl: String,

    @ColumnInfo(name = "post_count")
    val postCount: Int = 0,

    @ColumnInfo(name = "is_private")
    val isPrivate: Boolean = true
) {
    fun toDomainModel(): SavedBoard = SavedBoard(
        id = id,
        name = name,
        coverUrl = coverUrl,
        postCount = postCount,
        isPrivate = isPrivate
    )

    companion object {
        fun fromDomainModel(model: SavedBoard, userId: String = "u_1"): SavedBoardEntity = SavedBoardEntity(
            id = model.id,
            userId = userId,
            name = model.name,
            coverUrl = model.coverUrl,
            postCount = model.postCount,
            isPrivate = model.isPrivate
        )
    }
}
