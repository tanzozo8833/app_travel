package com.example.data.remote

import android.util.Log
import com.example.data.model.UserFollow
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreFollowService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FirestoreFollowService"
        const val COLLECTION_USER_FOLLOWS = "user_follows"
    }

    private val followsCollection = firestore.collection(COLLECTION_USER_FOLLOWS)

    /**
     * Maps a Firestore document to UserFollow model.
     */
    private fun documentToUserFollow(doc: DocumentSnapshot): UserFollow? {
        if (!doc.exists()) return null
        return try {
            val id = doc.getString("id") ?: doc.id
            val followerId = doc.getString("followerId") ?: ""
            val followedId = doc.getString("followedId") ?: ""
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

            if (followerId.isBlank() || followedId.isBlank()) return null

            UserFollow(
                id = id,
                followerId = followerId,
                followedId = followedId,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping doc ${doc.id} to UserFollow", e)
            null
        }
    }

    /**
     * Real-time stream of users that [followerId] is currently following.
     * Returns a set of followed user IDs.
     */
    fun observeFollowedUserIds(followerId: String): Flow<Set<String>> = callbackFlow {
        if (followerId.isBlank()) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }

        val registration = followsCollection
            .whereEqualTo("followerId", followerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for followings of $followerId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val followedIds = snapshot.documents.mapNotNull { doc ->
                        doc.getString("followedId")
                    }.toSet()
                    trySend(followedIds)
                } else {
                    trySend(emptySet())
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Real-time count of users that [userId] is currently following (Following Count).
     */
    fun observeFollowingCount(userId: String): Flow<Int> = callbackFlow {
        if (userId.isBlank()) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val registration = followsCollection
            .whereEqualTo("followerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for following count of $userId", error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Real-time count of followers that [userId] has (Follower Count).
     */
    fun observeFollowerCount(userId: String): Flow<Int> = callbackFlow {
        if (userId.isBlank()) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val registration = followsCollection
            .whereEqualTo("followedId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for follower count of $userId", error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Checks if [followerId] is following [followedId].
     */
    suspend fun isFollowing(followerId: String, followedId: String): Boolean {
        if (followerId.isBlank() || followedId.isBlank()) return false
        val docId = "${followerId}_${followedId}"
        return try {
            val doc = followsCollection.document(docId).get().await()
            doc.exists()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check follow status for $docId", e)
            false
        }
    }

    /**
     * Toggles follow relationship between [followerId] and [followedId] purely using user IDs.
     * Returns true if now following, false if unfollowed.
     */
    suspend fun toggleFollow(followerId: String, followedId: String): Boolean {
        if (followerId.isBlank() || followedId.isBlank() || followerId == followedId) return false
        val docId = "${followerId}_${followedId}"
        val docRef = followsCollection.document(docId)

        return try {
            val doc = docRef.get().await()
            if (doc.exists()) {
                // Currently following -> Unfollow (Delete)
                docRef.delete().await()
                Log.d(TAG, "Unfollowed: $docId")
                false
            } else {
                // Not following -> Follow (Create)
                val followData = hashMapOf(
                    "id" to docId,
                    "followerId" to followerId,
                    "followedId" to followedId,
                    "createdAt" to System.currentTimeMillis()
                )
                docRef.set(followData).await()
                Log.d(TAG, "Followed: $docId")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling follow for $docId", e)
            false
        }
    }

    /**
     * Cleans up all follow relationships involving [userId] on account deletion.
     */
    suspend fun deleteFollowsForUser(userId: String) {
        if (userId.isBlank()) return
        try {
            // Delete where user is follower
            val followings = followsCollection.whereEqualTo("followerId", userId).get().await()
            for (doc in followings.documents) {
                doc.reference.delete().await()
            }

            // Delete where user is followed
            val followers = followsCollection.whereEqualTo("followedId", userId).get().await()
            for (doc in followers.documents) {
                doc.reference.delete().await()
            }
            Log.d(TAG, "Cleaned up all follows for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up follows for user $userId", e)
        }
    }
}
