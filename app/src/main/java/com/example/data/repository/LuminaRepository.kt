package com.example.data.repository

import com.example.data.local.database.LuminaDatabase
import com.example.data.local.entity.*
import com.example.data.model.*
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
    private val database: LuminaDatabase
) {
    private val scope = CoroutineScope(Dispatchers.IO)

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

    val userProfile: Flow<UserProfile> = database.userProfileDao().getActiveProfileFlow().map {
        it?.toDomainModel() ?: UserProfile(
            id = "u_1",
            username = "linh.travels",
            displayName = "Thảo Linh",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
        )
    }

    val userProfiles: Flow<List<UserProfile>> = database.userProfileDao().getAllProfilesFlow().map { list ->
        list.map { it.toDomainModel() }
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

    // Profile updates
    fun updateUserProfile(user: UserProfile) {
        scope.launch {
            val entity = UserProfileEntity.fromDomainModel(user, isActive = true)
            database.userProfileDao().insert(entity)
        }
    }

    fun switchActiveUser(userId: String) {
        scope.launch {
            database.userProfileDao().setActiveProfile(userId)
        }
    }

    fun addNewUser(displayName: String, username: String) {
        scope.launch {
            val newAcc = UserProfile(
                id = "u_${System.currentTimeMillis()}",
                username = username.lowercase().replace(" ", "."),
                displayName = displayName,
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&auto=format&fit=crop",
                tagTitle = "NEW EXPLORER",
                bio = "Bắt đầu hành trình khám phá Việt Nam cùng Lumina ✨",
                topCityName = "Đà Nẵng",
                topCityCheckInCount = 1,
                visitedProvincesCount = 1,
                totalProvincesCount = 34,
                totalDistanceKm = 300.0,
                tripsCompletedCount = 1,
                totalSavesCount = 12
            )
            database.userProfileDao().insert(UserProfileEntity.fromDomainModel(newAcc, isActive = true))
            database.userProfileDao().setActiveProfile(newAcc.id)
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
            val activeUser = database.userProfileDao().getActiveProfile()?.toDomainModel()
            val authorName = activeUser?.displayName ?: "Thảo Linh"
            val authorHandle = activeUser?.username ?: "linh.travels"
            val authorAvatar = activeUser?.avatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop"

            val comment = Comment(
                id = "c_${UUID.randomUUID()}",
                postId = postId,
                authorName = authorName,
                authorHandle = authorHandle,
                authorAvatarUrl = authorAvatar,
                content = content,
                timeAgo = "Vừa xong",
                likeCount = 0,
                isLiked = false
            )
            database.commentDao().insert(CommentEntity.fromDomainModel(comment))
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
        val newPost = Post(
            id = newPostId,
            authorName = "Thảo Linh",
            authorHandle = "linh.travels",
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

        scope.launch {
            val activeUser = database.userProfileDao().getActiveProfile()
            val entity = PostEntity(
                id = newPost.id,
                userId = activeUser?.id ?: "u_1",
                authorName = activeUser?.displayName ?: "Thảo Linh",
                authorHandle = activeUser?.username ?: "linh.travels",
                authorAvatarUrl = activeUser?.avatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop",
                hasRecentCheckIn = true,
                locationLabel = locationLabel,
                provinceCode = provinceCode,
                timeAgo = "Vừa xong",
                caption = caption,
                mediaUrls = newPost.mediaUrls,
                aspectRatio = aspectRatio,
                attachedTripId = attachedTrip?.id,
                likeCount = 1,
                commentCount = 0,
                saveCount = 0,
                shareCount = 0,
                isLiked = true,
                isSaved = false,
                tags = newPost.tags,
                createdAt = System.currentTimeMillis()
            )
            database.postDao().insert(entity)
        }

        return newPost
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
            val activeUser = database.userProfileDao().getActiveProfile()
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

            // 4. Update user profile stats
            val allProvinces = database.provinceDao().getAllProvinces()
            val visitedCount = allProvinces.count { it.visitedPlacesCount > 0 }
            database.userProfileDao().updateStatsAfterCheckIn(userId, visitedCount, 0.0)

            // 5. Check & Update Badges
            val totalCheckIns = database.checkInDao().getCount()
            if (totalCheckIns >= 1) {
                database.badgeDao().updateProgress("first_stamp", 1, true, dateFormat.format(now))
            }
            if (visitedCount >= 5) {
                database.badgeDao().updateProgress("province_5", visitedCount, true, dateFormat.format(now))
            }
            if (visitedCount >= 10) {
                database.badgeDao().updateProgress("province_10", visitedCount, visitedCount >= 10, if (visitedCount >= 10) dateFormat.format(now) else null)
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
            val activeUser = database.userProfileDao().getActiveProfile()
            val userId = activeUser?.id ?: "u_1"
            val entity = TripEntity.fromDomainModel(trip, userId = userId)
            database.tripDao().insert(entity)
            database.userProfileDao().incrementTripsCompleted(userId)
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
