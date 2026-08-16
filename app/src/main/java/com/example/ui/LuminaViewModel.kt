package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SeedData
import com.example.data.model.*
import com.example.data.repository.LuminaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class LuminaViewModel(
    private val repository: LuminaRepository = LuminaRepository()
) : ViewModel() {

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

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _appLanguage = MutableStateFlow("vi")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _userAccounts = MutableStateFlow(
        listOf(
            UserProfile(
                id = "u_1",
                username = "linh.travels",
                displayName = "Thảo Linh",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop",
                tagTitle = "GEN Z EXPLORER",
                bio = "Gen Z Explorer | Săn hoàng hôn & Foodtour Việt Nam 🇻🇳 ✨",
                topCityName = "Hà Nội",
                topCityCheckInCount = 4,
                visitedProvincesCount = 5,
                totalProvincesCount = 34,
                totalDistanceKm = 12500.0,
                tripsCompletedCount = 18,
                totalSavesCount = 3420
            ),
            UserProfile(
                id = "u_2",
                username = "tan.explorer",
                displayName = "Đức Tân",
                avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300&auto=format&fit=crop",
                tagTitle = "TREK MASTER",
                bio = "Chinh phục 63 tỉnh thành • Đam mê cắm trại & flycam 🏕️ 🏔️",
                topCityName = "Hà Giang",
                topCityCheckInCount = 8,
                visitedProvincesCount = 19,
                totalProvincesCount = 34,
                totalDistanceKm = 24800.0,
                tripsCompletedCount = 32,
                totalSavesCount = 8900
            )
        )
    )
    val userAccounts: StateFlow<List<UserProfile>> = _userAccounts.asStateFlow()

    private val _currentAccountIndex = MutableStateFlow(0)
    val currentAccountIndex: StateFlow<Int> = _currentAccountIndex.asStateFlow()

    private val _isShowingCommentSheet = MutableStateFlow(false)
    val isShowingCommentSheet: StateFlow<Boolean> = _isShowingCommentSheet.asStateFlow()

    // Data streams from repository
    val posts: StateFlow<List<Post>> = repository.posts
    val trips: StateFlow<List<Trip>> = repository.trips
    val provinces: StateFlow<List<Province>> = repository.provinces
    val places: StateFlow<List<Place>> = repository.places
    val checkIns: StateFlow<List<CheckInRecord>> = repository.checkIns
    val badges: StateFlow<List<AchievementBadge>> = repository.badges
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val savedBoards: StateFlow<List<SavedBoard>> = repository.savedBoards

    // AI Planning State
    private val _aiWizardState = MutableStateFlow(AiWizardState())
    val aiWizardState: StateFlow<AiWizardState> = _aiWizardState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "Xin chào Thảo Linh! Mình là trợ lý du lịch Lumina ✨ Bạn đang dự định khám phá tỉnh thành nào ở Việt Nam? Hãy hỏi mình quán ăn ngon, outfit chụp ảnh hoặc chi phí chuyến đi nhé!"
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

    fun showCityPassport(show: Boolean, province: Province? = null) {
        _isShowingCityPassport.value = show
        _selectedProvinceForPassport.value = province
    }

    fun showTravelCv(show: Boolean) {
        _isShowingTravelCv.value = show
    }

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
        repository.toggleLike(postId)
        val updated = posts.value.find { it.id == postId }
        if (_selectedPost.value?.id == postId && updated != null) {
            _selectedPost.value = updated
        }
    }

    fun toggleSave(postId: String) {
        repository.toggleSave(postId)
        val updated = posts.value.find { it.id == postId }
        if (_selectedPost.value?.id == postId && updated != null) {
            _selectedPost.value = updated
        }
        val isNowSaved = updated?.isSaved == true
        showToast(if (isNowSaved) "Đã lưu vào bộ sưu tập!" else "Đã bỏ lưu bài viết")
    }

    fun sharePost(postId: String) {
        repository.sharePost(postId)
        showToast("Đã sao chép liên kết chia sẻ!")
    }

    fun addComment(postId: String, content: String) {
        if (content.isBlank()) return
        repository.addComment(postId, content)
        val updated = posts.value.find { it.id == postId }
        if (_selectedPost.value?.id == postId && updated != null) {
            _selectedPost.value = updated
        }
        showToast("Đã đăng bình luận!")
    }

    fun publishPost(
        caption: String,
        location: String,
        trip: Trip?,
        mediaUrls: List<String> = emptyList()
    ) {
        repository.createPost(
            caption = caption,
            mediaUrls = mediaUrls.ifEmpty {
                listOf("https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop")
            },
            locationLabel = location,
            provinceCode = "LDG",
            attachedTrip = trip
        )
        showCreatePost(false)
        showToast("Đã chia sẻ chuyến đi lên cộng đồng Lumina!")
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
        val dup = repository.duplicateTrip(tripId)
        if (dup != null) {
            showToast("Đã nhân bản chuyến đi thành công!")
            _selectedTrip.value = dup
        }
    }

    fun deleteTrip(tripId: String) {
        repository.deleteTrip(tripId)
        closeTripDetail()
        showToast("Đã xoá chuyến đi.")
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
    fun sendChatMessage(text: String, plugin: String? = null) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            val responseText = repository.askAi(
                question = text,
                destination = _aiWizardState.value.destination,
                plugin = plugin
            )
            val aiMsg = ChatMessage(isUser = false, text = responseText)
            _chatMessages.value = _chatMessages.value + aiMsg
        }
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

    fun switchAccount(index: Int) {
        if (index in _userAccounts.value.indices) {
            _currentAccountIndex.value = index
            val target = _userAccounts.value[index]
            repository.updateUserProfile(target)
            showToast("Đã chuyển sang tài khoản @${target.username}")
        }
    }

    fun addNewAccount(displayName: String, username: String) {
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
        _userAccounts.value = _userAccounts.value + newAcc
        _currentAccountIndex.value = _userAccounts.value.size - 1
        repository.updateUserProfile(newAcc)
        showToast("Đã tạo và đăng nhập tài khoản @${newAcc.username}")
    }

    fun updateProfileInfo(displayName: String, bio: String, tagTitle: String) {
        val current = repository.userProfile.value
        val updated = current.copy(
            displayName = displayName,
            bio = bio,
            tagTitle = tagTitle
        )
        repository.updateUserProfile(updated)
        val idx = _currentAccountIndex.value
        val list = _userAccounts.value.toMutableList()
        if (idx in list.indices) {
            list[idx] = updated
            _userAccounts.value = list
        }
        showToast("Đã lưu cập nhật hồ sơ!")
    }

    fun clearCache() {
        showToast("Đã dọn dẹp sạch 48.6 MB bộ nhớ đệm hình ảnh và dữ liệu tạm!")
    }

    fun logout() {
        showToast("Đã đăng xuất tài khoản an toàn.")
        _isShowingSettings.value = false
    }

    fun deleteAccount() {
        showToast("Đã xoá tài khoản và dữ liệu cá nhân.")
        _isShowingSettings.value = false
    }
}
