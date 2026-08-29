package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
                        onSignInWithEmail = { email, pass -> viewModel.signInWithEmail(email, pass) },
                        onSignUpWithEmail = { email, pass, name -> viewModel.signUpWithEmail(email, pass, name) },
                        onSignInAsGuest = { name -> viewModel.signInAsGuest(name) },
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
    val followedTargetKeys by viewModel.followedTargetKeys.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()
    val followerCount by viewModel.followerCount.collectAsState()

    val selectedPost by viewModel.selectedPost.collectAsState()
    val selectedTrip by viewModel.selectedTrip.collectAsState()
    val isShowingCreatePost by viewModel.isShowingCreatePost.collectAsState()
    val isShowingSearch by viewModel.isShowingSearch.collectAsState()
    val isShowingNotifications by viewModel.isShowingNotifications.collectAsState()
    val isShowingCityPassport by viewModel.isShowingCityPassport.collectAsState()
    val selectedProvinceForPassport by viewModel.selectedProvinceForPassport.collectAsState()
    val isShowingTravelCv by viewModel.isShowingTravelCv.collectAsState()
    val selectedUserProfileTarget by viewModel.selectedUserProfileTarget.collectAsState()

    val isShowingSettings by viewModel.isShowingSettings.collectAsState()
    val isDeletingAccount by viewModel.isDeletingAccount.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isAnonymous by viewModel.isAnonymous.collectAsState()
    val accountCollisionEmail by viewModel.accountCollisionEmail.collectAsState()

    val aiWizardState by viewModel.aiWizardState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val targetedStampPlace by viewModel.targetedStampPlace.collectAsState()
    val targetedStampProvince by viewModel.targetedStampProvince.collectAsState()
    val stampDropTrigger by viewModel.stampDropTrigger.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Comment Bottom Sheet State (Requirement 2)
    var commentPostTarget by remember { mutableStateOf<Post?>(null) }

    // Android System Back Navigation hierarchy
    BackHandler(enabled = isShowingSettings) {
        viewModel.showSettings(false)
    }
    BackHandler(enabled = selectedUserProfileTarget != null && !isShowingSettings) {
        viewModel.closeUserProfile()
    }
    BackHandler(enabled = isShowingTravelCv && selectedUserProfileTarget == null && !isShowingSettings) {
        viewModel.showTravelCv(false)
    }
    BackHandler(enabled = isShowingCityPassport && !isShowingTravelCv && selectedUserProfileTarget == null && !isShowingSettings) {
        viewModel.showCityPassport(false)
    }
    BackHandler(enabled = isShowingNotifications && !isShowingCityPassport && !isShowingTravelCv && selectedUserProfileTarget == null && !isShowingSettings) {
        viewModel.showNotifications(false)
    }
    BackHandler(enabled = isShowingSearch && !isShowingNotifications && !isShowingCityPassport && !isShowingTravelCv && selectedUserProfileTarget == null && !isShowingSettings) {
        viewModel.showSearch(false)
    }
    BackHandler(enabled = isShowingCreatePost && !isShowingSearch && !isShowingNotifications && !isShowingCityPassport && !isShowingTravelCv && selectedUserProfileTarget == null && !isShowingSettings) {
        viewModel.showCreatePost(false)
    }
    BackHandler(enabled = selectedTrip != null && !isShowingCreatePost && !isShowingSearch && !isShowingNotifications && !isShowingCityPassport && !isShowingTravelCv && selectedUserProfileTarget == null && !isShowingSettings) {
        viewModel.closeTripDetail()
    }
    BackHandler(enabled = selectedPost != null && selectedTrip == null && !isShowingCreatePost && !isShowingSearch && !isShowingNotifications && !isShowingCityPassport && !isShowingTravelCv && selectedUserProfileTarget == null && !isShowingSettings) {
        viewModel.closePostDetail()
    }
    BackHandler(enabled = commentPostTarget != null) {
        commentPostTarget = null
    }
    BackHandler(enabled = currentTab != MainTab.HOME && selectedPost == null && selectedTrip == null && !isShowingCreatePost && !isShowingSearch && !isShowingNotifications && !isShowingCityPassport && !isShowingTravelCv && selectedUserProfileTarget == null && !isShowingSettings && commentPostTarget == null) {
        viewModel.selectTab(MainTab.HOME)
    }

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
                            followedTargetKeys = followedTargetKeys,
                            currentUserId = userProfile.id,
                            currentUsername = userProfile.username,
                            currentSubTab = feedSubTab,
                            onSubTabChanged = { viewModel.setFeedSubTab(it) },
                            onFollowClick = { post ->
                                viewModel.toggleFollow(
                                    authorId = post.userId,
                                    authorHandle = post.authorHandle,
                                    authorName = post.authorName,
                                    authorAvatarUrl = post.authorAvatarUrl
                                )
                            },
                            onAuthorClick = { post ->
                                viewModel.openUserProfile(
                                    userId = post.userId,
                                    username = post.authorHandle,
                                    displayName = post.authorName,
                                    avatarUrl = post.authorAvatarUrl
                                )
                            },
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
                            followingCount = followingCount,
                            followerCount = followerCount,
                            isAnonymous = isAnonymous,
                            onOpenTravelCv = { viewModel.showTravelCv(true) },
                            onOpenSettings = { viewModel.showSettings(true) },
                            onUpgradeAccount = { (context as? Activity)?.let { viewModel.signInWithGoogle(it) } },
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
                    followedTargetKeys = followedTargetKeys,
                    currentUserId = userProfile.id,
                    currentUsername = userProfile.username,
                    onFollowClick = { p ->
                        viewModel.toggleFollow(
                            authorId = p.userId,
                            authorHandle = p.authorHandle,
                            authorName = p.authorName,
                            authorAvatarUrl = p.authorAvatarUrl
                        )
                    },
                    onAuthorClick = { p ->
                        viewModel.openUserProfile(
                            userId = p.userId,
                            username = p.authorHandle,
                            displayName = p.authorName,
                            avatarUrl = p.authorAvatarUrl
                        )
                    },
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

        // 8. User Profile Modal (Requirement: View another user's profile with posts & follow)
        AnimatedVisibility(
            visible = selectedUserProfileTarget != null,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            selectedUserProfileTarget?.let { target ->
                val profileFlow = remember(target.userId, target.username) {
                    viewModel.getUserProfileFlow(target.userId, target.username)
                }
                val dbProfile by profileFlow.collectAsState(initial = null)
                val targetFollowerCountFlow = remember(target.userId) {
                    viewModel.getUserFollowerCountFlow(target.userId)
                }
                val targetFollowerCount by targetFollowerCountFlow.collectAsState(initial = 0)
                val targetFollowingCountFlow = remember(target.userId) {
                    viewModel.getUserFollowingCountFlow(target.userId)
                }
                val targetFollowingCount by targetFollowingCountFlow.collectAsState(initial = 0)

                UserProfileDetailScreen(
                    target = target,
                    dbProfile = dbProfile,
                    allPosts = posts,
                    followedTargetKeys = followedTargetKeys,
                    currentUserId = userProfile.id,
                    currentUsername = userProfile.username,
                    followerCount = targetFollowerCount,
                    followingCount = targetFollowingCount,
                    onFollowClick = { uId, handle, name, avatarUrl ->
                        viewModel.toggleFollow(
                            authorId = uId,
                            authorHandle = handle,
                            authorName = name,
                            authorAvatarUrl = avatarUrl
                        )
                    },
                    onBack = { viewModel.closeUserProfile() },
                    onPostClick = { viewModel.openPostDetail(it) },
                    onTripClick = { viewModel.openTripDetail(it) },
                    onLikeClick = { viewModel.toggleLike(it.id) },
                    onSaveClick = { viewModel.toggleSave(it.id) },
                    onShareClick = { viewModel.sharePost(it.id) },
                    onCommentClick = { commentPostTarget = it }
                )
            }
        }

        // 9. Settings & Account Management Screen (Requirement)
        AnimatedVisibility(
            visible = isShowingSettings,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            SettingsScreen(
                userProfile = userProfile,
                isDarkMode = isDarkMode,
                appLanguage = appLanguage,
                isAnonymous = isAnonymous,
                isDeletingAccount = isDeletingAccount,
                onBack = { viewModel.showSettings(false) },
                onToggleDarkMode = { viewModel.setDarkMode(it) },
                onSetLanguage = { viewModel.setLanguage(it) },
                onUpdateProfile = { name, bio, title -> viewModel.updateProfileInfo(name, bio, title) },
                onClearCache = { viewModel.clearCache() },
                onLogout = { viewModel.logout() },
                onUpgradeAccount = { (context as? Activity)?.let { viewModel.signInWithGoogle(it) } },
                onDeleteAccount = {
                    (context as? Activity)?.let { act ->
                        viewModel.deleteAccount(act)
                    }
                }
            )
        }

        // Account Collision Dialog (when linking existing Google account from Guest Mode)
        accountCollisionEmail?.let { email ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissAccountCollision() },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = Gold500.copy(alpha = 0.15f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Tài khoản tồn tại",
                                tint = Brand700,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Tài Khoản Google Đã Tồn Tại",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tài khoản Google ($email) đã có dữ liệu riêng từ trước trên hệ thống Lumina.",
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DangerRed.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚠️ Bạn có muốn chuyển sang đăng nhập vào tài khoản cũ đó không?\n\nLưu ý: Dữ liệu bạn vừa tạo ở chế độ Lữ Khách sẽ không được mang theo.",
                                fontSize = 12.sp,
                                color = DangerRed,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.confirmSwitchToExistingAccount(context as? Activity)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Đăng nhập tài khoản cũ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissAccountCollision() }) {
                        Text("Huỷ & Giữ chế độ khách", color = TextPrimary)
                    }
                }
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
