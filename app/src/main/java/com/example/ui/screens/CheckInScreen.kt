package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.CheckInRecord
import com.example.data.model.LuminaFrame
import com.example.data.model.Place
import com.example.data.model.Province
import com.example.ui.components.CheckInColorMode
import com.example.ui.components.ImageColorFilterProcessor
import com.example.ui.components.RealCameraPreview
import com.example.ui.components.StampBadge
import com.example.ui.components.VietnamMapGraphic
import com.example.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class StampScreenTab {
    CAPTURE,
    VIEW_STAMPS
}

enum class CheckInStudioMode {
    LIVE_CAMERA,
    CAPTURED_PHOTO
}

sealed class RealGpsState {
    object Idle : RealGpsState()
    object PermissionRequired : RealGpsState()
    object PermissionDenied : RealGpsState()
    object LocationDisabled : RealGpsState()
    object Searching : RealGpsState()
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float,
        val timestamp: Long,
        val detectedProvince: Province?,
        val nearestPlace: Place?,
        val distanceToNearestPlaceMeters: Int
    ) : RealGpsState()
    data class Error(val message: String) : RealGpsState()
}

@Composable
fun CheckInScreen(
    provinces: List<Province>,
    places: List<Place>,
    checkIns: List<CheckInRecord>,
    frames: List<LuminaFrame> = emptyList(),
    initialProvince: Province? = null,
    initialPlace: Place? = null,
    onQuickCheckIn: (placeName: String, provinceCode: String, provinceName: String, photoUrl: String?) -> Unit,
    onPublishPost: (caption: String, location: String, photoUrl: String) -> Unit,
    onProvinceClick: (Province) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedProvince by remember(provinces, initialProvince) {
        mutableStateOf(
            initialProvince ?: provinces.find { it.code == "LDG" } ?: provinces.firstOrNull() ?: Province(
                id = 1,
                code = "LDG",
                name = "Tỉnh Lâm Đồng",
                shortName = "Đà Lạt",
                region = "Tây Nguyên",
                centerLat = 11.94,
                centerLng = 108.45,
                coverUrl = "",
                stampSymbol = "🌲",
                highlightDescription = "",
                suggestedPlacesCount = 5,
                visitedPlacesCount = 0,
                isCompleted = false
            )
        )
    }

    var selectedPlace by remember(places, initialPlace) {
        mutableStateOf(
            initialPlace ?: places.firstOrNull { it.provinceCode == "LDG" } ?: places.firstOrNull() ?: Place(
                id = "p_1",
                provinceId = 2,
                provinceCode = "LDG",
                name = "Đồi Chè Cầu Đất",
                category = com.example.data.model.PlaceCategory.PHOTO,
                address = "Đà Lạt, Lâm Đồng",
                description = "Điểm săn mây và check-in lý tưởng",
                coverUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
                checkinRadiusMeters = 500,
                isSuggested = true,
                priceRangeVnd = 50000,
                tags = listOf("Săn mây"),
                rating = 4.9,
                isVisited = false
            )
        )
    }

    LaunchedEffect(initialProvince, initialPlace) {
        if (initialProvince != null) {
            selectedProvince = initialProvince
        }
        if (initialPlace != null) {
            selectedPlace = initialPlace
        }
    }

    // Camera State
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequested by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        permissionRequested = true
        if (!isGranted) {
            Toast.makeText(context, "Cần cấp quyền Camera để chụp ảnh check-in", Toast.LENGTH_SHORT).show()
        }
    }

    // Studio State
    var studioMode by remember { mutableStateOf(CheckInStudioMode.LIVE_CAMERA) }
    var selectedColorMode by remember { mutableStateOf(CheckInColorMode.FILM) }
    var capturedPhotoUri by remember { mutableStateOf<String?>(null) }
    val hasRealPhoto = capturedPhotoUri != null

    var wasCapturedWithFrontCamera by remember { mutableStateOf(false) }
    var userCaption by remember { mutableStateOf("") }
    var showGridGuidelines by remember { mutableStateOf(true) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var flashMode by remember { mutableStateOf("AUTO") } // AUTO, ON, OFF
    var showShutterFlash by remember { mutableStateOf(false) }
    var showFocusRing by remember { mutableStateOf(false) }
    var isCapturingPhoto by remember { mutableStateOf(false) }
    var isProcessingPhoto by remember { mutableStateOf(false) }
    var processingMessage by remember { mutableStateOf("Đang áp bộ lọc màu...") }
    var showSuccessStampDialog by remember { mutableStateOf(false) }
    var lastSavedPhotoUri by remember { mutableStateOf<String?>(null) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // Frame selection
    var selectedFrame by remember(frames) {
        mutableStateOf(
            frames.firstOrNull() ?: LuminaFrame(
                id = "frame_90s_polaroid",
                name = "Cổ điển",
                badgeLabel = "📷 Cổ điển",
                subtitle = "TRAVEL DIARY · 2026",
                topTitle = "★ LUMINA PASSPORT ★",
                bottomTagline = "GEN Z FOOTPRINT PASSPORT",
                previewColor = 0xFF1B2A26,
                provinceCode = null,
                isSeasonal = false
            )
        )
    }

    // Photo picker launcher from user's gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            capturedPhotoUri = it.toString()
            wasCapturedWithFrontCamera = false
            studioMode = CheckInStudioMode.CAPTURED_PHOTO
        }
    }

    // Live Date/Time Formatter
    val currentTimeString = remember {
        val sdf = SimpleDateFormat("HH:mm • dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date())
    }

    // Real Camera Shutter Capture
    fun takeRealPhoto() {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        if (isCapturingPhoto || isProcessingPhoto) return
        isCapturingPhoto = true

        coroutineScope.launch {
            showShutterFlash = true
            delay(100)
            showShutterFlash = false
        }

        val cacheDir = File(context.cacheDir, "camera_captures").apply { if (!exists()) mkdirs() }
        val photoFile = File(cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        val mainExecutor = ContextCompat.getMainExecutor(context)
        imageCapture.takePicture(
            outputOptions,
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    isCapturingPhoto = false
                    capturedPhotoUri = photoFile.absolutePath
                    wasCapturedWithFrontCamera = isFrontCamera
                    studioMode = CheckInStudioMode.CAPTURED_PHOTO
                    Log.d("CheckInScreen", "Captured real photo to: ${photoFile.absolutePath}")
                }

                override fun onError(exception: ImageCaptureException) {
                    isCapturingPhoto = false
                    Log.e("CheckInScreen", "Real photo capture failed", exception)
                    Toast.makeText(context, "Chụp ảnh không thành công, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Process Filter & Save to App Private Storage (With OOM Safety & Fallback)
    fun processAndExecuteAction(
        isPublishPost: Boolean,
        onComplete: (savedUri: String) -> Unit
    ) {
        val sourceImage = capturedPhotoUri
        if (sourceImage == null) {
            Toast.makeText(context, "Vui lòng chụp ảnh hoặc chọn ảnh từ thư viện trước", Toast.LENGTH_SHORT).show()
            return
        }

        if (isProcessingPhoto) return
        isProcessingPhoto = true
        processingMessage = "Đang xử lý & áp bộ lọc ${selectedColorMode.title}..."

        coroutineScope.launch {
            try {
                val processResult = ImageColorFilterProcessor.processAndSaveImage(
                    context = context,
                    imageSource = sourceImage,
                    colorMode = selectedColorMode,
                    isMirrored = wasCapturedWithFrontCamera
                )

                isProcessingPhoto = false

                val finalUri = processResult.getOrElse { error ->
                    Log.w("CheckInScreen", "Image filtering fallback due to memory/processing error: ${error.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Bộ nhớ máy quá tải khi áp bộ lọc màu, đã lưu ảnh gốc an toàn",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    sourceImage
                }

                lastSavedPhotoUri = finalUri
                onComplete(finalUri)
            } catch (t: Throwable) {
                isProcessingPhoto = false
                Log.e("CheckInScreen", "Unexpected error in processing", t)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Đã lưu ảnh gốc an toàn", Toast.LENGTH_SHORT).show()
                }
                lastSavedPhotoUri = sourceImage
                onComplete(sourceImage)
            }
        }
    }

    var currentSubTab by remember { mutableStateOf(StampScreenTab.CAPTURE) }
    var isRefreshingGps by remember { mutableStateOf(false) }
    var showAllProvincesList by remember { mutableStateOf(false) }
    var selectedRegionFilter by remember { mutableStateOf("ALL") }

    // GPS and Real Location Services Management
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var realGpsState by remember { mutableStateOf<RealGpsState>(RealGpsState.Idle) }
    var locationJob by remember { mutableStateOf<Job?>(null) }
    var locationPermissionRequested by remember { mutableStateOf(false) }

    fun isGpsServiceEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                locationManager.isLocationEnabled
            } else {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }
        } catch (e: Exception) {
            false
        }
    }

    fun stopLocationTracking() {
        locationJob?.cancel()
        locationJob = null
    }

    fun fetchRealLocation(isManualRefresh: Boolean = false) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            if (locationPermissionRequested) {
                realGpsState = RealGpsState.PermissionDenied
            } else {
                realGpsState = RealGpsState.PermissionRequired
            }
            return
        }

        if (!isGpsServiceEnabled()) {
            realGpsState = RealGpsState.LocationDisabled
            return
        }

        stopLocationTracking()
        realGpsState = RealGpsState.Searching
        if (isManualRefresh) {
            isRefreshingGps = true
        }

        locationJob = coroutineScope.launch {
            try {
                var foundLocation: Location? = null
                val cancellationTokenSource = CancellationTokenSource()

                // 1. Try high-accuracy current location with timeout
                withTimeoutOrNull(10000L) {
                    try {
                        val task = fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            cancellationTokenSource.token
                        )
                        val loc = withContext(Dispatchers.IO) {
                            com.google.android.gms.tasks.Tasks.await(task)
                        }
                        if (loc != null) {
                            foundLocation = loc
                        }
                    } catch (e: Exception) {
                        Log.w("CheckInScreen", "getCurrentLocation error: ${e.message}")
                    }
                }

                // 2. Fallback to recent lastLocation if available within 5 mins
                if (foundLocation == null) {
                    try {
                        val lastLocTask = fusedLocationClient.lastLocation
                        val lastLoc = withContext(Dispatchers.IO) {
                            com.google.android.gms.tasks.Tasks.await(lastLocTask)
                        }
                        if (lastLoc != null && (System.currentTimeMillis() - lastLoc.time) < 300_000L) {
                            foundLocation = lastLoc
                        }
                    } catch (e: Exception) {
                        Log.w("CheckInScreen", "lastLocation error: ${e.message}")
                    }
                }

                val location = foundLocation
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    val accuracy = if (location.hasAccuracy()) location.accuracy else 10f
                    val timestamp = location.time

                    // Find nearest province
                    val nearestProvince = provinces.minByOrNull { prov ->
                        calculateDistanceMeters(lat, lng, prov.centerLat, prov.centerLng)
                    }

                    // Find nearest place
                    val nearestPlace = places.minByOrNull { place ->
                        val pLat = if (place.lat != 0.0) place.lat else (provinces.find { it.code == place.provinceCode }?.centerLat ?: 0.0)
                        val pLng = if (place.lng != 0.0) place.lng else (provinces.find { it.code == place.provinceCode }?.centerLng ?: 0.0)
                        calculateDistanceMeters(lat, lng, pLat, pLng)
                    }

                    val distanceToNearest = if (nearestPlace != null) {
                        val pLat = if (nearestPlace.lat != 0.0) nearestPlace.lat else (provinces.find { it.code == nearestPlace.provinceCode }?.centerLat ?: 0.0)
                        val pLng = if (nearestPlace.lng != 0.0) nearestPlace.lng else (provinces.find { it.code == nearestPlace.provinceCode }?.centerLng ?: 0.0)
                        calculateDistanceMeters(lat, lng, pLat, pLng)
                    } else 0

                    realGpsState = RealGpsState.Success(
                        latitude = lat,
                        longitude = lng,
                        accuracyMeters = accuracy,
                        timestamp = timestamp,
                        detectedProvince = nearestProvince,
                        nearestPlace = nearestPlace,
                        distanceToNearestPlaceMeters = distanceToNearest
                    )

                    // Auto-sync selected province
                    if (nearestProvince != null) {
                        selectedProvince = nearestProvince
                    }
                    if (nearestPlace != null) {
                        selectedPlace = nearestPlace
                    }

                    if (isManualRefresh) {
                        Toast.makeText(context, "Đã cập nhật vị trí GPS thật (±${accuracy.toInt()}m)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    realGpsState = RealGpsState.Error("Không bắt được tín hiệu GPS (có thể do đang ở trong nhà). Vui lòng thử lại ở khu vực thoáng đãng hơn.")
                }
            } catch (e: Exception) {
                Log.e("CheckInScreen", "Error getting location", e)
                realGpsState = RealGpsState.Error("Không thể lấy vị trí: ${e.localizedMessage ?: "Lỗi GPS"}")
            } finally {
                isRefreshingGps = false
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionRequested = true
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            fetchRealLocation()
        } else {
            realGpsState = RealGpsState.PermissionDenied
        }
    }

    // BATTERY EFFICIENCY: Stop GPS tracking immediately when leaving VIEW_STAMPS tab or screen
    DisposableEffect(currentSubTab) {
        if (currentSubTab == StampScreenTab.VIEW_STAMPS) {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasFine || hasCoarse) {
                fetchRealLocation()
            } else {
                realGpsState = RealGpsState.PermissionRequired
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } else {
            stopLocationTracking()
        }
        onDispose {
            stopLocationTracking()
        }
    }

    val visitedProvinces = remember(provinces) { provinces.filter { it.visitedPlacesCount > 0 } }
    val totalStampsCount = remember(provinces) { provinces.sumOf { it.visitedPlacesCount } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // TOP SUB-TAB SWITCHER (Chụp Stamp vs Xem Stamp)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BgSunken,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (currentSubTab == StampScreenTab.CAPTURE) Brand700 else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentSubTab = StampScreenTab.CAPTURE }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = if (currentSubTab == StampScreenTab.CAPTURE) Gold500 else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Chụp Stamp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentSubTab == StampScreenTab.CAPTURE) Color.White else TextPrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (currentSubTab == StampScreenTab.VIEW_STAMPS) Brand700 else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentSubTab = StampScreenTab.VIEW_STAMPS }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CollectionsBookmark,
                                    contentDescription = null,
                                    tint = if (currentSubTab == StampScreenTab.VIEW_STAMPS) Gold500 else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Xem Stamp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentSubTab == StampScreenTab.VIEW_STAMPS) Color.White else TextPrimary
                                )
                                if (visitedProvinces.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (currentSubTab == StampScreenTab.VIEW_STAMPS) Gold500 else Brand100
                                    ) {
                                        Text(
                                            text = "${visitedProvinces.size}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentSubTab == StampScreenTab.VIEW_STAMPS) TextOnGold else Brand700,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 1: CHỤP STAMP (STUDIO CAMERA & QUICK CHECK-IN)
        // =========================================================================
        if (currentSubTab == StampScreenTab.CAPTURE) {
            // 1. Header with Camera Branding & Retake Switch
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CAMERA & STAMP",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Brand700,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (studioMode == CheckInStudioMode.LIVE_CAMERA) HeartRed.copy(alpha = 0.15f) else Brand100
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (studioMode == CheckInStudioMode.LIVE_CAMERA) HeartRed else Brand700)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (studioMode == CheckInStudioMode.LIVE_CAMERA) "CAMERA THẬT 1:1" else "XEM LẠI ẢNH",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (studioMode == CheckInStudioMode.LIVE_CAMERA) HeartRed else Brand700
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Chụp Ảnh & Đóng Dấu Dấu Chân",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }

                        // Retake / Switch back to live camera
                        if (studioMode == CheckInStudioMode.CAPTURED_PHOTO) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Brand100,
                                modifier = Modifier.clickable {
                                    studioMode = CheckInStudioMode.LIVE_CAMERA
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Chụp lại", tint = Brand700, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Chụp lại",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Brand700
                                    )
                                }
                            }
                        }
                    }
                }
            }

        // 2. MAIN SQUARE CAMERA VIEWPORT (1:1 Aspect Ratio)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Outer Bezel Frame with smooth 28.dp rounded corner
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(selectedFrame.previewColor),
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 3.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Gold500.copy(alpha = 0.8f),
                                Color(selectedFrame.previewColor),
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Top Header inside Frame (Location + Time)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.Black.copy(alpha = 0.55f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Gold500,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${selectedPlace.name} · ${selectedProvince.shortName}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.Black.copy(alpha = 0.55f)
                            ) {
                                Text(
                                    text = currentTimeString,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Gold100,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // SQUARE 1:1 MEDIA CONTAINER
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.0f) // 1:1 SQUARE RATIO
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF0F1A17))
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        showFocusRing = true
                                        coroutineScope.launch {
                                            delay(1000)
                                            showFocusRing = false
                                        }
                                    }
                                }
                        ) {
                            if (studioMode == CheckInStudioMode.LIVE_CAMERA) {
                                if (hasCameraPermission) {
                                    // REAL CAMERAX PREVIEW
                                    RealCameraPreview(
                                        isFrontCamera = isFrontCamera,
                                        flashMode = flashMode,
                                        imageCapture = imageCapture,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // CAMERA PERMISSION REQUEST & EXPLANATION OVERLAY
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF14241F))
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Gold500.copy(alpha = 0.2f),
                                            modifier = Modifier.size(54.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.CameraAlt,
                                                    contentDescription = null,
                                                    tint = Gold500,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Mở Camera Thật",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Cần cấp quyền Camera để chụp ảnh check-in chân thực trước mặt bạn.",
                                            fontSize = 11.5.sp,
                                            color = Color.White.copy(alpha = 0.8f),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                                                shape = RoundedCornerShape(999.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text("Bật Camera", fontSize = 11.5.sp, color = TextOnGold, fontWeight = FontWeight.Bold)
                                            }

                                            if (permissionRequested) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val intent = Intent(
                                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                            Uri.fromParts("package", context.packageName, null)
                                                        )
                                                        context.startActivity(intent)
                                                    },
                                                    shape = RoundedCornerShape(999.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                                ) {
                                                    Text("Cài Đặt", fontSize = 11.5.sp, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // REVIEW MODE: Captured/Picked Photo with Live ColorMatrix Filter Preview
                                if (capturedPhotoUri != null) {
                                    AsyncImage(
                                        model = capturedPhotoUri,
                                        contentDescription = "Photo Preview",
                                        contentScale = ContentScale.Crop,
                                        colorFilter = selectedColorMode.getColorFilter(),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .scale(if (wasCapturedWithFrontCamera) -1f else 1f, 1f)
                                    )
                                }
                            }

                            // 3x3 Grid Guidelines Overlay (if active in live mode)
                            if (showGridGuidelines && studioMode == CheckInStudioMode.LIVE_CAMERA) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().border(0.5.dp, Color.White.copy(alpha = 0.25f)))
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().border(0.5.dp, Color.White.copy(alpha = 0.25f)))
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().border(0.5.dp, Color.White.copy(alpha = 0.25f)))
                                }
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.25f)))
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.25f)))
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, Color.White.copy(alpha = 0.25f)))
                                }
                            }

                            // Tap to Focus Ring (Yellow Animated Box)
                            if (showFocusRing) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .align(Alignment.Center)
                                        .border(1.5.dp, Gold500, RoundedCornerShape(8.dp))
                                )
                            }

                            // Live Shutter Flash effect
                            if (showShutterFlash) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                )
                            }

                            // Top In-Screen Badges (Color Mode indicator + Flash/Flip shortcuts)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Active Color Filter Pill
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color.Black.copy(alpha = 0.7f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold500)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = selectedColorMode.iconEmoji, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = selectedColorMode.title,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Quick camera toggles (Active in live mode)
                                if (studioMode == CheckInStudioMode.LIVE_CAMERA) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Grid Toggle
                                        Surface(
                                            shape = CircleShape,
                                            color = if (showGridGuidelines) Gold500 else Color.Black.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable { showGridGuidelines = !showGridGuidelines }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.GridOn,
                                                    contentDescription = "Grid",
                                                    tint = if (showGridGuidelines) TextOnGold else Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        // Flash toggle
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable {
                                                    flashMode = when (flashMode) {
                                                        "AUTO" -> "ON"
                                                        "ON" -> "OFF"
                                                        else -> "AUTO"
                                                    }
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (flashMode == "OFF") Icons.Outlined.FlashOff else Icons.Default.FlashOn,
                                                    contentDescription = "Flash",
                                                    tint = if (flashMode == "ON") Gold500 else Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        // Flip camera (Front/Back)
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable { isFrontCamera = !isFrontCamera }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.FlipCameraAndroid,
                                                    contentDescription = "Flip Camera",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Processing Spinner Overlay
                            if (isProcessingPhoto) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = Gold500, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = processingMessage,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Bottom Stamp Badge (Watermark & Footprint seal)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                        )
                                    )
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StampBadge(
                                        provinceCode = selectedProvince.code,
                                        provinceShortName = selectedProvince.shortName,
                                        dateText = "2026",
                                        symbolEmoji = selectedProvince.stampSymbol,
                                        size = 32.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = selectedFrame.topTitle,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Gold500,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = selectedFrame.bottomTagline,
                                            fontSize = 7.5.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color(0x334CAF50)
                                ) {
                                    Text(
                                        text = "STAMP 1:1",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF81C784),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Frame Subtitle bar
                        Text(
                            text = selectedFrame.subtitle,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Gold100,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 3. COLOR MODE PRESETS STRIP (Áp dụng thật vào ảnh khi lưu)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHẾ ĐỘ MÀU (BỘ LỌC PHOTOBOOTH)",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700
                    )
                    Text(
                        text = "Lưu thật vào ảnh 1:1",
                        fontSize = 9.5.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(CheckInColorMode.values()) { mode ->
                        val isSelected = selectedColorMode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Brand700 else BgSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) Gold500 else Color(0xFFD6DFDB)
                            ),
                            modifier = Modifier
                                .width(78.dp)
                                .clickable { selectedColorMode = mode }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Color swatch sample preview
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(mode.samplePreviewColor))
                                        .border(1.dp, if (isSelected) Gold500 else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = mode.iconEmoji, fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = mode.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = mode.subtext,
                                    fontSize = 7.5.sp,
                                    color = if (isSelected) Gold100 else TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. CAMERA CONTROLS: SHUTTER & GALLERY PICKER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // In Live Camera Mode: Show Gallery Picker + Large Real Shutter Button + Flip button
                if (studioMode == CheckInStudioMode.LIVE_CAMERA) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Gallery Upload Button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BgSurface,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Brand700),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Tải ảnh từ Thư viện",
                                        tint = Brand700,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Thư viện", fontSize = 10.sp, color = TextPrimary)
                        }

                        // 2. BIG REAL SHUTTER BUTTON
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(3.5.dp, Gold500),
                            modifier = Modifier
                                .size(66.dp)
                                .clickable(enabled = !isCapturingPhoto) {
                                    takeRealPhoto()
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(Color.White, Gold500)
                                            )
                                        )
                                )
                            }
                        }

                        // 3. Flip Camera Button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                isFrontCamera = !isFrontCamera
                            }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BgSurface,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD6DFDB)),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Cameraswitch,
                                        contentDescription = "Đổi Camera",
                                        tint = Brand700,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = if (isFrontCamera) "Trước" else "Sau", fontSize = 10.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // 5. FRAME STYLES SELECTOR
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "CHỌN KIỂU VIỀN KHUNG",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Brand700
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(frames) { frame ->
                        val isSelected = selectedFrame.id == frame.id
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSelected) Color(frame.previewColor) else BgSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) Gold500 else Color(0xFFD6DFDB)
                            ),
                            modifier = Modifier.clickable { selectedFrame = frame }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(frame.previewColor))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = frame.badgeLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. CAPTION & CHECK-IN PUBLISH ACTIONS
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = userCaption,
                    onValueChange = { userCaption = it },
                    placeholder = { Text("Viết cảm nghĩ về khoảnh khắc check-in này... #vietnam #checkin", fontSize = 12.sp) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // HELPER / REMINDER BANNER IF PHOTO HAS NOT BEEN TAKEN YET
                if (!hasRealPhoto) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Gold100.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold500.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Brand700,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Vui lòng chụp ảnh từ Camera hoặc chọn ảnh từ Thư viện trước khi đóng dấu hay đăng bài.",
                                fontSize = 12.sp,
                                color = Brand900,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Save to Passport
                    OutlinedButton(
                        onClick = {
                            processAndExecuteAction(isPublishPost = false) { savedUri ->
                                onQuickCheckIn(selectedPlace.name, selectedProvince.code, selectedProvince.shortName, savedUri)
                                showSuccessStampDialog = true
                            }
                        },
                        enabled = hasRealPhoto && !isProcessingPhoto,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        if (isProcessingPhoto) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Brand700, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Đang xử lý...", fontSize = 11.5.sp, color = Brand700)
                        } else {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = if (hasRealPhoto) Brand700 else TextTertiary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Đóng dấu Hộ Chiếu",
                                fontSize = 12.sp,
                                color = if (hasRealPhoto) Brand700 else TextTertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Publish to Community Feed
                    Button(
                        onClick = {
                            processAndExecuteAction(isPublishPost = true) { savedUri ->
                                val caption = if (userCaption.isNotBlank()) {
                                    userCaption
                                } else {
                                    "Check-in tại ${selectedPlace.name} (${selectedColorMode.title}) ✨"
                                }
                                onPublishPost(caption, "${selectedPlace.name}, ${selectedProvince.shortName}", savedUri)
                            }
                        },
                        enabled = hasRealPhoto && !isProcessingPhoto,
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        if (isProcessingPhoto) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Đang đăng...", fontSize = 11.5.sp, color = Color.White)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Đăng feed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 7. NEARBY SUGGESTED PLACES SELECTOR
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "ĐỊA ĐIỂM SĂN DẤU GẦN BẠN (${selectedProvince.shortName.uppercase()})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Brand700
                )

                Spacer(modifier = Modifier.height(8.dp))

                val provincePlaces = places.filter { it.provinceCode == selectedProvince.code }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(provincePlaces) { place ->
                        val isSelected = selectedPlace.id == place.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Brand100 else BgSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) Brand700 else Color(0xFFD6DFDB)
                            ),
                            modifier = Modifier
                                .width(150.dp)
                                .clickable {
                                    selectedPlace = place
                                }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = place.coverUrl,
                                        contentDescription = place.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = place.name,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${place.priceRangeVnd / 1000}k · ${place.category.titleVi}",
                                    fontSize = 9.5.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // =========================================================================
        // TAB 2: XEM STAMP (GPS REAL-TIME, BẢN ĐỒ VIỆT NAM, 34 TỈNH THÀNH & GRID DẤU)
        // =========================================================================

        // 1. STATS & PASSPORT SUMMARY
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "CUỐN HỘ CHIẾU DẤU CHÂN VIỆT NAM",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Brand700,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Hành Trình Chinh Phục 34 Tỉnh Thành",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2 Summary Metric Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Cities Visited Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BgSurface,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Brand700.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "THÀNH PHỐ",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = Brand700,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${visitedProvinces.size}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Brand700
                                )
                                Text(
                                    text = " / ${provinces.size} đã đi",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                                )
                            }
                        }
                    }

                    // Stamps Collected Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BgSurface,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Gold500.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CON DẤU",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = Gold500,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$totalStampsCount",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Gold500
                                )
                                Text(
                                    text = " con dấu đã thu",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                val progress = if (provinces.isNotEmpty()) visitedProvinces.size.toFloat() / provinces.size.toFloat() else 0f
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BgSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tiến độ hoàn thành Hộ Chiếu",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Brand700
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Gold500,
                            trackColor = BgSunken
                        )
                    }
                }
            }
        }

        // 2. GPS REAL-TIME TELEMETRY & ACTIONS
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BgSurface,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Brand700.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val indicatorColor = when (realGpsState) {
                                    is RealGpsState.Success -> Color(0xFF2E7D32)
                                    is RealGpsState.Searching -> Color(0xFFF57C00)
                                    else -> HeartRed
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = indicatorColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (realGpsState) {
                                                is RealGpsState.Success -> Icons.Default.GpsFixed
                                                is RealGpsState.Searching -> Icons.Default.Sensors
                                                else -> Icons.Default.GpsOff
                                            },
                                            contentDescription = null,
                                            tint = indicatorColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "VỊ TRÍ GPS THỰC TẾ",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Brand700
                                    )
                                    Text(
                                        text = when (val state = realGpsState) {
                                            is RealGpsState.Success -> {
                                                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(state.timestamp))
                                                "Cập nhật lúc: $timeStr • Sai số: ±${state.accuracyMeters.toInt()}m"
                                            }
                                            is RealGpsState.Searching -> "Đang nhận tín hiệu chip GPS..."
                                            is RealGpsState.LocationDisabled -> "Dịch vụ vị trí đang tắt"
                                            is RealGpsState.PermissionDenied -> "Chưa cấp quyền vị trí"
                                            is RealGpsState.PermissionRequired -> "Cần cấp quyền truy cập vị trí"
                                            is RealGpsState.Error -> "Mất tín hiệu GPS"
                                            RealGpsState.Idle -> "Chưa quét vị trí"
                                        },
                                        fontSize = 9.5.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Refresh GPS Button
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Brand100,
                                modifier = Modifier.clickable {
                                    fetchRealLocation(isManualRefresh = true)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = "Quét GPS",
                                        tint = Brand700,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isRefreshingGps || realGpsState is RealGpsState.Searching) "Đang dò..." else "Quét lại GPS",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Brand700
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic Content Based on RealGpsState
                        when (val state = realGpsState) {
                            is RealGpsState.Searching -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BgSunken, RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = Brand700,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Đang đọc tọa độ thực từ chip GPS của máy...",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                            is RealGpsState.PermissionRequired, is RealGpsState.PermissionDenied -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFF8E1), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Ứng dụng cần quyền vị trí chính xác để nhận diện địa phương bạn đang du lịch và mở khóa Stamp thật.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF8D6E63),
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                locationPermissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Cấp Quyền Vị Trí", fontSize = 11.sp, color = Color.White)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", context.packageName, null)
                                                )
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Cài Đặt App", fontSize = 11.sp, color = Brand700)
                                        }
                                    }
                                }
                            }
                            is RealGpsState.LocationDisabled -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Dịch vụ định vị (GPS) trên thiết bị đang tắt. Vui lòng bật vị trí để quét tọa độ thực.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFC62828),
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HeartRed),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Bật Vị Trí Trong Cài Đặt", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                            is RealGpsState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = state.message,
                                        fontSize = 11.sp,
                                        color = Color(0xFFE65100),
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { fetchRealLocation(isManualRefresh = true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Gold500)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Thử Quét Lại", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                            is RealGpsState.Success -> {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BgSunken,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "TỌA ĐỘ HIỆN TẠI (WGS84)",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = TextSecondary
                                                )
                                                Text(
                                                    text = "${"%.4f".format(state.latitude)}° N, ${"%.4f".format(state.longitude)}° E",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "KHU VỰC NHẬN DIỆN",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = TextSecondary
                                                )
                                                Text(
                                                    text = "${state.detectedProvince?.shortName ?: selectedProvince.shortName} (${state.detectedProvince?.code ?: selectedProvince.code})",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Brand700
                                                )
                                            }
                                        }

                                        if (state.nearestPlace != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFD6DFDB))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Gần: ${state.nearestPlace.name}",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                val distText = if (state.distanceToNearestPlaceMeters < 1000) {
                                                    "${state.distanceToNearestPlaceMeters}m"
                                                } else {
                                                    "${"%.1f".format(state.distanceToNearestPlaceMeters / 1000.0)}km"
                                                }
                                                Text(
                                                    text = "Khoảng cách: $distText",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Brand700
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Stamp Button at real GPS
                                val targetProvince = state.detectedProvince ?: selectedProvince
                                val targetPlaceName = state.nearestPlace?.name ?: "Vị trí GPS thực tế (${targetProvince.shortName})"
                                Button(
                                    onClick = {
                                        onQuickCheckIn(targetPlaceName, targetProvince.code, targetProvince.shortName, null)
                                        showSuccessStampDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.BookmarkAdded, contentDescription = null, tint = Gold500, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Đóng Dấu Tại ${targetProvince.shortName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            RealGpsState.Idle -> {
                                Button(
                                    onClick = { fetchRealLocation(isManualRefresh = true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = Gold500, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bắt Đầu Quét Vị Trí GPS Thật",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. VIETNAM REAL GOOGLE MAP (Same component as MyTripsScreen)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BẢN ĐỒ DẤU CHÂN VIỆT NAM (GOOGLE MAPS)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Bản đồ vệ tinh/địa hình trực quan & điểm check-in",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Gold500.copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold500)
                    ) {
                        Text(
                            text = "🗺️ Google Map Live",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brand700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Brand700.copy(alpha = 0.35f)),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    InteractiveMapScreen(
                        places = places,
                        provinces = provinces,
                        checkIns = checkIns,
                        onQuickCheckIn = { pName, pCode, provName ->
                            onQuickCheckIn(pName, pCode, provName, null)
                        },
                        onProvinceClick = onProvinceClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // 4. VERTICAL LIST OF 34 PROVINCES (Show 5 + "Xem thêm" expander)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DANH SÁCH 34 TỈNH THÀNH VIỆT NAM",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Chạm tỉnh thành để xem bộ con dấu",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Brand100
                    ) {
                        Text(
                            text = "${visitedProvinces.size}/${provinces.size} Đã đi",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brand700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val displayedProvinces = if (showAllProvincesList) provinces else provinces.take(5)

                displayedProvinces.forEach { prov ->
                    val isVisited = prov.visitedPlacesCount > 0
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BgSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isVisited) Gold500.copy(alpha = 0.6f) else Color(0xFFD6DFDB)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onProvinceClick(prov) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Symbol Badge
                                Surface(
                                    shape = CircleShape,
                                    color = if (isVisited) Gold500.copy(alpha = 0.15f) else BgSunken,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isVisited) Gold500 else Color(0xFFD6DFDB)
                                    ),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = prov.stampSymbol, fontSize = 18.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = prov.shortName,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = BgSunken
                                        ) {
                                            Text(
                                                text = prov.region,
                                                fontSize = 9.sp,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = prov.name,
                                        fontSize = 10.5.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (isVisited) "Đã check-in ${prov.visitedPlacesCount}/${prov.suggestedPlacesCount} điểm" else "Chưa mở khoá (0/${prov.suggestedPlacesCount} điểm)",
                                        fontSize = 10.sp,
                                        color = if (isVisited) Brand700 else TextTertiary,
                                        fontWeight = if (isVisited) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }

                            // Action Button
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (isVisited) Brand700 else BgSunken,
                                modifier = Modifier.clickable { onProvinceClick(prov) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isVisited) "Xem stamp" else "Mở khoá",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isVisited) Color.White else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = if (isVisited) Color.White else TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Expand / Collapse Button
                if (provinces.size > 5) {
                    OutlinedButton(
                        onClick = { showAllProvincesList = !showAllProvincesList },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (showAllProvincesList) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Brand700,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showAllProvincesList) "Thu gọn danh sách" else "Xem thêm toàn bộ ${provinces.size} tỉnh thành (${provinces.size - 5} tỉnh còn lại) ▼",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brand700
                        )
                    }
                }
            }
        }

        // 5. CIRCULAR STAMP GRID (TRANSFERRED FROM PROFILE)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "BỘ SƯU TẬP CON DẤU Ô TRÒN (PASSPORT)",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Brand700,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Cuốn Hộ Chiếu Dấu Chân Tròn",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Chạm vào từng con dấu tròn để mở trang hộ chiếu dấu chân",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Region Filter Chips Row
                val regionFilters = listOf(
                    "ALL" to "Tất cả (${provinces.size})",
                    "EARNED" to "Đã có (${visitedProvinces.size})",
                    "Bắc" to "Miền Bắc",
                    "Trung" to "Miền Trung",
                    "Tây Nguyên" to "Tây Nguyên",
                    "Nam" to "Miền Nam"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(regionFilters) { (filterKey, filterLabel) ->
                        val isSelected = selectedRegionFilter == filterKey
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSelected) Brand700 else BgSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Gold500 else Color(0xFFD6DFDB)
                            ),
                            modifier = Modifier.clickable { selectedRegionFilter = filterKey }
                        ) {
                            Text(
                                text = filterLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filtered Provinces List for Grid
                val filteredForGrid = when (selectedRegionFilter) {
                    "EARNED" -> provinces.filter { it.visitedPlacesCount > 0 }
                    "Bắc" -> provinces.filter { it.region.contains("Bắc", ignoreCase = true) }
                    "Trung" -> provinces.filter { it.region.contains("Trung", ignoreCase = true) }
                    "Tây Nguyên" -> provinces.filter { it.region.contains("Tây Nguyên", ignoreCase = true) }
                    "Nam" -> provinces.filter { it.region.contains("Nam", ignoreCase = true) }
                    else -> provinces
                }

                // Render in 3-column chunked rows
                val chunkedProvinces = filteredForGrid.chunked(3)

                chunkedProvinces.forEach { rowProvinces ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowProvinces.forEach { prov ->
                            val isEarned = prov.visitedPlacesCount > 0
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onProvinceClick(prov) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(76.dp)
                                ) {
                                    StampBadge(
                                        provinceCode = prov.code,
                                        provinceShortName = prov.shortName,
                                        dateText = if (isEarned) "2026" else "---",
                                        symbolEmoji = prov.stampSymbol,
                                        size = 72.dp,
                                        isEarned = isEarned,
                                        animateDrop = false
                                    )

                                    // Lock indicator for unvisited provinces
                                    if (!isEarned) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.55f),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Chưa mở khoá",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = prov.shortName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEarned) TextPrimary else TextSecondary,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = if (isEarned) "${prov.visitedPlacesCount} điểm" else "Khóa",
                                    fontSize = 9.sp,
                                    color = if (isEarned) Brand700 else TextTertiary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Fill remaining slots in row if less than 3
                        val remaining = 3 - rowProvinces.size
                        repeat(remaining) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

    // Success Stamp Drop Dialog
    if (showSuccessStampDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessStampDialog = false },
            confirmButton = {
                Button(
                    onClick = { showSuccessStampDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Tuyệt vời!")
                }
            },
            title = {
                Text(
                    text = "🎉 ĐÃ ĐÓNG DẤU HỘ CHIẾU",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Brand700,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StampBadge(
                        provinceCode = selectedProvince.code,
                        provinceShortName = selectedProvince.shortName,
                        dateText = "2026",
                        symbolEmoji = selectedProvince.stampSymbol,
                        size = 80.dp,
                        isEarned = true,
                        animateDrop = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Đã lưu dấu chân tại ${selectedPlace.name} với khung ${selectedFrame.name} và bộ lọc ${selectedColorMode.title} vào Hộ Chiếu Dấu Chân!",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = TextPrimary
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = BgSurface
        )
    }
}
