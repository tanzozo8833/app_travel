package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE post_id = :postId ORDER BY created_at ASC")
    fun getCommentsForPostFlow(postId: String): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE post_id = :postId ORDER BY created_at ASC")
    suspend fun getCommentsForPost(postId: String): List<CommentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<CommentEntity>)

    @Update
    suspend fun update(comment: CommentEntity)

    @Query("UPDATE comments SET is_liked = :isLiked, like_count = :likeCount WHERE id = :id")
    suspend fun updateLikeStatus(id: String, isLiked: Boolean, likeCount: Int)

    @Delete
    suspend fun delete(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE post_id = :postId")
    suspend fun deleteCommentsForPost(postId: String)

    @Query("SELECT COUNT(*) FROM comments")
    suspend fun getCount(): Int

    @Query("DELETE FROM comments")
    suspend fun deleteAll()
}
