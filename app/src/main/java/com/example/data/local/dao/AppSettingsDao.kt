package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT setting_value FROM app_settings WHERE setting_key = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<String?>

    @Query("SELECT setting_value FROM app_settings WHERE setting_key = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingsEntity)

    @Query("DELETE FROM app_settings WHERE setting_key = :key")
    suspend fun deleteSetting(key: String)
}
