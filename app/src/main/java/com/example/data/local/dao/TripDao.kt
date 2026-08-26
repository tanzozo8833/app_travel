package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TripEntity
import com.example.data.model.TripStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY created_at DESC")
    fun getAllTripsFlow(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE user_id = :userId ORDER BY created_at DESC")
    fun getTripsByUserIdFlow(userId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE status = :status ORDER BY created_at DESC")
    fun getTripsByStatusFlow(status: TripStatus): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTripById(id: String): TripEntity?

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    fun getTripByIdFlow(id: String): Flow<TripEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: TripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trips: List<TripEntity>)

    @Update
    suspend fun update(trip: TripEntity)

    @Query("UPDATE trips SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: TripStatus)

    @Delete
    suspend fun delete(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM trips")
    suspend fun getCount(): Int

    @Query("DELETE FROM trips")
    suspend fun deleteAll()
}
