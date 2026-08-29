package com.example.data.remote

import android.util.Log
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.Trip
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestorePostService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FirestorePostService"
        private const val COLLECTION_POSTS = "posts"
        private const val SUBCOLLECTION_COMMENTS = "comments"
        private const val SUBCOLLECTION_LIKES = "likes"
        private const val SUBCOLLECTION_SAVES = "saves"
        const val DEFAULT_PAGE_SIZE = 10
    }

    private val postsCollection = firestore.collection(COLLECTION_POSTS)

    /**
     * Converts a Firestore DocumentSnapshot to domain Post model.
     */
    fun documentToPost(
        doc: DocumentSnapshot,
        userLikedPostIds: Set<String> = emptySet(),
        userSavedPostIds: Set<String> = emptySet()
    ): Post? {
        if (!doc.exists()) return null
        return try {
            val id = doc.getString("id") ?: doc.id
            val userId = doc.getString("userId") ?: ""
            val authorName = doc.getString("authorName") ?: "Lữ khách Levart"
            val authorHandle = doc.getString("authorHandle") ?: "traveler"
            val authorAvatarUrl = doc.getString("authorAvatarUrl") ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
            val hasRecentCheckIn = doc.getBoolean("hasRecentCheckIn") ?: false
            val locationLabel = doc.getString("locationLabel") ?: ""
            val provinceCode = doc.getString("provinceCode") ?: "LDG"
            val caption = doc.getString("caption") ?: ""

            @Suppress("UNCHECKED_CAST")
            val mediaUrls = (doc.get("mediaUrls") as? List<String>) ?: emptyList()

            val aspectRatio = doc.getDouble("aspectRatio")?.toFloat() ?: 0.8f
            val likeCount = (doc.getLong("likeCount") ?: 0L).toInt().coerceAtLeast(0)
            val commentCount = (doc.getLong("commentCount") ?: 0L).toInt().coerceAtLeast(0)
            val saveCount = (doc.getLong("saveCount") ?: 0L).toInt().coerceAtLeast(0)
            val shareCount = (doc.getLong("shareCount") ?: 0L).toInt().coerceAtLeast(0)

            @Suppress("UNCHECKED_CAST")
            val tags = (doc.get("tags") as? List<String>) ?: emptyList()
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val timeAgo = calculateTimeAgo(createdAt)

            val isLiked = id in userLikedPostIds
            val isSaved = id in userSavedPostIds

            Post(
                id = id,
                userId = userId,
                authorName = authorName,
                authorHandle = authorHandle,
                authorAvatarUrl = authorAvatarUrl,
                hasRecentCheckIn = hasRecentCheckIn,
                locationLabel = locationLabel,
                provinceCode = provinceCode,
                timeAgo = timeAgo,
                caption = caption,
                mediaUrls = mediaUrls,
                aspectRatio = aspectRatio,
                attachedTrip = null,
                likeCount = likeCount,
                commentCount = commentCount,
                saveCount = saveCount,
                shareCount = shareCount,
                isLiked = isLiked,
                isSaved = isSaved,
                tags = tags
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping doc ${doc.id} to Post", e)
            null
        }
    }

    /**
     * Converts a Firestore DocumentSnapshot to domain Comment model.
     */
    fun documentToComment(doc: DocumentSnapshot, currentUserId: String = ""): Comment? {
        if (!doc.exists()) return null
        return try {
            val id = doc.getString("id") ?: doc.id
            val postId = doc.getString("postId") ?: ""
            val userId = doc.getString("userId") ?: ""
            val authorName = doc.getString("authorName") ?: "Lữ khách Levart"
            val authorHandle = doc.getString("authorHandle") ?: "traveler"
            val authorAvatarUrl = doc.getString("authorAvatarUrl") ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
            val content = doc.getString("content") ?: ""
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val likeCount = (doc.getLong("likeCount") ?: 0L).toInt().coerceAtLeast(0)
            val timeAgo = calculateTimeAgo(createdAt)

            Comment(
                id = id,
                postId = postId,
                userId = userId,
                authorName = authorName,
                authorHandle = authorHandle,
                authorAvatarUrl = authorAvatarUrl,
                content = content,
                timeAgo = timeAgo,
                likeCount = likeCount,
                isLiked = false,
                replies = emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping doc ${doc.id} to Comment", e)
            null
        }
    }

    /**
     * Fetches a paginated page of posts ordered by newest first.
     */
    suspend fun fetchPostsPage(
        pageSize: Int = DEFAULT_PAGE_SIZE,
        lastVisibleDoc: DocumentSnapshot? = null
    ): Pair<List<DocumentSnapshot>, Boolean> {
        var query = postsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(pageSize.toLong())

        if (lastVisibleDoc != null) {
            query = query.startAfter(lastVisibleDoc)
        }

        val snapshot = query.get().await()
        val docs = snapshot.documents
        val hasMore = docs.size >= pageSize
        return Pair(docs, hasMore)
    }

    /**
     * Real-time Flow observing recent posts from Firestore.
     */
    fun observeRecentPosts(limit: Int = 20): Flow<List<DocumentSnapshot>> = callbackFlow {
        val registration = postsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for posts feed", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Real-time Flow observing comments for a specific post.
     */
    fun observeCommentsForPost(postId: String): Flow<List<Comment>> = callbackFlow {
        if (postId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = postsCollection.document(postId)
            .collection(SUBCOLLECTION_COMMENTS)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for comments on post $postId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        documentToComment(doc)
                    }
                    trySend(comments)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Checks if current user has liked a specific post.
     */
    suspend fun isPostLikedByUser(postId: String, userId: String): Boolean {
        if (postId.isBlank() || userId.isBlank()) return false
        return try {
            val doc = postsCollection.document(postId)
                .collection(SUBCOLLECTION_LIKES)
                .document(userId)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if current user has saved a specific post.
     */
    suspend fun isPostSavedByUser(postId: String, userId: String): Boolean {
        if (postId.isBlank() || userId.isBlank()) return false
        return try {
            val doc = postsCollection.document(postId)
                .collection(SUBCOLLECTION_SAVES)
                .document(userId)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Creates a new post on Cloud Firestore.
     */
    suspend fun createPost(post: Post, userId: String) {
        val now = System.currentTimeMillis()
        val map = hashMapOf(
            "id" to post.id,
            "userId" to userId,
            "authorName" to post.authorName,
            "authorHandle" to post.authorHandle,
            "authorAvatarUrl" to post.authorAvatarUrl,
            "hasRecentCheckIn" to post.hasRecentCheckIn,
            "locationLabel" to post.locationLabel,
            "provinceCode" to post.provinceCode,
            "caption" to post.caption,
            "mediaUrls" to post.mediaUrls,
            "aspectRatio" to post.aspectRatio.toDouble(),
            "likeCount" to 0L,
            "commentCount" to 0L,
            "saveCount" to 0L,
            "shareCount" to 0L,
            "tags" to post.tags,
            "createdAt" to now,
            "updatedAt" to now
        )

        postsCollection.document(post.id)
            .set(map)
            .await()
        Log.d(TAG, "Created new post on Firestore: ${post.id}")
    }

    /**
     * Deletes a post from Firestore (only allowed for the author).
     */
    suspend fun deletePost(postId: String) {
        if (postId.isBlank()) return
        try {
            postsCollection.document(postId).delete().await()
            Log.d(TAG, "Deleted post from Firestore: $postId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post $postId", e)
            throw e
        }
    }

    /**
     * Atomically toggles like status for a post.
     * Uses FieldValue.increment(1 / -1) to prevent race conditions when multiple users like concurrently.
     */
    suspend fun toggleLike(postId: String, userId: String, isLiked: Boolean) {
        if (postId.isBlank() || userId.isBlank()) return
        val postRef = postsCollection.document(postId)
        val likeDocRef = postRef.collection(SUBCOLLECTION_LIKES).document(userId)

        try {
            if (isLiked) {
                // Add like mark and increment counter
                likeDocRef.set(
                    mapOf(
                        "userId" to userId,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()

                postRef.update(
                    mapOf(
                        "likeCount" to FieldValue.increment(1L),
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            } else {
                // Remove like mark and decrement counter
                likeDocRef.delete().await()

                postRef.update(
                    mapOf(
                        "likeCount" to FieldValue.increment(-1L),
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like for post $postId by user $userId", e)
            throw e
        }
    }

    /**
     * Atomically toggles save status for a post.
     * Uses FieldValue.increment(1 / -1).
     */
    suspend fun toggleSave(postId: String, userId: String, isSaved: Boolean) {
        if (postId.isBlank() || userId.isBlank()) return
        val postRef = postsCollection.document(postId)
        val saveDocRef = postRef.collection(SUBCOLLECTION_SAVES).document(userId)

        try {
            if (isSaved) {
                saveDocRef.set(
                    mapOf(
                        "userId" to userId,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()

                postRef.update(
                    mapOf(
                        "saveCount" to FieldValue.increment(1L),
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            } else {
                saveDocRef.delete().await()

                postRef.update(
                    mapOf(
                        "saveCount" to FieldValue.increment(-1L),
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling save for post $postId by user $userId", e)
            throw e
        }
    }

    /**
     * Increments share count atomically.
     */
    suspend fun incrementShareCount(postId: String) {
        if (postId.isBlank()) return
        try {
            postsCollection.document(postId)
                .update("shareCount", FieldValue.increment(1L))
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error incrementing share count for $postId", e)
        }
    }

    /**
     * Adds a comment to the post's subcollection and atomically increments post's commentCount.
     */
    suspend fun addComment(postId: String, comment: Comment) {
        if (postId.isBlank() || comment.content.isBlank()) return
        val postRef = postsCollection.document(postId)
        val commentDocRef = postRef.collection(SUBCOLLECTION_COMMENTS).document(comment.id)

        val map = hashMapOf(
            "id" to comment.id,
            "postId" to postId,
            "userId" to comment.userId,
            "authorName" to comment.authorName,
            "authorHandle" to comment.authorHandle,
            "authorAvatarUrl" to comment.authorAvatarUrl,
            "content" to comment.content,
            "likeCount" to 0L,
            "createdAt" to System.currentTimeMillis()
        )

        try {
            commentDocRef.set(map).await()
            postRef.update(
                mapOf(
                    "commentCount" to FieldValue.increment(1L),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Log.d(TAG, "Added comment ${comment.id} to post $postId")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment to post $postId", e)
            throw e
        }
    }

    /**
     * Deletes a comment from the post's subcollection and atomically decrements commentCount.
     */
    suspend fun deleteComment(postId: String, commentId: String) {
        if (postId.isBlank() || commentId.isBlank()) return
        val postRef = postsCollection.document(postId)
        val commentDocRef = postRef.collection(SUBCOLLECTION_COMMENTS).document(commentId)

        try {
            commentDocRef.delete().await()
            postRef.update(
                mapOf(
                    "commentCount" to FieldValue.increment(-1L),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting comment $commentId on post $postId", e)
            throw e
        }
    }

    private fun calculateTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            hours < 24 -> "$hours giờ trước"
            days < 7 -> "$days ngày trước"
            days < 30 -> "${days / 7} tuần trước"
            else -> "${days / 30} tháng trước"
        }
    }
}
