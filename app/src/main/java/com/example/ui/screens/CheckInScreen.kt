package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class CheckInStudioMode {
    LIVE_CAMERA,
    CAPTURED_PHOTO
}

@Composable
fun CheckInScreen(
    provinces: List<Province>,
    places: List<Place>,
    checkIns: List<CheckInRecord>,
    frames: List<LuminaFrame> = emptyList(),
    onQuickCheckIn: (placeName: String, provinceCode: String, provinceName: String, photoUrl: String?) -> Unit,
    onPublishPost: (caption: String, location: String, photoUrl: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedProvince by remember(provinces) {
        mutableStateOf(
            provinces.find { it.code == "LDG" } ?: provinces.firstOrNull() ?: Province(
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

    var selectedPlace by remember(places) {
        mutableStateOf(
            places.firstOrNull { it.provinceCode == "LDG" } ?: places.firstOrNull() ?: Place(
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
    var activePhotoUrl by remember { mutableStateOf(selectedPlace.coverUrl) }
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
                name = "Locket Classic",
                badgeLabel = "📷 Locket Classic",
                subtitle = "LOCKET TRAVEL DIARY · 2026",
                topTitle = "★ LUMINA LOCKET ★",
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
            activePhotoUrl = it.toString()
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
                    activePhotoUrl = photoFile.absolutePath
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

    // Process Filter & Save to App Private Storage
    fun processAndExecuteAction(
        isPublishPost: Boolean,
        onComplete: (savedUri: String) -> Unit
    ) {
        if (isProcessingPhoto) return
        isProcessingPhoto = true
        processingMessage = "Đang áp bộ lọc ${selectedColorMode.title} & lưu ảnh..."

        coroutineScope.launch {
            val sourceImage = if (studioMode == CheckInStudioMode.LIVE_CAMERA) {
                // If user clicks publish directly from live mode, use place cover or prompt
                selectedPlace.coverUrl
            } else {
                activePhotoUrl
            }

            val processResult = ImageColorFilterProcessor.processAndSaveImage(
                context = context,
                imageSource = sourceImage,
                colorMode = selectedColorMode,
                isMirrored = wasCapturedWithFrontCamera
            )

            isProcessingPhoto = false

            val finalUri = processResult.getOrElse { error ->
                Log.w("CheckInScreen", "Image filtering fallback due to error: ${error.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Không thể áp bộ lọc màu, lưu ảnh gốc", Toast.LENGTH_SHORT).show()
                }
                sourceImage
            }

            lastSavedPhotoUri = finalUri
            onComplete(finalUri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Header with Locket Style Branding & Retake Switch
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LOCKET CAMERA & STAMP",
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

        // 2. MAIN SQUARE LOCKET CAMERA VIEWPORT (1:1 Aspect Ratio)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Outer Locket Bezel Frame with smooth 28.dp rounded corner
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
                        // Top Header inside Locket Frame (Location + Time)
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
                                .aspectRatio(1.0f) // 1:1 SQUARE LOCKET RATIO
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
                                            text = "Cần cấp quyền Camera để chụp ảnh check-in Locket chân thực trước mặt bạn.",
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
                                AsyncImage(
                                    model = activePhotoUrl,
                                    contentDescription = "Locket Photo Preview",
                                    contentScale = ContentScale.Crop,
                                    colorFilter = selectedColorMode.getColorFilter(),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(if (wasCapturedWithFrontCamera) -1f else 1f, 1f)
                                )
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

                            // Bottom Locket Stamp Badge (Watermark & Footprint seal)
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
                                        text = "LOCKET 1:1",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF81C784),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

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
                    .padding(top = 16.dp, bottom = 6.dp)
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
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700
                    )
                    Text(
                        text = "Lưu thật vào ảnh 1:1",
                        fontSize = 10.5.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CheckInColorMode.values()) { mode ->
                        val isSelected = selectedColorMode == mode
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Brand700 else BgSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) Gold500 else Color(0xFFD6DFDB)
                            ),
                            modifier = Modifier
                                .width(106.dp)
                                .clickable { selectedColorMode = mode }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Color swatch sample preview
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(mode.samplePreviewColor))
                                        .border(1.dp, if (isSelected) Gold500 else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = mode.iconEmoji, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = mode.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = mode.subtext,
                                    fontSize = 8.5.sp,
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
                    .padding(horizontal = 16.dp, vertical = 10.dp)
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
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Tải ảnh từ Thư viện",
                                        tint = Brand700,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Thư viện", fontSize = 10.5.sp, color = TextPrimary)
                        }

                        // 2. BIG REAL SHUTTER BUTTON
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(4.dp, Gold500),
                            modifier = Modifier
                                .size(74.dp)
                                .clickable(enabled = !isCapturingPhoto) {
                                    takeRealPhoto()
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(6.dp)
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
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Cameraswitch,
                                        contentDescription = "Đổi Camera",
                                        tint = Brand700,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = if (isFrontCamera) "Trước" else "Sau", fontSize = 10.5.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // 5. LOCKET FRAME STYLES SELECTOR
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "CHỌN KIỂU VIỀN KHUNG LOCKET",
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
                    placeholder = { Text("Viết cảm nghĩ về khoảnh khắc check-in này... #locket #checkin", fontSize = 12.sp) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                        enabled = !isProcessingPhoto,
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
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = Brand700, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Đóng dấu Hộ Chiếu", fontSize = 12.sp, color = Brand700, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Publish to Community Feed
                    Button(
                        onClick = {
                            processAndExecuteAction(isPublishPost = true) { savedUri ->
                                val caption = if (userCaption.isNotBlank()) {
                                    userCaption
                                } else {
                                    "Check-in Locket tại ${selectedPlace.name} (${selectedColorMode.title}) ✨"
                                }
                                onPublishPost(caption, "${selectedPlace.name}, ${selectedProvince.shortName}", savedUri)
                            }
                        },
                        enabled = !isProcessingPhoto,
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
                            Text(text = "Đăng Locket Feed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                    activePhotoUrl = place.coverUrl
                                    wasCapturedWithFrontCamera = false
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
                        text = "Đã lưu dấu chân tại ${selectedPlace.name} với khung Locket và bộ lọc ${selectedColorMode.title} vào Hộ Chiếu Dấu Chân!",
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
