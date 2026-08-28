package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserFollowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserFollowDao {
    @Query("SELECT * FROM user_follows WHERE follower_id = :followerId ORDER BY created_at DESC")
    fun getFollowingsFlow(followerId: String): Flow<List<UserFollowEntity>>

    @Query("SELECT * FROM user_follows WHERE followed_id = :followedId ORDER BY created_at DESC")
    fun getFollowersFlow(followedId: String): Flow<List<UserFollowEntity>>

    @Query("SELECT COUNT(*) FROM user_follows WHERE follower_id = :followerId")
    fun getFollowingCountFlow(followerId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_follows WHERE followed_id = :followedId")
    fun getFollowerCountFlow(followedId: String): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM user_follows WHERE follower_id = :followerId AND (followed_id = :followedId OR (followed_handle != '' AND followed_handle = :followedHandle)))")
    fun isFollowingFlow(followerId: String, followedId: String, followedHandle: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM user_follows WHERE follower_id = :followerId AND (followed_id = :followedId OR (followed_handle != '' AND followed_handle = :followedHandle)))")
    suspend fun isFollowing(followerId: String, followedId: String, followedHandle: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(follow: UserFollowEntity)

    @Query("DELETE FROM user_follows WHERE follower_id = :followerId AND (followed_id = :followedId OR (followed_handle != '' AND followed_handle = :followedHandle))")
    suspend fun deleteFollow(followerId: String, followedId: String, followedHandle: String)

    @Query("SELECT COUNT(*) FROM user_follows WHERE follower_id = :followerId")
    suspend fun getFollowingCount(followerId: String): Int

    @Query("SELECT COUNT(*) FROM user_follows WHERE followed_id = :followedId")
    suspend fun getFollowerCount(followedId: String): Int

    @Query("DELETE FROM user_follows")
    suspend fun deleteAll()
}
