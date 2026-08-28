package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Post
import com.example.ui.LuminaViewModel
import com.example.ui.MainTab
import com.example.ui.components.CommentBottomSheet
import com.example.ui.components.LuminaBottomBar
import com.example.ui.components.LuminaHeader
import com.example.ui.components.StampBadge
import com.example.ui.components.TravelCvDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: LuminaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()
            val isLoggingIn by viewModel.isLoggingIn.collectAsState()
            val loginError by viewModel.loginError.collectAsState()
            val loginErrorIsNetwork by viewModel.loginErrorIsNetwork.collectAsState()
            val loginCanFallbackToBrowser by viewModel.loginCanFallbackToBrowser.collectAsState()

            LuminaTheme(darkTheme = isDark) {
                if (!isLoggedIn) {
                    LoginScreen(
                        isLoading = isLoggingIn,
                        errorMessage = loginError,
                        isNetworkError = loginErrorIsNetwork,
                        canFallbackToBrowser = loginCanFallbackToBrowser,
                        onSignInWithGoogle = { activity -> viewModel.signInWithGoogle(activity) },
                        onSignInWithBrowser = { activity -> viewModel.signInWithGoogleBrowser(activity) },
                        onClearError = { viewModel.clearLoginError() }
                    )
                } else {
                    LuminaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun LuminaApp(viewModel: LuminaViewModel) {
    val context = LocalContext.current

    // State collections
    val currentTab by viewModel.currentTab.collectAsState()
    val feedSubTab by viewModel.feedSubTab.collectAsState()
    val tripSubTab by viewModel.tripSubTab.collectAsState()

    val posts by viewModel.posts.collectAsState()
    val trips by viewModel.trips.collectAsState()
    val provinces by viewModel.provinces.collectAsState()
    val places by viewModel.places.collectAsState()
    val checkIns by viewModel.checkIns.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val savedBoards by viewModel.savedBoards.collectAsState()
    val frames by viewModel.frames.collectAsState()

    val selectedPost by viewModel.selectedPost.collectAsState()
    val selectedTrip by viewModel.selectedTrip.collectAsState()
    val isShowingCreatePost by viewModel.isShowingCreatePost.collectAsState()
    val isShowingSearch by viewModel.isShowingSearch.collectAsState()
    val isShowingNotifications by viewModel.isShowingNotifications.collectAsState()
    val isShowingCityPassport by viewModel.isShowingCityPassport.collectAsState()
    val selectedProvinceForPassport by viewModel.selectedProvinceForPassport.collectAsState()
    val isShowingTravelCv by viewModel.isShowingTravelCv.collectAsState()

    val isShowingSettings by viewModel.isShowingSettings.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    val aiWizardState by viewModel.aiWizardState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val targetedStampPlace by viewModel.targetedStampPlace.collectAsState()
    val targetedStampProvince by viewModel.targetedStampProvince.collectAsState()
    val stampDropTrigger by viewModel.stampDropTrigger.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Comment Bottom Sheet State (Requirement 2)
    var commentPostTarget by remember { mutableStateOf<Post?>(null) }

    // Toast triggers
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgCanvas)) {
        // Main Screen Scaffold
        Scaffold(
            topBar = {
                LuminaHeader(
                    unreadCount = notifications.count { !it.isRead },
                    onCreatePostClick = { viewModel.showCreatePost(true) },
                    onSearchClick = { viewModel.showSearch(true) },
                    onNotificationClick = { viewModel.showNotifications(true) }
                )
            },
            bottomBar = {
                LuminaBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            posts = posts,
                            provinces = provinces,
                            places = places,
                            checkIns = checkIns,
                            currentSubTab = feedSubTab,
                            onSubTabChanged = { viewModel.setFeedSubTab(it) },
                            onPostClick = { viewModel.openPostDetail(it) },
                            onTripClick = { viewModel.openTripDetail(it) },
                            onLikeClick = { viewModel.toggleLike(it.id) },
                            onSaveClick = { viewModel.toggleSave(it.id) },
                            onShareClick = { viewModel.sharePost(it.id) },
                            onCommentClick = { post -> commentPostTarget = post },
                            onQuickCheckInClick = { detectedPlace, detectedProvince ->
                                val placeName = detectedPlace?.name ?: "Vị trí GPS thực tế"
                                val provCode = detectedProvince?.code ?: "LDG"
                                val provName = detectedProvince?.shortName ?: "Lâm Đồng"
                                val photoUrl = detectedPlace?.coverUrl ?: ""
                                viewModel.performQuickStamp(placeName, provCode, provName, photoUrl)
                            },
                            onOpenStampCamera = { detectedPlace, detectedProvince ->
                                viewModel.openStampStudioWithPlace(detectedPlace, detectedProvince)
                            },
                            onProvinceClick = { viewModel.showCityPassport(true, it) },
                            onPlanTripWithAi = { dest ->
                                viewModel.updateAiWizard(destination = dest)
                                viewModel.selectTab(MainTab.AI_PLAN)
                            }
                        )
                    }
                    MainTab.AI_PLAN -> {
                        AiPlanScreen(
                            wizardState = aiWizardState,
                            chatMessages = chatMessages,
                            onUpdateWizard = { dest, days, budget, group, styles, notes ->
                                viewModel.updateAiWizard(dest, days, budget, group, styles, notes)
                            },
                            onGeneratePlan = { viewModel.generateAiPlan() },
                            onCreateManualPlan = { title, dest, days, budget, notes ->
                                viewModel.createManualTrip(title, dest, days, budget, notes)
                            },
                            onSaveManualTrip = { trip ->
                                viewModel.saveManualTrip(trip)
                            },
                            onSendMessage = { text, plugin, context ->
                                viewModel.sendChatMessage(text, plugin, context)
                            },
                            onViewTrip = { viewModel.openTripDetail(it) }
                        )
                    }
                    MainTab.CHECK_IN -> {
                        CheckInScreen(
                            provinces = provinces,
                            places = places,
                            checkIns = checkIns,
                            frames = frames,
                            initialProvince = targetedStampProvince,
                            initialPlace = targetedStampPlace,
                            onQuickCheckIn = { place, pCode, pName, photoUrl ->
                                viewModel.performQuickStamp(place, pCode, pName, photoUrl)
                            },
                            onPublishPost = { caption, loc, photoUrl ->
                                viewModel.publishPost(
                                    caption = caption,
                                    location = loc,
                                    trip = null,
                                    mediaUrls = if (photoUrl.isNotBlank()) listOf(photoUrl) else emptyList(),
                                    aspectRatio = 1.0f
                                )
                            },
                            onProvinceClick = { prov ->
                                viewModel.showCityPassport(true, prov)
                            }
                        )
                    }
                    MainTab.MY_TRIPS -> {
                        MyTripsScreen(
                            trips = trips,
                            provinces = provinces,
                            places = places,
                            checkIns = checkIns,
                            currentSubTab = tripSubTab,
                            onSubTabChanged = { viewModel.setTripSubTab(it) },
                            onTripClick = { viewModel.openTripDetail(it) },
                            onCreateNewTrip = { viewModel.selectTab(MainTab.AI_PLAN) },
                            onQuickCheckIn = { viewModel.performQuickStamp() },
                            onMapQuickCheckIn = { place, pCode, pName ->
                                viewModel.performQuickStamp(place, pCode, pName)
                            },
                            onProvinceClick = { prov ->
                                viewModel.showCityPassport(true, prov)
                            },
                            onCreateRecapPost = { trip ->
                                viewModel.publishPost(
                                    "Tổng kết chuyến đi ${trip.title} tuyệt vời cùng Lumina!",
                                    trip.destination,
                                    trip
                                )
                            }
                        )
                    }
                    MainTab.PROFILE -> {
                        ProfileScreen(
                            userProfile = userProfile,
                            provinces = provinces,
                            trips = trips,
                            posts = posts,
                            checkIns = checkIns,
                            places = places,
                            onOpenTravelCv = { viewModel.showTravelCv(true) },
                            onOpenSettings = { viewModel.showSettings(true) },
                            onProvinceClick = { viewModel.showCityPassport(true, it) },
                            onTripClick = { viewModel.openTripDetail(it) },
                            onPostClick = { viewModel.openPostDetail(it) },
                            onCreateTrip = { viewModel.selectTab(MainTab.AI_PLAN) },
                            onExploreFeed = { viewModel.selectTab(MainTab.HOME) }
                        )
                    }
                }
            }
        }

        // Full Screen Modals & Overlays

        // 1. Post Detail Modal
        AnimatedVisibility(
            visible = selectedPost != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedPost?.let { post ->
                PostDetailScreen(
                    post = post,
                    allPosts = posts,
                    onPostClick = { viewModel.openPostDetail(it) },
                    onBack = { viewModel.closePostDetail() },
                    onTripClick = { viewModel.openTripDetail(it) },
                    onLikeClick = { viewModel.toggleLike(it.id) },
                    onSaveClick = { viewModel.toggleSave(it.id) },
                    onShareClick = { viewModel.sharePost(it.id) },
                    onCommentClick = { commentPostTarget = it },
                    onAddComment = { pId, content -> viewModel.addComment(pId, content) }
                )
            }
        }

        // 2. Trip Detail Modal (with form editing support)
        AnimatedVisibility(
            visible = selectedTrip != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedTrip?.let { trip ->
                TripDetailScreen(
                    trip = trip,
                    onBack = { viewModel.closeTripDetail() },
                    onSaveTrip = { viewModel.saveTrip(it) },
                    onUpdateTrip = { viewModel.updateTripFromForm(it) },
                    onDuplicateTrip = { viewModel.duplicateTrip(it) },
                    onShareTrip = { viewModel.showToast("Đã sao chép liên kết chia sẻ lịch trình!") },
                    onCheckInSpot = { item ->
                        viewModel.performQuickStamp(item.locationName, trip.provinceCode, trip.destination)
                    }
                )
            }
        }

        // 3. Create Post Full Screen (with device image selection)
        AnimatedVisibility(
            visible = isShowingCreatePost,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            CreatePostScreen(
                trips = trips,
                onClose = { viewModel.showCreatePost(false) },
                onPublish = { caption, loc, trip, mediaUrls, aspectRatio ->
                    viewModel.publishPost(caption, loc, trip, mediaUrls, aspectRatio)
                }
            )
        }

        // 4. Search Screen
        AnimatedVisibility(
            visible = isShowingSearch,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SearchScreen(
                places = places,
                provinces = provinces,
                onBack = { viewModel.showSearch(false) },
                onPlaceClick = { place ->
                    viewModel.showSearch(false)
                    viewModel.performQuickStamp(place.name, place.provinceCode, place.name)
                },
                onProvinceClick = { prov ->
                    viewModel.showSearch(false)
                    viewModel.showCityPassport(true, prov)
                }
            )
        }

        // 5. Notifications Screen
        AnimatedVisibility(
            visible = isShowingNotifications,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            NotificationsScreen(
                notifications = notifications,
                onBack = { viewModel.showNotifications(false) }
            )
        }

        // 6. City Passport Modal
        AnimatedVisibility(
            visible = isShowingCityPassport && selectedProvinceForPassport != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedProvinceForPassport?.let { prov ->
                CityPassportModal(
                    province = prov,
                    places = places,
                    onClose = { viewModel.showCityPassport(false) },
                    onQuickCheckIn = { place, pCode, pName ->
                        viewModel.performQuickStamp(place, pCode, pName)
                        viewModel.showCityPassport(false)
                    },
                    onPlanWithAi = { pName ->
                        viewModel.updateAiWizard(destination = pName)
                        viewModel.showCityPassport(false)
                        viewModel.selectTab(MainTab.AI_PLAN)
                    }
                )
            }
        }

        // 7. Travel CV Dialog
        if (isShowingTravelCv) {
            TravelCvDialog(
                userProfile = userProfile,
                onDismiss = { viewModel.showTravelCv(false) },
                onShare = {
                    viewModel.showToast("Đã chia sẻ Thẻ Dấu Chân Du Lịch thành công!")
                    viewModel.showTravelCv(false)
                }
            )
        }

        // 8. Settings & Account Management Screen (Requirement)
        AnimatedVisibility(
            visible = isShowingSettings,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            SettingsScreen(
                userProfile = userProfile,
                isDarkMode = isDarkMode,
                appLanguage = appLanguage,
                onBack = { viewModel.showSettings(false) },
                onToggleDarkMode = { viewModel.setDarkMode(it) },
                onSetLanguage = { viewModel.setLanguage(it) },
                onUpdateProfile = { name, bio, title -> viewModel.updateProfileInfo(name, bio, title) },
                onClearCache = { viewModel.clearCache() },
                onLogout = { viewModel.logout() },
                onDeleteAccount = { viewModel.deleteAccount() }
            )
        }

        // 9. Comment Popup Bottom Sheet (Requirement 2)
        commentPostTarget?.let { post ->
            val commentsForPost by viewModel.getCommentsForPost(post.id)
                .collectAsStateWithLifecycle(initialValue = emptyList())
            CommentBottomSheet(
                post = post,
                comments = commentsForPost,
                currentUserProfile = userProfile,
                onDismiss = { commentPostTarget = null },
                onAddComment = { pId, content ->
                    viewModel.addComment(pId, content)
                }
            )
        }

        // 9. Stamp Celebration Overlay (when user checks in)
        stampDropTrigger?.let { record ->
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "ĐÓNG DẤU THÀNH CÔNG!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Gold500,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StampBadge(
                            provinceCode = record.provinceCode,
                            provinceShortName = record.provinceName,
                            dateText = record.formattedDate,
                            symbolEmoji = "★",
                            size = 110.dp,
                            animateDrop = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = record.placeName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Hộ chiếu đã ghi nhận thêm 1 dấu chân",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
