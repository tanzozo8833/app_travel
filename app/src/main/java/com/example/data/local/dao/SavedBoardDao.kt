package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SavedBoardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedBoardDao {
    @Query("SELECT * FROM saved_boards ORDER BY id ASC")
    fun getAllSavedBoardsFlow(): Flow<List<SavedBoardEntity>>

    @Query("SELECT * FROM saved_boards WHERE user_id = :userId ORDER BY id ASC")
    fun getSavedBoardsByUserIdFlow(userId: String): Flow<List<SavedBoardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(board: SavedBoardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(boards: List<SavedBoardEntity>)

    @Update
    suspend fun update(board: SavedBoardEntity)

    @Query("UPDATE saved_boards SET post_count = post_count + 1 WHERE id = :id")
    suspend fun incrementPostCount(id: String)

    @Delete
    suspend fun delete(board: SavedBoardEntity)

    @Query("DELETE FROM saved_boards WHERE user_id = :userId")
    suspend fun deleteSavedBoardsByUserId(userId: String)

    @Query("SELECT COUNT(*) FROM saved_boards")
    suspend fun getCount(): Int

    @Query("DELETE FROM saved_boards")
    suspend fun deleteAll()
}
