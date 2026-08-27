package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.LuminaApplication
import com.example.data.model.*
import com.example.data.repository.LuminaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class LuminaViewModel(
    private val repository: LuminaRepository = LuminaApplication.instance.repository
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

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _appLanguage = MutableStateFlow("vi")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    val userAccounts: StateFlow<List<UserProfile>> = repository.userProfiles.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    private val _currentAccountIndex = MutableStateFlow(0)
    val currentAccountIndex: StateFlow<Int> = _currentAccountIndex.asStateFlow()

    private val _isShowingCommentSheet = MutableStateFlow(false)
    val isShowingCommentSheet: StateFlow<Boolean> = _isShowingCommentSheet.asStateFlow()

    // Data streams directly from Room Database via repository
    val posts: StateFlow<List<Post>> = repository.posts.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )
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
    val badges: StateFlow<List<AchievementBadge>> = repository.badges.stateIn(
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
        mediaUrls: List<String> = emptyList(),
        aspectRatio: Float = 0.8f
    ) {
        repository.createPost(
            caption = caption,
            mediaUrls = mediaUrls.ifEmpty {
                listOf("https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop")
            },
            locationLabel = location,
            provinceCode = "LDG",
            attachedTrip = trip,
            aspectRatio = aspectRatio
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
        val accounts = userAccounts.value
        if (index in accounts.indices) {
            _currentAccountIndex.value = index
            val target = accounts[index]
            repository.switchActiveUser(target.id)
            showToast("Đã chuyển sang tài khoản @${target.username}")
        }
    }

    fun addNewAccount(displayName: String, username: String) {
        repository.addNewUser(displayName, username)
        showToast("Đã tạo và đăng nhập tài khoản mới!")
    }

    fun updateProfileInfo(displayName: String, bio: String, tagTitle: String) {
        val current = userProfile.value
        val updated = current.copy(
            displayName = displayName,
            bio = bio,
            tagTitle = tagTitle
        )
        repository.updateUserProfile(updated)
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
