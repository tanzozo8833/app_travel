package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles ORDER BY id ASC")
    fun getAllProfilesFlow(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE is_active = 1 LIMIT 1")
    fun getActiveProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<UserProfileEntity>)

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET is_active = CASE WHEN id = :activeId THEN 1 ELSE 0 END")
    suspend fun setActiveProfile(activeId: String)

    @Query("UPDATE user_profiles SET visited_provinces_count = :visitedCount, total_distance_km = total_distance_km + :distanceDelta WHERE id = :userId")
    suspend fun updateStatsAfterCheckIn(userId: String, visitedCount: Int, distanceDelta: Double)

    @Query("UPDATE user_profiles SET trips_completed_count = trips_completed_count + 1 WHERE id = :userId")
    suspend fun incrementTripsCompleted(userId: String)

    @Delete
    suspend fun delete(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE id = :userId")
    suspend fun deleteById(userId: String)

    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getCount(): Int

    @Query("DELETE FROM user_profiles")
    suspend fun deleteAll()
}
