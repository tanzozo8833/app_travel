package com.example.data.repository

import com.example.data.local.SeedData
import com.example.data.model.*
import com.example.data.remote.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LuminaRepository {

    private val _posts = MutableStateFlow<List<Post>>(SeedData.posts)
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _trips = MutableStateFlow<List<Trip>>(SeedData.sampleTrips)
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    private val _provinces = MutableStateFlow<List<Province>>(SeedData.provinces)
    val provinces: StateFlow<List<Province>> = _provinces.asStateFlow()

    private val _places = MutableStateFlow<List<Place>>(SeedData.places)
    val places: StateFlow<List<Place>> = _places.asStateFlow()

    private val _checkIns = MutableStateFlow<List<CheckInRecord>>(SeedData.checkIns)
    val checkIns: StateFlow<List<CheckInRecord>> = _checkIns.asStateFlow()

    private val _badges = MutableStateFlow<List<AchievementBadge>>(SeedData.badges)
    val badges: StateFlow<List<AchievementBadge>> = _badges.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(SeedData.notifications)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile>(SeedData.defaultProfile)
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _cityVault = MutableStateFlow<List<CheckInRecord>>(emptyList())
    val cityVault: StateFlow<List<CheckInRecord>> = _cityVault.asStateFlow()

    private val _savedBoards = MutableStateFlow<List<SavedBoard>>(SeedData.savedBoards)
    val savedBoards: StateFlow<List<SavedBoard>> = _savedBoards.asStateFlow()

    fun updateUserProfile(user: UserProfile) {
        _userProfile.value = user
    }

    // Post Interactions
    fun toggleLike(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newLiked = !post.isLiked
                post.copy(
                    isLiked = newLiked,
                    likeCount = if (newLiked) post.likeCount + 1 else post.likeCount - 1
                )
            } else post
        }
    }

    fun toggleSave(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newSaved = !post.isSaved
                post.copy(
                    isSaved = newSaved,
                    saveCount = if (newSaved) post.saveCount + 1 else post.saveCount - 1
                )
            } else post
        }
    }

    fun sharePost(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(shareCount = post.shareCount + 1)
            } else post
        }
    }

    fun addComment(postId: String, content: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(commentCount = post.commentCount + 1)
            } else post
        }
    }

    fun createPost(
        caption: String,
        mediaUrls: List<String>,
        locationLabel: String,
        provinceCode: String,
        attachedTrip: Trip? = null
    ): Post {
        val newPost = Post(
            id = "post_${UUID.randomUUID()}",
            authorName = _userProfile.value.displayName,
            authorHandle = _userProfile.value.username,
            authorAvatarUrl = _userProfile.value.avatarUrl,
            hasRecentCheckIn = true,
            locationLabel = locationLabel,
            provinceCode = provinceCode,
            timeAgo = "Vừa xong",
            caption = caption,
            mediaUrls = mediaUrls.ifEmpty {
                listOf("https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop")
            },
            aspectRatio = 0.82f,
            attachedTrip = attachedTrip,
            likeCount = 1,
            commentCount = 0,
            saveCount = 0,
            shareCount = 0,
            isLiked = true,
            tags = listOf("Lumina", "VietnamTravel", "GenZ")
        )
        _posts.value = listOf(newPost) + _posts.value
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

        _checkIns.value = listOf(record) + _checkIns.value

        // Update province visited count
        _provinces.value = _provinces.value.map { prov ->
            if (prov.code == provinceCode) {
                val newVisited = (prov.visitedPlacesCount + 1).coerceAtMost(prov.suggestedPlacesCount)
                prov.copy(
                    visitedPlacesCount = newVisited,
                    isCompleted = newVisited >= prov.suggestedPlacesCount
                )
            } else prov
        }

        // Update profile
        val visitedProvCount = _provinces.value.count { it.visitedPlacesCount > 0 }
        _userProfile.value = _userProfile.value.copy(
            visitedProvincesCount = visitedProvCount,
            totalDistanceKm = _userProfile.value.totalDistanceKm + 120.0
        )

        // Trigger notification
        val notif = NotificationItem(
            id = "notif_${UUID.randomUUID()}",
            isSocial = false,
            title = "Đóng dấu thành công!",
            message = "Bạn vừa đóng dấu tại $placeName ($provinceName). Hộ chiếu đã được cập nhật.",
            timeAgo = "Vừa xong",
            actionType = "checkin_success"
        )
        _notifications.value = listOf(notif) + _notifications.value

        return record
    }

    fun saveToCityVault(checkInRecord: CheckInRecord) {
        _cityVault.value = listOf(checkInRecord) + _cityVault.value
    }

    // Trip Management
    fun saveTrip(trip: Trip) {
        val existingIndex = _trips.value.indexOfFirst { it.id == trip.id }
        if (existingIndex >= 0) {
            val updated = _trips.value.toMutableList()
            updated[existingIndex] = trip
            _trips.value = updated
        } else {
            _trips.value = listOf(trip) + _trips.value
            _userProfile.value = _userProfile.value.copy(
                tripsCompletedCount = _userProfile.value.tripsCompletedCount + 1
            )
        }
    }

    fun duplicateTrip(tripId: String): Trip? {
        val original = _trips.value.find { it.id == tripId } ?: return null
        val duplicated = original.copy(
            id = "trip_${UUID.randomUUID()}",
            title = "${original.title} (Bản sao)",
            status = TripStatus.UPCOMING
        )
        _trips.value = listOf(duplicated) + _trips.value
        return duplicated
    }

    fun deleteTrip(tripId: String) {
        _trips.value = _trips.value.filter { it.id != tripId }
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
    fun search(query: String): List<Place> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return _places.value
        return _places.value.filter {
            it.name.lowercase().contains(q) ||
            it.address.lowercase().contains(q) ||
            it.tags.any { tag -> tag.lowercase().contains(q) } ||
            it.description.lowercase().contains(q)
        }
    }
}
