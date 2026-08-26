package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AchievementBadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementBadgeDao {
    @Query("SELECT * FROM badges ORDER BY is_earned DESC, id ASC")
    fun getAllBadgesFlow(): Flow<List<AchievementBadgeEntity>>

    @Query("SELECT * FROM badges ORDER BY is_earned DESC, id ASC")
    suspend fun getAllBadges(): List<AchievementBadgeEntity>

    @Query("SELECT * FROM badges WHERE is_earned = 1")
    fun getEarnedBadgesFlow(): Flow<List<AchievementBadgeEntity>>

    @Query("SELECT * FROM badges WHERE code = :code LIMIT 1")
    suspend fun getBadgeByCode(code: String): AchievementBadgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(badges: List<AchievementBadgeEntity>)

    @Update
    suspend fun update(badge: AchievementBadgeEntity)

    @Query("UPDATE badges SET is_earned = :isEarned, progress_current = :progress, earned_date = :earnedDate WHERE code = :code")
    suspend fun updateProgress(code: String, progress: Int, isEarned: Boolean, earnedDate: String?)

    @Query("SELECT COUNT(*) FROM badges WHERE is_earned = 1")
    fun getEarnedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM badges")
    suspend fun getCount(): Int

    @Query("DELETE FROM badges")
    suspend fun deleteAll()
}
