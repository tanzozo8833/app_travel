package com.example.ui

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.LuminaApplication
import com.example.data.auth.AuthResult
import com.example.data.auth.DeleteAccountResult
import com.example.data.model.*
import com.example.data.repository.LuminaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class MainTab {
    HOME,
    AI_PLAN,
    CHECK_IN,
    MY_TRIPS,
    PROFILE
}

enum class FeedSubTab {
    EXPLORE,   // Khám phá
    FOLLOWING, // Đang theo dõi
    NEARBY     // Gần bạn
}

enum class TripSubTab {
    UPCOMING,  // Sắp đi
    ONGOING,   // Đang đi
    PAST       // Đã đi
}

data class AiWizardState(
    val step: Int = 1,
    val destination: String = "Đà Lạt",
    val durationDays: Int = 3,
    val budgetVnd: Long = 2400000,
    val groupType: String = "Cặp đôi",
    val styleTags: List<String> = listOf("Chill sương mù", "Cà phê view", "Sống ảo"),
    val customNotes: String = "",
    val isGenerating: Boolean = false,
    val generationProgressMessage: String = "",
    val generatedTrip: Trip? = null
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val attachedPlace: Place? = null
)

data class UserProfileTarget(
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val bio: String = ""
)

class LuminaViewModel(
    private val repository: LuminaRepository = LuminaApplication.instance.repository
) : ViewModel() {

    // Authentication State
    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
    val isAnonymous: StateFlow<Boolean> = repository.isAnonymous

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _loginErrorIsNetwork = MutableStateFlow(false)
    val loginErrorIsNetwork: StateFlow<Boolean> = _loginErrorIsNetwork.asStateFlow()

    private val _loginCanFallbackToBrowser = MutableStateFlow(false)
    val loginCanFallbackToBrowser: StateFlow<Boolean> = _loginCanFallbackToBrowser.asStateFlow()

    // Account Collision when upgrading Guest -> Google
    private val _accountCollisionPendingCredential = MutableStateFlow<com.google.firebase.auth.AuthCredential?>(null)
    val accountCollisionPendingCredential: StateFlow<com.google.firebase.auth.AuthCredential?> = _accountCollisionPendingCredential.asStateFlow()

    private val _accountCollisionEmail = MutableStateFlow<String?>(null)
    val accountCollisionEmail: StateFlow<String?> = _accountCollisionEmail.asStateFlow()

    // Main Navigation
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Sub Tabs
    private val _feedSubTab = MutableStateFlow(FeedSubTab.EXPLORE)
    val feedSubTab: StateFlow<FeedSubTab> = _feedSubTab.asStateFlow()

    private val _tripSubTab = MutableStateFlow(TripSubTab.ONGOING)
    val tripSubTab: StateFlow<TripSubTab> = _tripSubTab.asStateFlow()

    // Active Modals & Full Screens
    private val _selectedPost = MutableStateFlow<Post?>(null)
    val selectedPost: StateFlow<Post?> = _selectedPost.asStateFlow()

    private val _selectedTrip = MutableStateFlow<Trip?>(null)
    val selectedTrip: StateFlow<Trip?> = _selectedTrip.asStateFlow()

    private val _isEditingTrip = MutableStateFlow(false)
    val isEditingTrip: StateFlow<Boolean> = _isEditingTrip.asStateFlow()

    private val _isShowingCreatePost = MutableStateFlow(false)
    val isShowingCreatePost: StateFlow<Boolean> = _isShowingCreatePost.asStateFlow()

    private val _isShowingFrameCam = MutableStateFlow(false)
    val isShowingFrameCam: StateFlow<Boolean> = _isShowingFrameCam.asStateFlow()

    private val _targetedStampPlace = MutableStateFlow<Place?>(null)
    val targetedStampPlace: StateFlow<Place?> = _targetedStampPlace.asStateFlow()

    private val _targetedStampProvince = MutableStateFlow<Province?>(null)
    val targetedStampProvince: StateFlow<Province?> = _targetedStampProvince.asStateFlow()

    private val _isShowingSearch = MutableStateFlow(false)
    val isShowingSearch: StateFlow<Boolean> = _isShowingSearch.asStateFlow()

    private val _isShowingNotifications = MutableStateFlow(false)
    val isShowingNotifications: StateFlow<Boolean> = _isShowingNotifications.asStateFlow()

    private val _isShowingCityPassport = MutableStateFlow(false)
    val isShowingCityPassport: StateFlow<Boolean> = _isShowingCityPassport.asStateFlow()

    private val _selectedProvinceForPassport = MutableStateFlow<Province?>(null)
    val selectedProvinceForPassport: StateFlow<Province?> = _selectedProvinceForPassport.asStateFlow()

    private val _isShowingTravelCv = MutableStateFlow(false)
    val isShowingTravelCv: StateFlow<Boolean> = _isShowingTravelCv.asStateFlow()

    private val _isShowingSettings = MutableStateFlow(false)
    val isShowingSettings: StateFlow<Boolean> = _isShowingSettings.asStateFlow()

    private val _selectedUserProfileTarget = MutableStateFlow<UserProfileTarget?>(null)
    val selectedUserProfileTarget: StateFlow<UserProfileTarget?> = _selectedUserProfileTarget.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _appLanguage = MutableStateFlow("vi")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _isShowingCommentSheet = MutableStateFlow(false)
    val isShowingCommentSheet: StateFlow<Boolean> = _isShowingCommentSheet.asStateFlow()

    // Remote Firestore Feed UI State & Pagination
    private val _feedUiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val feedUiState: StateFlow<FeedUiState> = _feedUiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _currentFeedPosts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _currentFeedPosts.asStateFlow()

    private var lastVisibleDoc: com.google.firebase.firestore.DocumentSnapshot? = null
    private var hasMorePages: Boolean = true

    init {
        loadInitialFeed()
    }

    fun loadInitialFeed() {
        viewModelScope.launch {
            _feedUiState.value = if (_currentFeedPosts.value.isEmpty()) FeedUiState.Loading else _feedUiState.value
            try {
                lastVisibleDoc = null
                val (fetchedPosts, lastDoc) = repository.fetchPostsPage(pageSize = 10, lastVisibleDoc = null)
                lastVisibleDoc = lastDoc
                hasMorePages = fetchedPosts.size >= 10
                _currentFeedPosts.value = fetchedPosts

                if (fetchedPosts.isEmpty()) {
                    _feedUiState.value = FeedUiState.Empty(isOffline = false)
                } else {
                    _feedUiState.value = FeedUiState.Success(
                        posts = fetchedPosts,
                        isOffline = false,
                        hasMore = hasMorePages
                    )
                }
            } catch (e: Exception) {
                val isNetworkError = e is java.io.IOException || e.message?.contains("network", ignoreCase = true) == true || e.message?.contains("offline", ignoreCase = true) == true
                if (_currentFeedPosts.value.isNotEmpty()) {
                    _feedUiState.value = FeedUiState.Success(
                        posts = _currentFeedPosts.value,
                        isOffline = true,
                        hasMore = false
                    )
                } else {
                    _feedUiState.value = FeedUiState.Error(
                        message = if (isNetworkError) "Không có kết nối mạng. Vui lòng kiểm tra lại đường truyền." else "Không thể tải bài viết (${e.localizedMessage})",
                        isOffline = isNetworkError
                    )
                }
            }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                lastVisibleDoc = null
                val (fetchedPosts, lastDoc) = repository.fetchPostsPage(pageSize = 10, lastVisibleDoc = null)
                lastVisibleDoc = lastDoc
                hasMorePages = fetchedPosts.size >= 10
                _currentFeedPosts.value = fetchedPosts

                if (fetchedPosts.isEmpty()) {
                    _feedUiState.value = FeedUiState.Empty(isOffline = false)
                } else {
                    _feedUiState.value = FeedUiState.Success(
                        posts = fetchedPosts,
                        isOffline = false,
                        hasMore = hasMorePages
                    )
                }
            } catch (e: Exception) {
                val isNetworkError = e is java.io.IOException || e.message?.contains("network", ignoreCase = true) == true
                if (_currentFeedPosts.value.isNotEmpty()) {
                    _feedUiState.value = FeedUiState.Success(
                        posts = _currentFeedPosts.value,
                        isOffline = true,
                        hasMore = false
                    )
                    showToast("Đang xem bài viết ngoại tuyến")
                } else {
                    _feedUiState.value = FeedUiState.Error(
                        message = if (isNetworkError) "Mất kết nối mạng. Hãy kiểm tra WiFi/4G." else "Không thể làm mới: ${e.localizedMessage}",
                        isOffline = isNetworkError
                    )
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMorePosts() {
        if (_isLoadingMore.value || !hasMorePages || lastVisibleDoc == null) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val (morePosts, lastDoc) = repository.fetchPostsPage(pageSize = 10, lastVisibleDoc = lastVisibleDoc)
                lastVisibleDoc = lastDoc
                hasMorePages = morePosts.size >= 10

                val combined = (_currentFeedPosts.value + morePosts).distinctBy { it.id }
                _currentFeedPosts.value = combined
                _feedUiState.value = FeedUiState.Success(
                    posts = combined,
                    isOffline = false,
                    hasMore = hasMorePages
                )
            } catch (e: Exception) {
                Log.w("LuminaViewModel", "Failed to load more posts: ${e.message}")
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun retryFeed() {
        loadInitialFeed()
    }
    val trips: StateFlow<List<Trip>> = repository.trips.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )
    val provinces: StateFlow<List<Province>> = repository.provinces.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )
    val places: StateFlow<List<Place>> = repository.places.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )
    val checkIns: StateFlow<List<CheckInRecord>> = repository.checkIns.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )
    val userProfile: StateFlow<UserProfile> = repository.userProfile.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        UserProfile(
            id = "u_1",
            username = "linh.travels",
            displayName = "Thảo Linh",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
        )
    )
    val savedBoards: StateFlow<List<SavedBoard>> = repository.savedBoards.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )
    val frames: StateFlow<List<LuminaFrame>> = repository.frames.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    // Follow / Social State
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val followedTargetKeys: StateFlow<Set<String>> = userProfile
        .flatMapLatest { profile ->
            repository.getFollowedTargetKeysFlow(profile.id)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptySet()
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val followingCount: StateFlow<Int> = userProfile
        .flatMapLatest { profile ->
            repository.getFollowingCountFlow(profile.id)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            0
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val followerCount: StateFlow<Int> = userProfile
        .flatMapLatest { profile ->
            repository.getFollowerCountFlow(profile.id)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            0
        )

    // AI Planning State
    private val _aiWizardState = MutableStateFlow(AiWizardState())
    val aiWizardState: StateFlow<AiWizardState> = _aiWizardState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "Xin chào Thảo Linh! Mình là trợ lý du lịch Levart ✨ Bạn đang dự định khám phá tỉnh thành nào ở Việt Nam? Hãy hỏi mình quán ăn ngon, outfit chụp ảnh hoặc chi phí chuyến đi nhé!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Stamp Drop Animation Trigger
    private val _stampDropTrigger = MutableStateFlow<CheckInRecord?>(null)
    val stampDropTrigger: StateFlow<CheckInRecord?> = _stampDropTrigger.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Navigation methods
    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setFeedSubTab(tab: FeedSubTab) {
        _feedSubTab.value = tab
    }

    fun setTripSubTab(tab: TripSubTab) {
        _tripSubTab.value = tab
    }

    fun openPostDetail(post: Post) {
        _selectedPost.value = post
    }

    fun closePostDetail() {
        _selectedPost.value = null
        _isShowingCommentSheet.value = false
    }

    fun openTripDetail(trip: Trip, editMode: Boolean = false) {
        _selectedTrip.value = trip
        _isEditingTrip.value = editMode
    }

    fun closeTripDetail() {
        _selectedTrip.value = null
        _isEditingTrip.value = false
    }

    fun showCreatePost(show: Boolean) {
        _isShowingCreatePost.value = show
    }

    fun showFrameCam(show: Boolean) {
        _isShowingFrameCam.value = show
    }

    fun showSearch(show: Boolean) {
        _isShowingSearch.value = show
    }

    fun showNotifications(show: Boolean) {
        _isShowingNotifications.value = show
    }

    fun openStampStudioWithPlace(place: Place?, province: Province?) {
        _targetedStampPlace.value = place
        _targetedStampProvince.value = province
        selectTab(MainTab.CHECK_IN)
    }

    fun clearTargetedStamp() {
        _targetedStampPlace.value = null
        _targetedStampProvince.value = null
    }

    fun showCityPassport(show: Boolean, province: Province? = null) {
        _isShowingCityPassport.value = show
        _selectedProvinceForPassport.value = province
    }

    fun showTravelCv(show: Boolean) {
        _isShowingTravelCv.value = show
    }

    fun openUserProfile(
        userId: String,
        username: String = "",
        displayName: String = "",
        avatarUrl: String = "",
        bio: String = ""
    ) {
        _selectedUserProfileTarget.value = UserProfileTarget(
            userId = userId,
            username = username.removePrefix("@"),
            displayName = displayName,
            avatarUrl = avatarUrl,
            bio = bio
        )
    }

    fun closeUserProfile() {
        _selectedUserProfileTarget.value = null
    }

    fun getUserProfileFlow(userId: String, username: String): Flow<UserProfile?> =
        repository.getUserProfileFlow(userId, username)

    fun getUserFollowerCountFlow(userId: String): Flow<Int> =
        repository.getFollowerCountFlow(userId)

    fun getUserFollowingCountFlow(userId: String): Flow<Int> =
        repository.getFollowingCountFlow(userId)

    fun showCommentSheet(show: Boolean) {
        _isShowingCommentSheet.value = show
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(3000)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    // Interactions
    fun toggleLike(postId: String) {
        val current = _currentFeedPosts.value.find { it.id == postId }
            ?: _selectedPost.value?.takeIf { it.id == postId }
            ?: return

        viewModelScope.launch {
            val updatedPost = repository.toggleLike(postId, current)
            _currentFeedPosts.value = _currentFeedPosts.value.map { if (it.id == postId) updatedPost else it }
            if (_selectedPost.value?.id == postId) {
                _selectedPost.value = updatedPost
            }
            // Update FeedUiState if needed
            val currentState = _feedUiState.value
            if (currentState is FeedUiState.Success) {
                _feedUiState.value = currentState.copy(posts = _currentFeedPosts.value)
            }
        }
    }

    fun toggleSave(postId: String) {
        val current = _currentFeedPosts.value.find { it.id == postId }
            ?: _selectedPost.value?.takeIf { it.id == postId }
            ?: return

        viewModelScope.launch {
            val updatedPost = repository.toggleSave(postId, current)
            _currentFeedPosts.value = _currentFeedPosts.value.map { if (it.id == postId) updatedPost else it }
            if (_selectedPost.value?.id == postId) {
                _selectedPost.value = updatedPost
            }
            val currentState = _feedUiState.value
            if (currentState is FeedUiState.Success) {
                _feedUiState.value = currentState.copy(posts = _currentFeedPosts.value)
            }
            showToast(if (updatedPost.isSaved) "Đã lưu vào bộ sưu tập!" else "Đã bỏ lưu bài viết")
        }
    }

    fun sharePost(postId: String) {
        repository.sharePost(postId)
        showToast("Đã sao chép liên kết chia sẻ!")
    }

    fun addComment(postId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            try {
                repository.addComment(postId, content)
                // Increment comment count locally for immediate feedback
                _currentFeedPosts.value = _currentFeedPosts.value.map { post ->
                    if (post.id == postId) post.copy(commentCount = post.commentCount + 1) else post
                }
                if (_selectedPost.value?.id == postId) {
                    _selectedPost.value = _selectedPost.value?.copy(commentCount = (_selectedPost.value?.commentCount ?: 0) + 1)
                }
                val currentState = _feedUiState.value
                if (currentState is FeedUiState.Success) {
                    _feedUiState.value = currentState.copy(posts = _currentFeedPosts.value)
                }
                showToast("Đã đăng bình luận!")
            } catch (e: Exception) {
                showToast("Không thể gửi bình luận: ${e.localizedMessage}")
            }
        }
    }

    fun getCommentsForPost(postId: String): Flow<List<Comment>> = repository.getCommentsForPost(postId)

    fun toggleFollow(
        authorId: String,
        authorHandle: String = "",
        authorName: String = "",
        authorAvatarUrl: String = ""
    ) {
        val currentUserId = userProfile.value.id
        if (currentUserId.isNotBlank() && currentUserId == authorId) {
            showToast("Bạn không thể tự theo dõi chính mình")
            return
        }
        viewModelScope.launch {
            val isNowFollowing = repository.toggleFollow(
                followerId = currentUserId,
                followedId = authorId
            )
            val displayName = authorName.ifBlank { authorHandle.ifBlank { "người dùng" } }
            showToast(if (isNowFollowing) "Đã theo dõi $displayName" else "Đã bỏ theo dõi $displayName")
        }
    }

    fun publishPost(
        caption: String,
        location: String,
        trip: Trip?,
        mediaUrls: List<String> = emptyList(),
        aspectRatio: Float = 0.8f
    ) {
        viewModelScope.launch {
            try {
                val newPost = repository.createPost(
                    caption = caption,
                    mediaUrls = mediaUrls.ifEmpty {
                        listOf("https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop")
                    },
                    locationLabel = location,
                    provinceCode = "LDG",
                    attachedTrip = trip,
                    aspectRatio = aspectRatio
                )
                // Insert newly created post at top of feed
                val updatedList = listOf(newPost) + _currentFeedPosts.value
                _currentFeedPosts.value = updatedList
                _feedUiState.value = FeedUiState.Success(
                    posts = updatedList,
                    isOffline = false,
                    hasMore = hasMorePages
                )
                showCreatePost(false)
                showToast("Đã chia sẻ chuyến đi lên cộng đồng Levart!")
            } catch (e: Exception) {
                showToast("Không thể đăng bài: ${e.localizedMessage}")
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                repository.deletePost(postId)
                _currentFeedPosts.value = _currentFeedPosts.value.filter { it.id != postId }
                if (_selectedPost.value?.id == postId) {
                    _selectedPost.value = null
                }
                val updated = _currentFeedPosts.value
                _feedUiState.value = if (updated.isEmpty()) {
                    FeedUiState.Empty(isOffline = false)
                } else {
                    FeedUiState.Success(posts = updated, isOffline = false, hasMore = hasMorePages)
                }
                showToast("Đã xóa bài viết")
            } catch (e: Exception) {
                showToast("Không thể xóa bài: ${e.localizedMessage}")
            }
        }
    }

    // Check-in
    fun performQuickStamp(placeName: String = "Đồi Chè Cầu Đất", provinceCode: String = "LDG", provinceName: String = "Lâm Đồng", photoUrl: String? = null) {
        val record = repository.performQuickCheckIn(placeName, provinceCode, provinceName, photoUrl)
        _stampDropTrigger.value = record
        showToast("Đã đóng dấu hộ chiếu: $placeName!")
        viewModelScope.launch {
            delay(2500)
            _stampDropTrigger.value = null
        }
    }

    // Trip actions
    fun saveTrip(trip: Trip) {
        repository.saveTrip(trip)
        showToast("Đã lưu chuyến đi thành công!")
        _selectedTrip.value = trip
    }

    fun updateTripFromForm(trip: Trip) {
        repository.saveTrip(trip)
        _selectedTrip.value = trip
        _isEditingTrip.value = false
        showToast("Đã cập nhật lịch trình chuyến đi!")
    }

    fun duplicateTrip(tripId: String) {
        repository.duplicateTrip(tripId) { dup ->
            _selectedTrip.value = dup
            showToast("Đã nhân bản chuyến đi thành công!")
        }
    }

    fun deleteTrip(tripId: String) {
        repository.deleteTrip(tripId)
        closeTripDetail()
        showToast("Đã xoá chuyến đi.")
    }

    fun saveManualTrip(trip: Trip) {
        repository.saveTrip(trip)
        openTripDetail(trip, editMode = false)
        showToast("Đã lưu lịch trình thành công!")
    }

    fun createManualTrip(
        title: String,
        destination: String,
        durationDays: Int,
        budgetVnd: Long,
        notes: String
    ) {
        val days = (1..durationDays).map { dayNum ->
            com.example.data.model.TripDay(
                dayNumber = dayNum,
                dateText = "Ngày $dayNum",
                title = "Ngày $dayNum: Khám phá $destination",
                items = listOf(
                    com.example.data.model.TripItem(
                        timeSlot = "08:00 - 10:00",
                        title = "Ăn sáng & khởi hành tại trung tâm $destination",
                        description = "Thưởng thức đặc sản địa phương",
                        locationName = destination,
                        costVnd = 50000L
                    ),
                    com.example.data.model.TripItem(
                        timeSlot = "14:00 - 17:00",
                        title = "Tham quan địa điểm nổi bật",
                        description = "Check-in ngắm cảnh đẹp $destination",
                        locationName = destination,
                        costVnd = 100000L
                    )
                )
            )
        }
        val newTrip = Trip(
            id = "trip_manual_${System.currentTimeMillis()}",
            title = if (title.isNotBlank()) title else "Chuyến đi $destination",
            destination = destination,
            provinceCode = "VN",
            coverUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
            startDate = "Hôm nay",
            endDate = "Ngày $durationDays",
            durationText = "${durationDays}N${durationDays - 1}Đ",
            totalEstimatedCostVnd = budgetVnd,
            styleTags = listOf("Tự tạo", "Khám phá"),
            isAiGenerated = false,
            days = days
        )
        repository.saveTrip(newTrip)
        openTripDetail(newTrip, editMode = true)
        showToast("Đã tạo lịch trình thủ công thành công!")
    }

    // AI Wizard
    fun updateAiWizard(
        destination: String? = null,
        durationDays: Int? = null,
        budgetVnd: Long? = null,
        groupType: String? = null,
        styleTags: List<String>? = null,
        customNotes: String? = null,
        step: Int? = null
    ) {
        _aiWizardState.value = _aiWizardState.value.copy(
            destination = destination ?: _aiWizardState.value.destination,
            durationDays = durationDays ?: _aiWizardState.value.durationDays,
            budgetVnd = budgetVnd ?: _aiWizardState.value.budgetVnd,
            groupType = groupType ?: _aiWizardState.value.groupType,
            styleTags = styleTags ?: _aiWizardState.value.styleTags,
            customNotes = customNotes ?: _aiWizardState.value.customNotes,
            step = step ?: _aiWizardState.value.step
        )
    }

    fun generateAiPlan() {
        viewModelScope.launch {
            _aiWizardState.value = _aiWizardState.value.copy(
                isGenerating = true,
                generationProgressMessage = "Đang chọn địa điểm đẹp tại ${_aiWizardState.value.destination}..."
            )
            delay(600)
            _aiWizardState.value = _aiWizardState.value.copy(
                generationProgressMessage = "Đang sắp xếp khung giờ săn mây & hoàng hôn..."
            )
            delay(700)
            _aiWizardState.value = _aiWizardState.value.copy(
                generationProgressMessage = "Đang tối ưu hoá chi phí & liên kết dịch vụ..."
            )

            val generated = repository.generateAiTrip(
                destination = _aiWizardState.value.destination,
                durationDays = _aiWizardState.value.durationDays,
                budgetVnd = _aiWizardState.value.budgetVnd,
                groupType = _aiWizardState.value.groupType,
                styleTags = _aiWizardState.value.styleTags,
                customNotes = _aiWizardState.value.customNotes
            )

            _aiWizardState.value = _aiWizardState.value.copy(
                isGenerating = false,
                generatedTrip = generated
            )

            // Open trip detail in preview-ai mode
            openTripDetail(generated, editMode = false)
        }
    }

    // AI Chat
    fun sendChatMessage(text: String, plugin: String? = null, contextInfo: String? = null) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            val destinationContext = contextInfo ?: _aiWizardState.value.destination
            val responseText = repository.askAi(
                question = text,
                destination = destinationContext,
                plugin = plugin
            )
            val aiMsg = ChatMessage(isUser = false, text = responseText)
            _chatMessages.value = _chatMessages.value + aiMsg
        }
    }

    // Google Sign-In & Session Handling
    fun signInWithGoogle(activity: Activity) {
        if (_isLoggingIn.value) return
        _isLoggingIn.value = true
        _loginError.value = null
        _loginErrorIsNetwork.value = false
        _loginCanFallbackToBrowser.value = false

        viewModelScope.launch {
            try {
                when (val result = repository.signInWithGoogle(activity, allowBrowserFallback = true)) {
                    is AuthResult.Success -> {
                        _isLoggingIn.value = false
                        _loginError.value = null
                        _loginCanFallbackToBrowser.value = false
                        if (result.isUpgrade) {
                            showToast("Đã liên kết tài khoản Google thành công! Dữ liệu hành trình đã được lưu vĩnh viễn 🎉")
                        } else {
                            showToast("Đăng nhập thành công! Chào mừng ${result.userProfile.displayName} 🇻🇳")
                        }
                    }
                    is AuthResult.Cancelled -> {
                        _isLoggingIn.value = false
                        _loginCanFallbackToBrowser.value = false
                        // Do not show error banner on user cancel
                    }
                    is AuthResult.AccountCollision -> {
                        _isLoggingIn.value = false
                        _loginCanFallbackToBrowser.value = false
                        _accountCollisionEmail.value = result.email
                        _accountCollisionPendingCredential.value = result.credential
                    }
                    is AuthResult.Error -> {
                        _isLoggingIn.value = false
                        _loginError.value = result.message
                        _loginErrorIsNetwork.value = result.isNetworkError
                        _loginCanFallbackToBrowser.value = result.canFallbackToBrowser || result.isNoCredential
                    }
                }
            } catch (e: Exception) {
                _isLoggingIn.value = false
                _loginError.value = "Đã xảy ra lỗi không mong muốn: ${e.localizedMessage ?: "Vui lòng thử lại"}"
                _loginCanFallbackToBrowser.value = true
            }
        }
    }

    fun signInWithGoogleBrowser(activity: Activity) {
        if (_isLoggingIn.value) return
        _isLoggingIn.value = true
        _loginError.value = null
        _loginErrorIsNetwork.value = false
        _loginCanFallbackToBrowser.value = false

        viewModelScope.launch {
            try {
                when (val result = repository.signInWithGoogleBrowser(activity)) {
                    is AuthResult.Success -> {
                        _isLoggingIn.value = false
                        _loginError.value = null
                        _loginCanFallbackToBrowser.value = false
                        if (result.isUpgrade) {
                            showToast("Đã liên kết tài khoản Google thành công! Dữ liệu hành trình đã được lưu vĩnh viễn 🎉")
                        } else {
                            showToast("Đăng nhập thành công! Chào mừng ${result.userProfile.displayName} 🇻🇳")
                        }
                    }
                    is AuthResult.Cancelled -> {
                        _isLoggingIn.value = false
                        _loginCanFallbackToBrowser.value = false
                        // User cancelled or closed browser cleanly
                    }
                    is AuthResult.AccountCollision -> {
                        _isLoggingIn.value = false
                        _loginCanFallbackToBrowser.value = false
                        _accountCollisionEmail.value = result.email
                        _accountCollisionPendingCredential.value = result.credential
                    }
                    is AuthResult.Error -> {
                        _isLoggingIn.value = false
                        _loginError.value = result.message
                        _loginErrorIsNetwork.value = result.isNetworkError
                        _loginCanFallbackToBrowser.value = false
                    }
                }
            } catch (e: Exception) {
                _isLoggingIn.value = false
                _loginError.value = "Lỗi khi mở trình duyệt: ${e.localizedMessage ?: "Vui lòng thử lại"}"
                _loginCanFallbackToBrowser.value = false
            }
        }
    }

    fun dismissAccountCollision() {
        _accountCollisionEmail.value = null
        _accountCollisionPendingCredential.value = null
    }

    fun confirmSwitchToExistingAccount(activity: Activity? = null) {
        val cred = _accountCollisionPendingCredential.value
        dismissAccountCollision()
        if (_isLoggingIn.value) return
        _isLoggingIn.value = true
        _loginError.value = null
        viewModelScope.launch {
            try {
                when (val result = repository.switchToExistingGoogleAccount(cred, activity)) {
                    is AuthResult.Success -> {
                        _isLoggingIn.value = false
                        showToast("Đã chuyển sang tài khoản Google: ${result.userProfile.displayName} 🇻🇳")
                    }
                    is AuthResult.Cancelled -> {
                        _isLoggingIn.value = false
                    }
                    is AuthResult.AccountCollision -> {
                        _isLoggingIn.value = false
                    }
                    is AuthResult.Error -> {
                        _isLoggingIn.value = false
                        showToast(result.message)
                    }
                }
            } catch (e: Exception) {
                _isLoggingIn.value = false
                showToast("Lỗi chuyển tài khoản: ${e.localizedMessage ?: "Vui lòng thử lại"}")
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (_isLoggingIn.value) return
        _isLoggingIn.value = true
        _loginError.value = null
        _loginErrorIsNetwork.value = false
        _loginCanFallbackToBrowser.value = false

        viewModelScope.launch {
            try {
                when (val result = repository.signInWithEmail(email, password)) {
                    is AuthResult.Success -> {
                        _isLoggingIn.value = false
                        _loginError.value = null
                        _loginCanFallbackToBrowser.value = false
                        showToast("Đăng nhập thành công! Chào mừng ${result.userProfile.displayName} 🇻🇳")
                    }
                    is AuthResult.Cancelled -> {
                        _isLoggingIn.value = false
                    }
                    is AuthResult.AccountCollision -> {
                        _isLoggingIn.value = false
                    }
                    is AuthResult.Error -> {
                        _isLoggingIn.value = false
                        _loginError.value = result.message
                        _loginErrorIsNetwork.value = result.isNetworkError
                        _loginCanFallbackToBrowser.value = false
                    }
                }
            } catch (e: Exception) {
                _isLoggingIn.value = false
                _loginError.value = "Lỗi đăng nhập: ${e.localizedMessage ?: "Vui lòng thử lại"}"
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, displayName: String) {
        if (_isLoggingIn.value) return
        _isLoggingIn.value = true
        _loginError.value = null
        _loginErrorIsNetwork.value = false
        _loginCanFallbackToBrowser.value = false

        viewModelScope.launch {
            try {
                when (val result = repository.signUpWithEmail(email, password, displayName)) {
                    is AuthResult.Success -> {
                        _isLoggingIn.value = false
                        _loginError.value = null
                        _loginCanFallbackToBrowser.value = false
                        showToast("Đăng ký thành công! Chào mừng ${result.userProfile.displayName} đến với Lumina 🇻🇳")
                    }
                    is AuthResult.Cancelled -> {
                        _isLoggingIn.value = false
                    }
                    is AuthResult.AccountCollision -> {
                        _isLoggingIn.value = false
                    }
                    is AuthResult.Error -> {
                        _isLoggingIn.value = false
                        _loginError.value = result.message
                        _loginErrorIsNetwork.value = result.isNetworkError
                        _loginCanFallbackToBrowser.value = false
                    }
                }
            } catch (e: Exception) {
                _isLoggingIn.value = false
                _loginError.value = "Lỗi đăng ký: ${e.localizedMessage ?: "Vui lòng thử lại"}"
            }
        }
    }

    fun signInAsGuest(customName: String = "Lữ khách Khám Phá") {
        if (_isLoggingIn.value) return
        _isLoggingIn.value = true
        _loginError.value = null
        _loginErrorIsNetwork.value = false
        _loginCanFallbackToBrowser.value = false

        viewModelScope.launch {
            try {
                when (val result = repository.signInAsGuest(customName)) {
                    is AuthResult.Success -> {
                        _isLoggingIn.value = false
                        _loginError.value = null
                        _loginCanFallbackToBrowser.value = false
                        showToast("Bắt đầu hành trình với tư cách ${result.userProfile.displayName} 🇻🇳")
                    }
                    is AuthResult.Cancelled -> {
                        _isLoggingIn.value = false
                    }
                    is AuthResult.AccountCollision -> {
                        _isLoggingIn.value = false
                    }
                    is AuthResult.Error -> {
                        _isLoggingIn.value = false
                        _loginError.value = result.message
                        _loginErrorIsNetwork.value = result.isNetworkError
                        _loginCanFallbackToBrowser.value = false
                    }
                }
            } catch (e: Exception) {
                _isLoggingIn.value = false
                _loginError.value = "Lỗi kích hoạt: ${e.localizedMessage ?: "Vui lòng thử lại"}"
            }
        }
    }

    fun clearLoginError() {
        _loginError.value = null
        _loginErrorIsNetwork.value = false
        _loginCanFallbackToBrowser.value = false
    }

    // Settings & Account Management
    fun showSettings(show: Boolean) {
        _isShowingSettings.value = show
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        showToast(if (enabled) "Đã kích hoạt Giao diện Tối (Dark Mode)" else "Đã chuyển sang Giao diện Sáng (Light Mode)")
    }

    fun setLanguage(lang: String) {
        _appLanguage.value = lang
        showToast(if (lang == "vi") "Đã đổi ngôn ngữ sang Tiếng Việt 🇻🇳" else "Language changed to English 🇬🇧")
    }

    fun updateProfileInfo(displayName: String, bio: String, tagTitle: String) {
        val current = userProfile.value
        val updated = current.copy(
            displayName = displayName,
            bio = bio,
            tagTitle = tagTitle,
            isCustomName = true
        )
        repository.updateUserProfile(updated)
        repository.authManager.updateSavedUserProfile(updated)
        showToast("Đã lưu cập nhật hồ sơ!")
    }

    fun clearCache() {
        showToast("Đã dọn dẹp sạch 48.6 MB bộ nhớ đệm hình ảnh và dữ liệu tạm!")
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _isShowingSettings.value = false
            showToast("Đã đăng xuất tài khoản.")
        }
    }

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount.asStateFlow()

    fun deleteAccount(activity: Activity) {
        if (_isDeletingAccount.value) return
        _isDeletingAccount.value = true

        viewModelScope.launch {
            try {
                when (val result = repository.deleteUserAccount(activity)) {
                    is DeleteAccountResult.Success -> {
                        _isDeletingAccount.value = false
                        _isShowingSettings.value = false
                        _selectedTrip.value = null
                        _selectedPost.value = null
                        _selectedProvinceForPassport.value = null
                        _isShowingCreatePost.value = false
                        _isShowingSearch.value = false
                        _isShowingNotifications.value = false
                        _isShowingCityPassport.value = false
                        _isShowingTravelCv.value = false
                        _currentTab.value = MainTab.HOME
                        showToast("Đã xoá tài khoản và toàn bộ dữ liệu cá nhân vĩnh viễn.")
                    }
                    is DeleteAccountResult.Cancelled -> {
                        _isDeletingAccount.value = false
                    }
                    is DeleteAccountResult.Error -> {
                        _isDeletingAccount.value = false
                        showToast(result.message)
                    }
                }
            } catch (e: Exception) {
                _isDeletingAccount.value = false
                showToast("Lỗi khi xoá tài khoản: ${e.localizedMessage ?: "Vui lòng thử lại sau"}")
            }
        }
    }
}
