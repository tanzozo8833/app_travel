package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ProvinceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProvinceDao {
    @Query("SELECT * FROM provinces ORDER BY id ASC")
    fun getAllProvincesFlow(): Flow<List<ProvinceEntity>>

    @Query("SELECT * FROM provinces ORDER BY id ASC")
    suspend fun getAllProvinces(): List<ProvinceEntity>

    @Query("SELECT * FROM provinces WHERE code = :code LIMIT 1")
    suspend fun getProvinceByCode(code: String): ProvinceEntity?

    @Query("SELECT * FROM provinces WHERE id = :id LIMIT 1")
    suspend fun getProvinceById(id: Int): ProvinceEntity?

    @Query("SELECT * FROM provinces WHERE region = :region ORDER BY id ASC")
    fun getProvincesByRegionFlow(region: String): Flow<List<ProvinceEntity>>

    @Query("SELECT * FROM provinces WHERE visited_places_count > 0 ORDER BY visited_places_count DESC")
    fun getVisitedProvincesFlow(): Flow<List<ProvinceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(provinces: List<ProvinceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(province: ProvinceEntity)

    @Update
    suspend fun update(province: ProvinceEntity)

    @Query("UPDATE provinces SET visited_places_count = :visitedCount, is_completed = :isCompleted WHERE code = :provinceCode")
    suspend fun updateProgress(provinceCode: String, visitedCount: Int, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM provinces WHERE visited_places_count > 0")
    fun getVisitedProvincesCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM provinces")
    suspend fun getCount(): Int

    @Query("DELETE FROM provinces")
    suspend fun deleteAll()
}
