package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PlaceEntity
import com.example.data.model.PlaceCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places ORDER BY name ASC")
    fun getAllPlacesFlow(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places ORDER BY name ASC")
    suspend fun getAllPlaces(): List<PlaceEntity>

    @Query("SELECT * FROM places WHERE province_code = :provinceCode ORDER BY is_suggested DESC, rating DESC")
    fun getPlacesByProvinceCodeFlow(provinceCode: String): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE province_id = :provinceId ORDER BY is_suggested DESC, rating DESC")
    fun getPlacesByProvinceIdFlow(provinceId: Int): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE category = :category ORDER BY rating DESC")
    fun getPlacesByCategoryFlow(category: PlaceCategory): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    suspend fun getPlaceById(id: String): PlaceEntity?

    @Query("SELECT * FROM places WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchPlacesFlow(query: String): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchPlaces(query: String): List<PlaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<PlaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: PlaceEntity)

    @Update
    suspend fun update(place: PlaceEntity)

    @Query("UPDATE places SET is_visited = :isVisited WHERE id = :placeId")
    suspend fun updateVisitedStatus(placeId: String, isVisited: Boolean)

    @Query("UPDATE places SET is_visited = 1 WHERE name = :placeName OR province_code = :provinceCode AND name LIKE '%' || :placeName || '%'")
    suspend fun markAsVisitedByName(placeName: String, provinceCode: String)

    @Query("SELECT COUNT(*) FROM places")
    suspend fun getCount(): Int

    @Query("DELETE FROM places")
    suspend fun deleteAll()
}
