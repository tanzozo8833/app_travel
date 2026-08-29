package com.example.data.remote

import android.util.Log
import com.example.data.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreUserProfileService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FirestoreProfileService"
        private const val COLLECTION_USERS = "users"
    }

    private val usersCollection = firestore.collection(COLLECTION_USERS)

    /**
     * Converts a Firestore DocumentSnapshot to domain UserProfile model.
     */
    fun documentToUserProfile(doc: DocumentSnapshot, fallbackId: String = ""): UserProfile? {
        if (!doc.exists()) return null
        return try {
            val id = doc.getString("id") ?: doc.id.ifBlank { fallbackId }
            val username = doc.getString("username") ?: "traveler"
            val displayName = doc.getString("displayName") ?: "Lữ khách Lumina"
            val avatarUrl = doc.getString("avatarUrl") ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
            val tagTitle = doc.getString("tagTitle") ?: "GEN Z EXPLORER"
            val bio = doc.getString("bio") ?: "Khám phá 34 tỉnh thành Việt Nam cùng Lumina ✨"
            val topCityName = doc.getString("topCityName") ?: ""
            val topCityCheckInCount = doc.getLong("topCityCheckInCount")?.toInt() ?: 0
            val visitedProvincesCount = doc.getLong("visitedProvincesCount")?.toInt() ?: 0
            val totalProvincesCount = doc.getLong("totalProvincesCount")?.toInt() ?: 34
            val totalDistanceKm = doc.getDouble("totalDistanceKm") ?: 0.0
            val tripsCompletedCount = doc.getLong("tripsCompletedCount")?.toInt() ?: 0
            val totalSavesCount = doc.getLong("totalSavesCount")?.toInt() ?: 0

            UserProfile(
                id = id,
                username = username,
                displayName = displayName,
                avatarUrl = avatarUrl,
                tagTitle = tagTitle,
                bio = bio,
                topCityName = topCityName,
                topCityCheckInCount = topCityCheckInCount,
                visitedProvincesCount = visitedProvincesCount,
                totalProvincesCount = totalProvincesCount,
                totalDistanceKm = totalDistanceKm,
                tripsCompletedCount = tripsCompletedCount,
                totalSavesCount = totalSavesCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping document ${doc.id} to UserProfile", e)
            null
        }
    }

    /**
     * Fetches user profile once from Firestore.
     */
    suspend fun getUserProfile(userId: String): UserProfile? {
        if (userId.isBlank()) return null
        return try {
            val doc = usersCollection.document(userId).get().await()
            documentToUserProfile(doc, userId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get user profile for $userId from Firestore", e)
            null
        }
    }

    /**
     * Real-time Flow observing a specific user profile on Firestore.
     */
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for user profile $userId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val profile = documentToUserProfile(snapshot, userId)
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Real-time Flow observing all user profiles on Firestore.
     */
    fun observeAllUserProfiles(): Flow<List<UserProfile>> = callbackFlow {
        val registration = usersCollection.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed for all user profiles", error)
                return@addSnapshotListener
            }
            if (snapshots != null) {
                val list = snapshots.documents.mapNotNull { doc ->
                    documentToUserProfile(doc, doc.id)
                }
                trySend(list)
            } else {
                trySend(emptyList())
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Saves or completely updates a user profile to Firestore.
     */
    suspend fun saveOrUpdateUserProfile(profile: UserProfile) {
        if (profile.id.isBlank()) return
        try {
            val map = hashMapOf(
                "id" to profile.id,
                "username" to profile.username,
                "displayName" to profile.displayName,
                "avatarUrl" to profile.avatarUrl,
                "tagTitle" to profile.tagTitle,
                "bio" to profile.bio,
                "topCityName" to profile.topCityName,
                "topCityCheckInCount" to profile.topCityCheckInCount,
                "visitedProvincesCount" to profile.visitedProvincesCount,
                "totalProvincesCount" to profile.totalProvincesCount,
                "totalDistanceKm" to profile.totalDistanceKm,
                "tripsCompletedCount" to profile.tripsCompletedCount,
                "totalSavesCount" to profile.totalSavesCount,
                "updatedAt" to System.currentTimeMillis()
            )

            usersCollection.document(profile.id)
                .set(map, SetOptions.merge())
                .await()
            Log.d(TAG, "Successfully synced user profile to Firestore: ${profile.displayName} (${profile.id})")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile to Firestore for ${profile.id}", e)
        }
    }

    /**
     * Synchronizes profile upon login.
     * If profile already exists on Firestore, it loads the existing profile and merges non-empty identity fields (displayName/avatarUrl) while PRESERVING stats and bio.
     * If profile is new, creates the document on Firestore.
     */
    suspend fun syncProfileOnLogin(authProfile: UserProfile): UserProfile {
        if (authProfile.id.isBlank()) return authProfile
        return try {
            val existingDoc = usersCollection.document(authProfile.id).get().await()
            if (existingDoc.exists()) {
                val existing = documentToUserProfile(existingDoc, authProfile.id)
                if (existing != null) {
                    val merged = existing.copy(
                        displayName = authProfile.displayName.ifBlank { existing.displayName },
                        avatarUrl = authProfile.avatarUrl.ifBlank { existing.avatarUrl },
                        username = existing.username.ifBlank { authProfile.username }
                    )
                    // If anything was updated, sync back
                    if (merged != existing) {
                        saveOrUpdateUserProfile(merged)
                    }
                    Log.d(TAG, "Loaded existing Firestore profile for ${authProfile.id}: ${merged.displayName}")
                    return merged
                }
            }

            // New profile: save to Firestore
            saveOrUpdateUserProfile(authProfile)
            Log.d(TAG, "Created new Firestore profile for ${authProfile.id}: ${authProfile.displayName}")
            authProfile
        } catch (e: Exception) {
            Log.w(TAG, "Error during syncProfileOnLogin, falling back to local authProfile", e)
            authProfile
        }
    }

    /**
     * Updates editable personal info: displayName, bio, tagTitle.
     */
    suspend fun updateProfileInfo(userId: String, displayName: String, bio: String, tagTitle: String) {
        if (userId.isBlank()) return
        try {
            val updates = mapOf(
                "displayName" to displayName,
                "bio" to bio,
                "tagTitle" to tagTitle,
                "updatedAt" to System.currentTimeMillis()
            )
            usersCollection.document(userId)
                .update(updates)
                .await()
            Log.d(TAG, "Updated profile info on Firestore for $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile info on Firestore for $userId", e)
        }
    }

    /**
     * Updates user stats after check-in.
     */
    suspend fun updateStatsAfterCheckIn(userId: String, visitedCount: Int, distanceDelta: Double) {
        if (userId.isBlank()) return
        try {
            val updates = mapOf(
                "visitedProvincesCount" to visitedCount,
                "totalDistanceKm" to FieldValue.increment(distanceDelta),
                "updatedAt" to System.currentTimeMillis()
            )
            usersCollection.document(userId)
                .update(updates)
                .await()
            Log.d(TAG, "Updated check-in stats on Firestore for $userId: visited=$visitedCount")
        } catch (e: Exception) {
            Log.w(TAG, "Error updating check-in stats on Firestore for $userId", e)
        }
    }

    /**
     * Increments trips completed count on Firestore.
     */
    suspend fun incrementTripsCompleted(userId: String) {
        if (userId.isBlank()) return
        try {
            usersCollection.document(userId)
                .update("tripsCompletedCount", FieldValue.increment(1), "updatedAt", System.currentTimeMillis())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error incrementing trips completed on Firestore for $userId", e)
        }
    }

    /**
     * Deletes user profile document from Firestore when account is deleted.
     */
    suspend fun deleteUserProfile(userId: String) {
        if (userId.isBlank()) return
        try {
            usersCollection.document(userId).delete().await()
            Log.d(TAG, "Deleted Firestore user profile document for $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user profile from Firestore for $userId", e)
        }
    }
}
