package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.data.auth.AuthManager
import com.example.data.auth.AuthResult
import com.example.data.auth.DeleteAccountResult
import com.example.data.local.database.LuminaDatabase
import com.example.data.local.entity.*
import com.example.data.local.seed.UserProfileSeed
import com.example.data.model.*
import com.example.data.remote.FirestoreUserProfileService
import com.example.data.remote.GeminiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LuminaRepository(
    private val database: LuminaDatabase,
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    val authManager = AuthManager(context)
    val firestoreService = FirestoreUserProfileService()

    val isLoggedIn: StateFlow<Boolean> = authManager.isLoggedIn
    val isAnonymous: StateFlow<Boolean> = authManager.isAnonymous

    init {
        scope.launch {
            authManager.currentAuthUser.collectLatest { profile ->
                if (profile != null && profile.id.isNotBlank()) {
                    firestoreService.syncProfileOnLogin(profile)
                }
            }
        }
    }

    // Reactive data streams directly connected to Room Database
    val posts: Flow<List<Post>> = combine(
        database.postDao().getAllPostsFlow(),
        database.tripDao().getAllTripsFlow()
    ) { postEntities, tripEntities ->
        val tripsMap = tripEntities.associateBy { it.id }
        postEntities.map { entity ->
            val attached = entity.attachedTripId?.let { tripsMap[it]?.toDomainModel() }
            entity.toDomainModel(attached)
        }
    }

    val trips: Flow<List<Trip>> = database.tripDao().getAllTripsFlow().map { list ->
        list.map { it.toDomainModel() }
    }

    val provinces: Flow<List<Province>> = database.provinceDao().getAllProvincesFlow().map { list ->
        list.map { it.toDomainModel() }
    }

    val places: Flow<List<Place>> = database.placeDao().getAllPlacesFlow().map { list ->
        list.map { it.toDomainModel() }
    }

    val checkIns: Flow<List<CheckInRecord>> = database.checkInDao().getAllCheckInsFlow().map { list ->
        list.map { it.toDomainModel() }
    }

    val badges: Flow<List<AchievementBadge>> = database.badgeDao().getAllBadgesFlow().map { list ->
        list.map { it.toDomainModel() }
    }

    val notifications: Flow<List<NotificationItem>> = database.notificationDao().getAllNotificationsFlow().map { list ->
        list.map { it.toDomainModel() }
    }

    /**
     * Real-time UserProfile flow sourced directly from Cloud Firestore.
     * Falls back to auth manager's current profile if Firestore is syncing or offline.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userProfile: Flow<UserProfile> = authManager.currentAuthUser.flatMapLatest { authProfile ->
        if (authProfile != null && authProfile.id.isNotBlank()) {
            firestoreService.observeUserProfile(authProfile.id).map { remoteProfile ->
                remoteProfile ?: authProfile
            }
        } else {
            flowOf(
                UserProfile(
                    id = "traveler_guest",
                    username = "traveler",
                    displayName = "Lữ khách Lumina",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
                )
            )
        }
    }

    /**
     * Real-time list of all user profiles from Cloud Firestore, combined with sample seeds for community explore.
     */
    val userProfiles: Flow<List<UserProfile>> = firestoreService.observeAllUserProfiles().map { remoteList ->
        val remoteIds = remoteList.map { it.id }.toSet()
        val missingSeeds = UserProfileSeed.data.filter { it.id !in remoteIds }
        remoteList + missingSeeds
    }

    /**
     * Fetches or observes a specific user profile from Cloud Firestore.
     */
    fun getUserProfileFlow(userId: String, username: String): Flow<UserProfile?> {
        val cleanHandle = username.removePrefix("@").lowercase()
        if (userId.isNotBlank()) {
            return firestoreService.observeUserProfile(userId).map { remote ->
                remote ?: UserProfileSeed.data.find { it.id == userId || it.username.equals(cleanHandle, ignoreCase = true) }
            }
        }
        return userProfiles.map { list ->
            list.find { it.username.equals(cleanHandle, ignoreCase = true) }
        }
    }

    // Google Sign-In & Authentication
    suspend fun signInWithGoogle(activity: Activity, allowBrowserFallback: Boolean = true): AuthResult {
        val result = authManager.signInWithGoogle(activity, allowBrowserFallback = allowBrowserFallback)
        handleAuthSuccess(result)
        return result
    }

    suspend fun signInWithGoogleBrowser(activity: Activity): AuthResult {
        val result = authManager.signInWithGoogleBrowser(activity)
        handleAuthSuccess(result)
        return result
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        val result = authManager.signInWithEmail(email, password)
        handleAuthSuccess(result)
        return result
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String): AuthResult {
        val result = authManager.signUpWithEmail(email, password, displayName)
        handleAuthSuccess(result)
        return result
    }

    suspend fun signInAsGuest(customName: String = "Lữ khách Khám Phá"): AuthResult {
        val result = authManager.signInAsGuest(customName)
        handleAuthSuccess(result)
        return result
    }

    suspend fun switchToExistingGoogleAccount(credential: com.google.firebase.auth.AuthCredential?, activity: Activity? = null): AuthResult {
        val result = authManager.switchToExistingGoogleAccount(credential, activity)
        handleAuthSuccess(result)
        return result
    }

    private suspend fun handleAuthSuccess(result: AuthResult) {
        if (result is AuthResult.Success) {
            val synced = firestoreService.syncProfileOnLogin(result.userProfile)
            authManager.updateSavedUserProfile(synced)
        }
    }

    suspend fun logout() {
        authManager.logout()
    }

    /**
     * Completely deletes the user account from Firebase Auth, deletes the profile from
     * Cloud Firestore server, and wipes all local personal records (posts, comments, check-in stamps,
     * trips, follows, notifications, and saved boards) belonging to this user from Room database.
     */
    suspend fun deleteUserAccount(activity: Activity): DeleteAccountResult {
        val currentUser = authManager.currentAuthUser.value
        val userId = currentUser?.id ?: ""
        val userHandle = currentUser?.username ?: ""

        // 1. Delete from Firebase Authentication (handles re-auth automatically if needed)
        val deleteResult = authManager.deleteAccount(activity)
        if (deleteResult is DeleteAccountResult.Error || deleteResult is DeleteAccountResult.Cancelled) {
            return deleteResult
        }

        // 2. Delete user profile document from Firestore server
        if (userId.isNotBlank()) {
            try {
                firestoreService.deleteUserProfile(userId)
            } catch (e: Exception) {
                Log.w("LuminaRepository", "Error deleting Firestore profile for $userId", e)
            }
            database.postDao().deletePostsByUser(userId, userHandle)
            database.commentDao().deleteCommentsByUser(userId, userHandle)
            database.checkInDao().deleteCheckInsByUserId(userId)
            database.tripDao().deleteTripsByUserId(userId)
            database.userFollowDao().deleteFollowsByUser(userId, userHandle)
            database.notificationDao().deleteNotificationsByUserId(userId)
            database.savedBoardDao().deleteSavedBoardsByUserId(userId)
        }

        return DeleteAccountResult.Success
    }

    val cityVault: Flow<List<CheckInRecord>> = database.checkInDao().getCityVaultFlow().map { list ->
        list.map { it.toDomainModel() }
    }

    val savedBoards: Flow<List<SavedBoard>> = database.savedBoardDao().getAllSavedBoardsFlow().map { list ->
        list.map { it.toDomainModel() }
    }

    val frames: Flow<List<LuminaFrame>> = database.frameDao().getAllFramesFlow().map { list ->
        list.map { it.toDomainModel() }
    }

    fun getCommentsForPost(postId: String): Flow<List<Comment>> {
        return database.commentDao().getCommentsForPostFlow(postId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    // Profile updates synced directly to Firestore
    fun updateUserProfile(user: UserProfile) {
        scope.launch {
            firestoreService.saveOrUpdateUserProfile(user)
            authManager.updateSavedUserProfile(user)
        }
    }

    // Post Interactions
    fun toggleLike(postId: String) {
        scope.launch {
            val post = database.postDao().getPostById(postId) ?: return@launch
            val newLiked = !post.isLiked
            val newLikeCount = if (newLiked) post.likeCount + 1 else (post.likeCount - 1).coerceAtLeast(0)
            database.postDao().updateLikeStatus(postId, newLiked, newLikeCount)
        }
    }

    fun toggleSave(postId: String) {
        scope.launch {
            val post = database.postDao().getPostById(postId) ?: return@launch
            val newSaved = !post.isSaved
            val newSaveCount = if (newSaved) post.saveCount + 1 else (post.saveCount - 1).coerceAtLeast(0)
            database.postDao().updateSaveStatus(postId, newSaved, newSaveCount)
        }
    }

    fun sharePost(postId: String) {
        scope.launch {
            database.postDao().incrementShareCount(postId)
        }
    }

    fun addComment(postId: String, content: String) {
        scope.launch {
            val activeUser = authManager.currentAuthUser.value
            val userId = activeUser?.id ?: "u_1"
            val authorName = activeUser?.displayName ?: "Lữ khách Lumina"
            val authorHandle = activeUser?.username ?: "traveler"
            val authorAvatar = activeUser?.avatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop"

            val comment = Comment(
                id = "c_${UUID.randomUUID()}",
                postId = postId,
                userId = userId,
                authorName = authorName,
                authorHandle = authorHandle,
                authorAvatarUrl = authorAvatar,
                content = content,
                timeAgo = "Vừa xong",
                likeCount = 0,
                isLiked = false
            )
            database.commentDao().insert(CommentEntity.fromDomainModel(comment, userId = userId))
            database.postDao().incrementCommentCount(postId)
        }
    }

    fun createPost(
        caption: String,
        mediaUrls: List<String>,
        locationLabel: String,
        provinceCode: String,
        attachedTrip: Trip? = null,
        aspectRatio: Float = 0.82f
    ): Post {
        val newPostId = "post_${UUID.randomUUID()}"
        scope.launch {
            val activeUser = authManager.currentAuthUser.value
            val userId = activeUser?.id ?: "u_1"
            val authorName = activeUser?.displayName ?: "Lữ khách Lumina"
            val authorHandle = activeUser?.username ?: "traveler"
            val authorAvatarUrl = activeUser?.avatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop"

            val entity = PostEntity(
                id = newPostId,
                userId = userId,
                authorName = authorName,
                authorHandle = authorHandle,
                authorAvatarUrl = authorAvatarUrl,
                hasRecentCheckIn = true,
                locationLabel = locationLabel,
                provinceCode = provinceCode,
                timeAgo = "Vừa xong",
                caption = caption,
                mediaUrls = mediaUrls.ifEmpty {
                    listOf("https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop")
                },
                aspectRatio = aspectRatio,
                attachedTripId = attachedTrip?.id,
                likeCount = 1,
                commentCount = 0,
                saveCount = 0,
                shareCount = 0,
                isLiked = true,
                isSaved = false,
                tags = listOf("Lumina", "VietnamTravel", "GenZ"),
                createdAt = System.currentTimeMillis()
            )
            database.postDao().insert(entity)
        }

        return Post(
            id = newPostId,
            authorName = "Lữ khách Lumina",
            authorHandle = "traveler",
            authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop",
            hasRecentCheckIn = true,
            locationLabel = locationLabel,
            provinceCode = provinceCode,
            timeAgo = "Vừa xong",
            caption = caption,
            mediaUrls = mediaUrls.ifEmpty {
                listOf("https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop")
            },
            aspectRatio = aspectRatio,
            attachedTrip = attachedTrip,
            likeCount = 1,
            commentCount = 0,
            saveCount = 0,
            shareCount = 0,
            isLiked = true,
            tags = listOf("Lumina", "VietnamTravel", "GenZ")
        )
    }

    // Check-in Engine
    fun performQuickCheckIn(
        placeName: String = "Đồi Chè Cầu Đất",
        provinceCode: String = "LDG",
        provinceName: String = "Lâm Đồng",
        photoUrl: String? = null
    ): CheckInRecord {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val now = Date()

        val record = CheckInRecord(
            id = "chk_${UUID.randomUUID()}",
            placeName = placeName,
            provinceName = provinceName,
            provinceCode = provinceCode,
            timestamp = timeFormat.format(now),
            formattedDate = dateFormat.format(now),
            method = if (photoUrl != null) CheckInMethod.PHOTO else CheckInMethod.QUICK,
            photoUrl = photoUrl,
            trustScore = 1.0f
        )

        scope.launch {
            val activeUser = authManager.currentAuthUser.value
            val userId = activeUser?.id ?: "u_1"

            // 1. Insert check-in record
            database.checkInDao().insert(CheckInEntity.fromDomainModel(record, userId = userId))

            // 2. Mark place as visited
            database.placeDao().markAsVisitedByName(placeName, provinceCode)

            // 3. Update province visited count
            val province = database.provinceDao().getProvinceByCode(provinceCode)
            if (province != null) {
                val newVisited = (province.visitedPlacesCount + 1).coerceAtMost(province.suggestedPlacesCount)
                val isComp = newVisited >= province.suggestedPlacesCount
                database.provinceDao().updateProgress(provinceCode, newVisited, isComp)
            }

            // 4. Update user profile stats on Firestore server for this user
            val allUserCheckIns = database.checkInDao().getAllCheckInsFlow().first().filter { it.userId == userId }
            val userVisitedProvincesCount = allUserCheckIns.map { it.provinceCode }.distinct().size
            firestoreService.updateStatsAfterCheckIn(userId, userVisitedProvincesCount, 0.0)

            // 5. Check & Update Badges
            val totalCheckIns = allUserCheckIns.size
            if (totalCheckIns >= 1) {
                database.badgeDao().updateProgress("first_stamp", 1, true, dateFormat.format(now))
            }
            if (userVisitedProvincesCount >= 5) {
                database.badgeDao().updateProgress("province_5", userVisitedProvincesCount, true, dateFormat.format(now))
            }
            if (userVisitedProvincesCount >= 10) {
                database.badgeDao().updateProgress("province_10", userVisitedProvincesCount, userVisitedProvincesCount >= 10, if (userVisitedProvincesCount >= 10) dateFormat.format(now) else null)
            }

            // 6. Trigger notification
            val notif = NotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                userId = userId,
                isSocial = false,
                title = "Đóng dấu thành công!",
                message = "Bạn vừa đóng dấu tại $placeName ($provinceName). Hộ chiếu đã được cập nhật.",
                timeAgo = "Vừa xong",
                actionType = "checkin_success",
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            database.notificationDao().insert(notif)
        }

        return record
    }

    fun saveToCityVault(checkInRecord: CheckInRecord) {
        scope.launch {
            database.checkInDao().updateCityVaultStatus(checkInRecord.id, true)
        }
    }

    // Trip Management
    fun saveTrip(trip: Trip) {
        scope.launch {
            val activeUser = authManager.currentAuthUser.value
            val userId = activeUser?.id ?: "u_1"
            val entity = TripEntity.fromDomainModel(trip, userId = userId)
            database.tripDao().insert(entity)
            firestoreService.incrementTripsCompleted(userId)
        }
    }

    fun duplicateTrip(tripId: String, onDuplicated: (Trip) -> Unit = {}) {
        scope.launch {
            val originalEntity = database.tripDao().getTripById(tripId) ?: return@launch
            val original = originalEntity.toDomainModel()
            val duplicated = original.copy(
                id = "trip_${UUID.randomUUID()}",
                title = "${original.title} (Bản sao)",
                status = TripStatus.UPCOMING
            )
            val entity = TripEntity.fromDomainModel(duplicated, userId = originalEntity.userId)
            database.tripDao().insert(entity)
            withContext(Dispatchers.Main) {
                onDuplicated(duplicated)
            }
        }
    }

    fun deleteTrip(tripId: String) {
        scope.launch {
            database.tripDao().deleteById(tripId)
        }
    }

    // Notifications
    fun markNotificationAsRead(id: String) {
        scope.launch {
            database.notificationDao().markAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        scope.launch {
            database.notificationDao().markAllAsRead()
        }
    }

    // AI Generation
    suspend fun generateAiTrip(
        destination: String,
        durationDays: Int,
        budgetVnd: Long,
        groupType: String,
        styleTags: List<String>,
        customNotes: String
    ): Trip {
        return GeminiService.generateTripPlan(
            destination = destination,
            durationDays = durationDays,
            budgetVnd = budgetVnd,
            groupType = groupType,
            styleTags = styleTags,
            customNotes = customNotes
        )
    }

    suspend fun askAi(question: String, destination: String, plugin: String? = null): String {
        return GeminiService.askAi(question, destination, plugin)
    }

    // Follow & Social Relationship Engine
    fun getFollowedTargetKeysFlow(followerId: String): Flow<Set<String>> {
        return database.userFollowDao().getFollowingsFlow(followerId).map { follows ->
            val set = mutableSetOf<String>()
            follows.forEach { follow ->
                if (follow.followedId.isNotBlank()) set.add(follow.followedId)
                if (follow.followedHandle.isNotBlank()) set.add(follow.followedHandle)
            }
            set
        }
    }

    fun getFollowingCountFlow(userId: String): Flow<Int> {
        return database.userFollowDao().getFollowingCountFlow(userId)
    }

    fun getFollowerCountFlow(userId: String): Flow<Int> {
        return database.userFollowDao().getFollowerCountFlow(userId)
    }

    suspend fun isFollowing(followerId: String, followedId: String, followedHandle: String): Boolean {
        return database.userFollowDao().isFollowing(followerId, followedId, followedHandle)
    }

    suspend fun toggleFollow(
        followerId: String,
        followedId: String,
        followedHandle: String,
        followedName: String,
        followedAvatarUrl: String
    ): Boolean {
        val currentlyFollowing = database.userFollowDao().isFollowing(followerId, followedId, followedHandle)
        if (currentlyFollowing) {
            database.userFollowDao().deleteFollow(followerId, followedId, followedHandle)
            return false
        } else {
            val entity = UserFollowEntity(
                followerId = followerId,
                followedId = followedId,
                followedHandle = followedHandle,
                followedName = followedName,
                followedAvatarUrl = followedAvatarUrl,
                createdAt = System.currentTimeMillis()
            )
            database.userFollowDao().insert(entity)
            return true
        }
    }

    // Search filter
    fun search(query: String): Flow<List<Place>> {
        val q = query.trim()
        return if (q.isBlank()) {
            places
        } else {
            database.placeDao().searchPlacesFlow(q).map { list ->
                list.map { it.toDomainModel() }
            }
        }
    }
}
