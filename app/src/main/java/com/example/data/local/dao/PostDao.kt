package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY created_at DESC")
    fun getAllPostsFlow(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE user_id = :userId ORDER BY created_at DESC")
    fun getPostsByUserIdFlow(userId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE province_code = :provinceCode ORDER BY created_at DESC")
    fun getPostsByProvinceCodeFlow(provinceCode: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE is_saved = 1 ORDER BY created_at DESC")
    fun getSavedPostsFlow(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: String): PostEntity?

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    fun getPostByIdFlow(id: String): Flow<PostEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    @Update
    suspend fun update(post: PostEntity)

    @Query("UPDATE posts SET is_liked = :isLiked, like_count = :likeCount WHERE id = :postId")
    suspend fun updateLikeStatus(postId: String, isLiked: Boolean, likeCount: Int)

    @Query("UPDATE posts SET is_saved = :isSaved, save_count = :saveCount WHERE id = :postId")
    suspend fun updateSaveStatus(postId: String, isSaved: Boolean, saveCount: Int)

    @Query("UPDATE posts SET share_count = share_count + 1 WHERE id = :postId")
    suspend fun incrementShareCount(postId: String)

    @Query("UPDATE posts SET comment_count = comment_count + 1 WHERE id = :postId")
    suspend fun incrementCommentCount(postId: String)

    @Delete
    suspend fun delete(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getCount(): Int

    @Query("DELETE FROM posts")
    suspend fun deleteAll()
}
