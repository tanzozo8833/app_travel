package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PendingUploadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingUploadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: PendingUploadEntity)

    /** Cu nhat truoc: bai soan truoc thi len bang tin truoc. */
    @Query("SELECT * FROM pending_uploads ORDER BY created_at ASC")
    suspend fun getAll(): List<PendingUploadEntity>

    @Query("SELECT * FROM pending_uploads ORDER BY created_at ASC")
    fun observeAll(): Flow<List<PendingUploadEntity>>

    @Query("SELECT COUNT(*) FROM pending_uploads")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM pending_uploads WHERE post_id = :postId LIMIT 1")
    suspend fun getById(postId: String): PendingUploadEntity?

    @Query("UPDATE pending_uploads SET attempt_count = attempt_count + 1, last_error = :error WHERE post_id = :postId")
    suspend fun markAttemptFailed(postId: String, error: String)

    @Query("DELETE FROM pending_uploads WHERE post_id = :postId")
    suspend fun remove(postId: String)

    @Delete
    suspend fun delete(item: PendingUploadEntity)
}
