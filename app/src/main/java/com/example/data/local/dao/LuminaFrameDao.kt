package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.LuminaFrameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LuminaFrameDao {
    @Query("SELECT * FROM frames ORDER BY is_seasonal DESC, id ASC")
    fun getAllFramesFlow(): Flow<List<LuminaFrameEntity>>

    @Query("SELECT * FROM frames ORDER BY is_seasonal DESC, id ASC")
    suspend fun getAllFrames(): List<LuminaFrameEntity>

    @Query("SELECT * FROM frames WHERE province_code = :provinceCode LIMIT 1")
    suspend fun getFrameByProvinceCode(provinceCode: String): LuminaFrameEntity?

    @Query("SELECT * FROM frames WHERE id = :id LIMIT 1")
    suspend fun getFrameById(id: String): LuminaFrameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(frames: List<LuminaFrameEntity>)

    @Query("SELECT COUNT(*) FROM frames")
    suspend fun getCount(): Int

    @Query("DELETE FROM frames")
    suspend fun deleteAll()
}
