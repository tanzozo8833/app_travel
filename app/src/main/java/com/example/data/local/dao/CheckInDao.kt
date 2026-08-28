package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC")
    fun getAllCheckInsFlow(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getCheckInsByUserIdFlow(userId: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE province_code = :provinceCode ORDER BY timestamp DESC")
    fun getCheckInsByProvinceCodeFlow(provinceCode: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE is_in_city_vault = 1 ORDER BY timestamp DESC")
    fun getCityVaultFlow(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE id = :id LIMIT 1")
    suspend fun getCheckInById(id: String): CheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkIn: CheckInEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checkIns: List<CheckInEntity>)

    @Update
    suspend fun update(checkIn: CheckInEntity)

    @Query("UPDATE check_ins SET is_in_city_vault = :isInVault WHERE id = :id")
    suspend fun updateCityVaultStatus(id: String, isInVault: Boolean)

    @Delete
    suspend fun delete(checkIn: CheckInEntity)

    @Query("SELECT COUNT(DISTINCT province_code) FROM check_ins")
    fun getDistinctVisitedProvincesCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM check_ins")
    fun getTotalCheckInsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM check_ins")
    suspend fun getCount(): Int

    @Query("DELETE FROM check_ins WHERE user_id = :userId")
    suspend fun deleteCheckInsByUserId(userId: String)

    @Query("DELETE FROM check_ins")
    suspend fun deleteAll()
}
