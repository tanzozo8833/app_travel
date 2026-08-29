package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.CheckInRecord
import com.example.data.model.Place
import com.example.data.model.Post
import com.example.data.model.Province
import com.example.data.model.Trip
import com.example.ui.FeedSubTab
import com.example.ui.FeedUiState
import com.example.ui.components.PostCard
import com.example.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

private fun computeGpsDistanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Int {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return (r * c).toInt()
}

@Composable
fun HomeScreen(
    posts: List<Post>,
    provinces: List<Province>,
    places: List<Place> = emptyList(),
    checkIns: List<CheckInRecord> = emptyList(),
    followedTargetKeys: Set<String> = emptySet(),
    currentUserId: String = "",
    currentUsername: String = "",
    currentSubTab: FeedSubTab,
    feedUiState: FeedUiState = FeedUiState.Success(posts),
    isRefreshing: Boolean = false,
    isLoadingMore: Boolean = false,
    onSubTabChanged: (FeedSubTab) -> Unit,
    onRefreshFeed: () -> Unit = {},
    onLoadMorePosts: () -> Unit = {},
    onRetryFeed: () -> Unit = {},
    onCreatePostClick: () -> Unit = {},
    onFollowClick: (Post) -> Unit = {},
    onAuthorClick: (Post) -> Unit = {},
    onPostClick: (Post) -> Unit,
    onTripClick: (Trip) -> Unit,
    onLikeClick: (Post) -> Unit,
    onSaveClick: (Post) -> Unit,
    onShareClick: (Post) -> Unit,
    onCommentClick: (Post) -> Unit,
    onQuickCheckInClick: (place: Place?, province: Province?) -> Unit,
    onOpenStampCamera: (place: Place?, province: Province?) -> Unit = { _, _ -> },
    onProvinceClick: (Province) -> Unit,
    onPlanTripWithAi: (destination: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val gridState = androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState()

    // Infinite scroll detection
    LaunchedEffect(gridState, isLoadingMore) {
        snapshotFlow { gridState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (totalItems > 0 && lastVisibleIndex >= totalItems - 3 && !isLoadingMore) {
                    onLoadMorePosts()
                }
            }
    }

    // Real GPS States
    var detectedLat by remember { mutableStateOf<Double?>(null) }
    var detectedLng by remember { mutableStateOf<Double?>(null) }
    var detectedProvince by remember { mutableStateOf<Province?>(null) }
    var detectedPlace by remember { mutableStateOf<Place?>(null) }
    var distanceMeters by remember { mutableStateOf<Int?>(null) }
    var isSearchingGps by remember { mutableStateOf(false) }
    var gpsErrorMessage by remember { mutableStateOf<String?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    var locationJob by remember { mutableStateOf<Job?>(null) }

    fun refreshGpsLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            hasLocationPermission = false
            gpsErrorMessage = "Chưa cấp quyền vị trí"
            return
        }
        hasLocationPermission = true

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = locationManager?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                it.isLocationEnabled
            } else {
                it.isProviderEnabled(LocationManager.GPS_PROVIDER) || it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }
        } ?: false

        if (!isGpsEnabled) {
            gpsErrorMessage = "Dịch vụ GPS đang tắt"
            return
        }

        locationJob?.cancel()
        isSearchingGps = true
        gpsErrorMessage = null

        locationJob = coroutineScope.launch {
            try {
                var foundLocation: Location? = null
                val tokenSource = CancellationTokenSource()

                withTimeoutOrNull(8000L) {
                    try {
                        val task = fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            tokenSource.token
                        )
                        val loc = withContext(Dispatchers.IO) {
                            com.google.android.gms.tasks.Tasks.await(task)
                        }
                        if (loc != null) {
                            foundLocation = loc
                        }
                    } catch (e: Exception) {
                        Log.w("HomeScreenGPS", "getCurrentLocation error: ${e.message}")
                    }
                }

                if (foundLocation == null) {
                    try {
                        val lastLocTask = fusedLocationClient.lastLocation
                        val lastLoc = withContext(Dispatchers.IO) {
                            com.google.android.gms.tasks.Tasks.await(lastLocTask)
                        }
                        if (lastLoc != null) {
                            foundLocation = lastLoc
                        }
                    } catch (e: Exception) {
                        Log.w("HomeScreenGPS", "lastLocation error: ${e.message}")
                    }
                }

                val loc = foundLocation
                if (loc != null) {
                    val lat = loc.latitude
                    val lng = loc.longitude
                    detectedLat = lat
                    detectedLng = lng

                    // Find closest Province
                    val nearestProv = provinces.minByOrNull { prov ->
                        computeGpsDistanceMeters(lat, lng, prov.centerLat, prov.centerLng)
                    }
                    detectedProvince = nearestProv

                    // Find closest Place
                    val nearestPl = places.minByOrNull { pl ->
                        val pLat = if (pl.lat != 0.0) pl.lat else (provinces.find { it.code == pl.provinceCode }?.centerLat ?: 0.0)
                        val pLng = if (pl.lng != 0.0) pl.lng else (provinces.find { it.code == pl.provinceCode }?.centerLng ?: 0.0)
                        computeGpsDistanceMeters(lat, lng, pLat, pLng)
                    }
                    detectedPlace = nearestPl

                    if (nearestPl != null) {
                        val pLat = if (nearestPl.lat != 0.0) nearestPl.lat else (provinces.find { it.code == nearestPl.provinceCode }?.centerLat ?: 0.0)
                        val pLng = if (nearestPl.lng != 0.0) nearestPl.lng else (provinces.find { it.code == nearestPl.provinceCode }?.centerLng ?: 0.0)
                        distanceMeters = computeGpsDistanceMeters(lat, lng, pLat, pLng)
                    } else {
                        distanceMeters = null
                    }
                    gpsErrorMessage = null
                } else {
                    gpsErrorMessage = "Chưa nhận được tín hiệu GPS"
                }
            } catch (e: Exception) {
                gpsErrorMessage = e.message
            } finally {
                isSearchingGps = false
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            hasLocationPermission = true
            refreshGpsLocation()
        } else {
            hasLocationPermission = false
            gpsErrorMessage = "Cần quyền vị trí để định vị GPS thật"
        }
    }

    // Auto-fetch real location on mount or when provinces/places are loaded
    LaunchedEffect(provinces.size, places.size) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            hasLocationPermission = true
            refreshGpsLocation()
        } else {
            // Automatically request location permission for seamless real GPS experience
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
    ) {
        // Sub Tabs: Khám phá | Đang theo dõi | Gần bạn
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeedTabPill(
                label = "Khám phá",
                isSelected = currentSubTab == FeedSubTab.EXPLORE,
                onClick = { onSubTabChanged(FeedSubTab.EXPLORE) }
            )
            FeedTabPill(
                label = "Đang theo dõi",
                isSelected = currentSubTab == FeedSubTab.FOLLOWING,
                onClick = { onSubTabChanged(FeedSubTab.FOLLOWING) }
            )
            FeedTabPill(
                label = if (detectedProvince != null) "Gần bạn (${detectedProvince?.shortName})" else "Gần bạn",
                isSelected = currentSubTab == FeedSubTab.NEARBY,
                onClick = { onSubTabChanged(FeedSubTab.NEARBY) }
            )
        }

        // 2-Column Staggered Grid with rounded post cards and elegant small gap spacing:
        LazyVerticalStaggeredGrid(
            state = gridState,
            columns = StaggeredGridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Offline Warning Notice if offline
            val isOffline = (feedUiState as? FeedUiState.Success)?.isOffline == true || (feedUiState as? FeedUiState.Error)?.isOffline == true
            if (isOffline) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3CD),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFEEBA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("⚡", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Đang xem bài viết lưu tạm (Ngoại tuyến)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF856404),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            TextButton(
                                onClick = onRetryFeed,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Thử lại", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Brand700)
                            }
                        }
                    }
                }
            }

            // 1. Hero Banner: Real GPS Passport Stamp Prompt with Intelligent Action State
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                    val activePlace = detectedPlace ?: places.firstOrNull { it.provinceCode == "LDG" } ?: places.firstOrNull()
                    val activeProvince = detectedProvince ?: provinces.firstOrNull { it.code == "LDG" } ?: provinces.firstOrNull()

                    val isAlreadyStamped = remember(activePlace, checkIns) {
                        if (activePlace == null) false
                        else {
                            activePlace.isVisited || checkIns.any {
                                it.placeName.trim().equals(activePlace.name.trim(), ignoreCase = true) ||
                                (it.provinceCode == activePlace.provinceCode && it.placeName.contains(activePlace.name, ignoreCase = true))
                            }
                        }
                    }

                    HeroCheckInBanner(
                        detectedProvince = activeProvince,
                        detectedPlace = activePlace,
                        detectedLat = detectedLat,
                        detectedLng = detectedLng,
                        distanceMeters = distanceMeters,
                        isSearchingGps = isSearchingGps,
                        hasPermission = hasLocationPermission,
                        isAlreadyStamped = isAlreadyStamped,
                        onRequestPermission = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        onRefreshGps = {
                            if (!hasLocationPermission) {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                refreshGpsLocation()
                            }
                        },
                        onQuickCheckIn = {
                            onQuickCheckInClick(activePlace, activeProvince)
                        },
                        onOpenStampCamera = {
                            onOpenStampCamera(activePlace, activeProvince)
                        }
                    )
                }
            }

            // 2. Province Stories / Stamp Ring Tray
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(modifier = Modifier.padding(vertical = 2.dp)) {
                    ProvinceStoryTray(
                        provinces = provinces,
                        onProvinceClick = onProvinceClick
                    )
                }
            }

            // 3. Filtered Posts: 2-Column Staggered Waterfall Layout
            val targetProvinceCode = detectedProvince?.code ?: "LDG"
            val filteredPosts = when (currentSubTab) {
                FeedSubTab.EXPLORE -> posts
                FeedSubTab.FOLLOWING -> posts.filter { post ->
                    post.userId in followedTargetKeys || (post.authorHandle.isNotBlank() && post.authorHandle in followedTargetKeys)
                }
                FeedSubTab.NEARBY -> {
                    val nearbyPosts = posts.filter { it.provinceCode == targetProvinceCode }
                    if (nearbyPosts.isNotEmpty()) nearbyPosts else posts
                }
            }

            // First Loading State (if no posts loaded yet)
            if (feedUiState is FeedUiState.Loading && posts.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = Brand700,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Đang tải bài viết từ cộng đồng Levart...",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Error State (if failed and no posts loaded)
            if (feedUiState is FeedUiState.Error && posts.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8EFEA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFDE8E8),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color(0xFFE02424),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Không thể tải bảng tin",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = feedUiState.message,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onRetryFeed,
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Brand700)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Thử lại", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Empty Community Feed State
            if (currentSubTab == FeedSubTab.EXPLORE && (feedUiState is FeedUiState.Empty || (feedUiState is FeedUiState.Success && filteredPosts.isEmpty()))) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE6E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 20.dp)
                            .testTag("empty_community_feed")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Brand100,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = null,
                                        tint = Brand700,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Bảng tin cộng đồng đang trống",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Chưa có bài viết nào trên máy chủ. Hãy là người đầu tiên chia sẻ chuyến đi tuyệt vời của bạn!",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onCreatePostClick,
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                modifier = Modifier.testTag("post_first_button")
                            ) {
                                Text(
                                    text = "Đăng bài đầu tiên ngay ➔",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (currentSubTab == FeedSubTab.FOLLOWING && filteredPosts.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE6E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 20.dp)
                            .testTag("empty_following_prompt")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Brand100,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.People,
                                        contentDescription = null,
                                        tint = Brand700,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Bạn chưa theo dõi ai",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Bạn chưa theo dõi ai, hãy khám phá thêm ở tab Khám phá.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onSubTabChanged(FeedSubTab.EXPLORE) },
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                modifier = Modifier.testTag("explore_tab_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = Gold500,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Khám phá ngay ➔",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    val isFollowing = post.userId in followedTargetKeys || (post.authorHandle.isNotBlank() && post.authorHandle in followedTargetKeys)
                    val isSelf = post.userId == currentUserId || (post.authorHandle.isNotBlank() && (post.authorHandle.equals(currentUsername, ignoreCase = true) || post.authorHandle.equals("@$currentUsername", ignoreCase = true)))

                    PostCard(
                        post = post,
                        isFollowing = isFollowing,
                        isSelf = isSelf,
                        onFollowClick = { onFollowClick(post) },
                        onAuthorClick = onAuthorClick,
                        onPostClick = onPostClick,
                        onTripClick = onTripClick,
                        onLikeClick = onLikeClick,
                        onSaveClick = onSaveClick,
                        onShareClick = onShareClick,
                        onCommentClick = onCommentClick
                    )
                }
            }

            // Pagination loading indicator item at bottom
            if (isLoadingMore) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Brand700,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedTabPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (isSelected) Brand700 else BgSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Brand700 else Color(0xFFD6DFDB)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun HeroCheckInBanner(
    detectedProvince: Province?,
    detectedPlace: Place?,
    detectedLat: Double?,
    detectedLng: Double?,
    distanceMeters: Int?,
    isSearchingGps: Boolean,
    hasPermission: Boolean,
    isAlreadyStamped: Boolean,
    onRequestPermission: () -> Unit,
    onRefreshGps: () -> Unit,
    onQuickCheckIn: () -> Unit,
    onOpenStampCamera: () -> Unit
) {
    val provinceDisplayName = detectedProvince?.shortName ?: detectedProvince?.name ?: "LÂM ĐỒNG"
    val placeDisplayName = detectedPlace?.name ?: "Đồi Chè Cầu Đất"

    val distanceText = when {
        !hasPermission -> "Chưa cấp quyền"
        distanceMeters == null -> "Đang định vị"
        distanceMeters < 1000 -> "${distanceMeters}m"
        else -> String.format(Locale.US, "%.1fkm", distanceMeters / 1000.0)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Brand900),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Brand900, Brand700)
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (!hasPermission) Icons.Default.MyLocation else Icons.Default.GpsFixed,
                            contentDescription = "GPS Thật",
                            tint = if (!hasPermission) Color(0xFFFFB74D) else Gold500,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (!hasPermission) {
                                "GPS THỰC TẾ · CHƯA CẤP QUYỀN"
                            } else {
                                "ĐANG Ở ${provinceDisplayName.uppercase()} · GPS THỰC TẾ"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (!hasPermission) Color(0xFFFFB74D) else Gold500,
                            letterSpacing = 0.5.sp
                        )
                        if (isSearchingGps) {
                            CircularProgressIndicator(
                                color = Gold500,
                                strokeWidth = 1.5.dp,
                                modifier = Modifier.size(10.dp)
                            )
                        } else if (hasPermission) {
                            IconButton(
                                onClick = onRefreshGps,
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Làm mới GPS",
                                    tint = Gold500.copy(alpha = 0.8f),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (!hasPermission) "Định vị vị trí hiện tại của bạn" else "Gần $placeDisplayName ($distanceText)",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            !hasPermission -> "Chạm để cấp quyền truy cập GPS và định vị địa điểm gần bạn"
                            isAlreadyStamped -> "Đã đóng dấu hộ chiếu · Bấm chụp ảnh để lưu giữ khoảnh khắc"
                            detectedLat != null && detectedLng != null -> "Tọa độ: ${"%.4f".format(detectedLat)}°N, ${"%.4f".format(detectedLng)}°E · Chạm để đóng dấu thật"
                            else -> "Chạm để đóng dấu hộ chiếu & tích luỹ dấu chân thật"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFFC7E2D7),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action Button with 3 distinct dynamic states:
                // 1. "Truy cập GPS" if location permission not granted
                // 2. "Chụp ảnh" if place is already stamped
                // 3. "Đóng dấu" if place is not stamped yet
                if (!hasPermission) {
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = TextOnGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Truy cập GPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextOnGold
                        )
                    }
                } else if (isAlreadyStamped) {
                    Button(
                        onClick = onOpenStampCamera,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = TextOnGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Chụp ảnh",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextOnGold
                        )
                    }
                } else {
                    Button(
                        onClick = onQuickCheckIn,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = TextOnGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Đóng dấu",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextOnGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProvinceStoryTray(
    provinces: List<Province>,
    onProvinceClick: (Province) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "HỘ CHIẾU 34 TỈNH THÀNH VIỆT NAM",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Brand700,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(provinces) { province ->
                val hasVisited = province.visitedPlacesCount > 0
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onProvinceClick(province) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = province.coverUrl,
                            contentDescription = province.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (hasVisited) 2.2.dp else 1.dp,
                                    color = if (hasVisited) Gold500 else Color(0xFFD0DDD6),
                                    shape = CircleShape
                                )
                        )
                        if (hasVisited) {
                            Surface(
                                shape = CircleShape,
                                color = Brand700,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = province.stampSymbol,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = province.shortName,
                        fontSize = 10.5.sp,
                        fontWeight = if (hasVisited) FontWeight.Bold else FontWeight.Normal,
                        color = if (hasVisited) TextPrimary else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
