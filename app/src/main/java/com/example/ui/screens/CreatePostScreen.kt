package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Trip
import com.example.ui.theme.*

enum class ShareAudience(val label: String, val description: String) {
    PUBLIC("Thế giới", "Mọi người trên Lumina đều có thể xem và lưu"),
    FRIENDS("Bạn bè", "Chỉ những người bạn theo dõi lẫn nhau có thể xem"),
    ONLY_ME("Chỉ mình tôi", "Lưu vào nhật ký du lịch riêng tư")
}

@Composable
fun CreatePostScreen(
    trips: List<Trip>,
    onClose: () -> Unit,
    onPublish: (caption: String, location: String, attachedTrip: Trip?, mediaUrls: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Đà Lạt, Lâm Đồng") }
    var selectedAudience by remember { mutableStateOf(ShareAudience.PUBLIC) }

    // Uploaded photos list (Supports up to 20 media)
    var selectedImages by remember {
        mutableStateOf(
            listOf(
                "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800&auto=format&fit=crop"
            )
        )
    }

    // Photo picker launcher up to 20 media
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val uriStrings = uris.map { it.toString() }
            val combined = (selectedImages + uriStrings).distinct().take(20)
            selectedImages = combined
        }
    }

    // Fallback getContent launcher
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (selectedImages.size < 20) {
                selectedImages = (selectedImages + it.toString()).take(20)
            }
        }
    }

    // Trip attachment modes (3 vertical sections)
    var tripAttachmentMode by remember { mutableStateOf(0) } // 0: None, 1: Select existing trip, 2: Create new attached
    var selectedTrip by remember { mutableStateOf<Trip?>(trips.firstOrNull()) }

    fun moveImage(fromIndex: Int, toIndex: Int) {
        if (fromIndex in selectedImages.indices && toIndex in selectedImages.indices) {
            val mutable = selectedImages.toMutableList()
            val item = mutable.removeAt(fromIndex)
            mutable.add(toIndex, item)
            selectedImages = mutable
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = BgCanvas,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Huỷ", tint = TextPrimary)
                    }
                    Text(
                        text = "Tạo Bài Viết Mới",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Button(
                        onClick = {
                            if (caption.isNotBlank()) {
                                val tripToAttach = if (tripAttachmentMode == 1) selectedTrip else null
                                onPublish(caption, location, tripToAttach, selectedImages)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                        shape = RoundedCornerShape(999.dp),
                        enabled = caption.isNotBlank()
                    ) {
                        Text(text = "Đăng", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BgCanvas)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
        ) {
            // 1. Share With (Audience Selector: Thế giới, Bạn bè, Chỉ mình tôi)
            item {
                Column {
                    Text(
                        text = "CHIA SẺ VỚI (QUYỀN RIÊNG TƯ)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShareAudience.values().forEach { audience ->
                            val isSelected = selectedAudience == audience
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Brand700 else BgSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Brand700 else Color(0xFFD6DFDB)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedAudience = audience }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = when (audience) {
                                            ShareAudience.PUBLIC -> Icons.Default.Public
                                            ShareAudience.FRIENDS -> Icons.Default.People
                                            ShareAudience.ONLY_ME -> Icons.Default.Lock
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Gold500 else Brand700,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = audience.label,
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

            // 2. Media Upload & Reorder (Limit up to 20 media + Cover Badge)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ẢNH BÀI VIẾT (${selectedImages.size}/20)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700
                        )
                        Text(
                            text = "Ảnh đầu tiên là ảnh bìa trên feed",
                            fontSize = 10.5.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Upload button (up to 20)
                        if (selectedImages.size < 20) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Brand100,
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Brand700),
                                    modifier = Modifier
                                        .size(width = 120.dp, height = 150.dp)
                                        .clickable {
                                            try {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            } catch (_: Exception) {
                                                getContentLauncher.launch("image/*")
                                            }
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = "Chọn ảnh từ máy",
                                            tint = Brand700,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Thêm ảnh",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Brand700
                                        )
                                        Text(
                                            text = "Tối đa 20 ảnh",
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Display picked photos with reorder & cover setting
                        itemsIndexed(selectedImages) { index, imgUrl ->
                            val isCover = index == 0
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = BgSurface),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier
                                    .size(width = 130.dp, height = 150.dp)
                                    .border(
                                        width = if (isCover) 2.dp else 1.dp,
                                        color = if (isCover) Gold500 else Color(0xFFD6DFDB),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = imgUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Cover Badge (determines image shown on feed)
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (isCover) Gold500 else Color.Black.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(5.dp)
                                            .clickable {
                                                if (!isCover) {
                                                    moveImage(index, 0)
                                                }
                                            }
                                    ) {
                                        Text(
                                            text = if (isCover) "Ảnh bìa Feed" else "Đặt làm bìa",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCover) TextOnGold else Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Remove button
                                    if (selectedImages.size > 1) {
                                        IconButton(
                                            onClick = {
                                                selectedImages = selectedImages.filterIndexed { i, _ -> i != index }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(22.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Xoá ảnh",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }

                                    // Reorder controls (< and >) at bottom
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.55f))
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (index > 0) {
                                            IconButton(
                                                onClick = { moveImage(index, index - 1) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ChevronLeft,
                                                    contentDescription = "Di chuyển sang trái",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (index < selectedImages.size - 1) {
                                            IconButton(
                                                onClick = { moveImage(index, index + 1) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ChevronRight,
                                                    contentDescription = "Di chuyển sang phải",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Caption Field
            item {
                Column {
                    Text(
                        text = "NỘI DUNG CHIA SẺ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        placeholder = { Text("Chia sẻ cảm nhận, kinh nghiệm săn mây, ăn uống, góc chụp ảnh đẹp...", fontSize = 13.sp) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            }

            // 4. Location Selector with Autocomplete Dropdown
            item {
                val sampleLocations = listOf(
                    "Đà Lạt, Lâm Đồng",
                    "Hồ Xuân Hương, Đà Lạt",
                    "Đồi Chè Cầu Đất, Lâm Đồng",
                    "Phố cổ Hội An, Quảng Nam",
                    "Hà Giang (Đèo Mã Pí Lèng)",
                    "Vịnh Hạ Long, Quảng Ninh",
                    "Sa Pa, Lào Cai",
                    "Phú Quốc, Kiên Giang",
                    "Côn Đảo, Bà Rịa - Vũng Tàu",
                    "Cố đô Huế, Thừa Thiên Huế",
                    "Tràng An, Ninh Bình",
                    "Bà Nà Hills, Đà Nẵng",
                    "Mũi Né, Bình Thuận",
                    "Nha Trang, Khánh Hòa",
                    "Phong Nha - Kẻ Bàng, Quảng Bình",
                    "Cần Thơ (Chợ nổi Cái Răng)",
                    "Mộc Châu, Sơn La",
                    "Hồ Gươm, Hà Nội",
                    "Nhà thờ Đức Bà, TP. Hồ Chí Minh"
                )
                var isLocationDropdownOpen by remember { mutableStateOf(false) }
                val filteredLocations = remember(location) {
                    if (location.isBlank()) sampleLocations.take(6)
                    else sampleLocations.filter { it.contains(location, ignoreCase = true) }.take(6)
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ĐỊA ĐIỂM CHECK-IN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = {
                                location = it
                                isLocationDropdownOpen = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Brand700)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isLocationDropdownOpen = !isLocationDropdownOpen }) {
                                    Icon(
                                        imageVector = if (isLocationDropdownOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Gợi ý địa điểm",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            placeholder = { Text("Nhập địa điểm check-in...", fontSize = 13.sp) },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brand700,
                                unfocusedBorderColor = Color(0xFFD6DFDB)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = isLocationDropdownOpen && filteredLocations.isNotEmpty(),
                            onDismissRequest = { isLocationDropdownOpen = false },
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .background(BgSurface)
                        ) {
                            Text(
                                text = "GỢI Ý ĐỊA ĐIỂM",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                            filteredLocations.forEach { locSuggestion ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = Brand700,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = locSuggestion,
                                                fontSize = 13.sp,
                                                fontWeight = if (locSuggestion == location) FontWeight.Bold else FontWeight.Normal,
                                                color = if (locSuggestion == location) Brand700 else TextPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        location = locSuggestion
                                        isLocationDropdownOpen = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Attach Trip: 3 Vertical Items Layout (UI Dọc 3 Mục)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ĐÍNH KÈM LỊCH TRÌNH (3 MỤC LỰA CHỌN)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Mục 1: Không đính kèm lịch trình
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (tripAttachmentMode == 0) Brand100 else BgSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (tripAttachmentMode == 0) Brand700 else Color(0xFFD6DFDB)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tripAttachmentMode = 0 }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = tripAttachmentMode == 0,
                                    onClick = { tripAttachmentMode = 0 },
                                    colors = RadioButtonDefaults.colors(selectedColor = Brand700)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "1. Không đính kèm lịch trình",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Đăng bài viết dạng chia sẻ ảnh và bài viết tự do",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Mục 2: Chọn từ chuyến đi của tôi
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (tripAttachmentMode == 1) Brand100 else BgSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (tripAttachmentMode == 1) Brand700 else Color(0xFFD6DFDB)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tripAttachmentMode = 1 }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = tripAttachmentMode == 1,
                                        onClick = { tripAttachmentMode = 1 },
                                        colors = RadioButtonDefaults.colors(selectedColor = Brand700)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "2. Chọn từ lịch trình chuyến đi của bạn",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Người xem có thể bấm vào để xem toàn bộ chi tiết hành trình",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                // List of user trips (when selected)
                                if (tripAttachmentMode == 1 && trips.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        trips.forEach { trip ->
                                            val isThisTrip = selectedTrip?.id == trip.id
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isThisTrip) Brand700 else BgCanvas,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (isThisTrip) Gold500 else Color(0xFFD6DFDB)
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedTrip = trip }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    AsyncImage(
                                                        model = trip.coverUrl,
                                                        contentDescription = trip.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = trip.title,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isThisTrip) Color.White else TextPrimary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "${trip.destination} • ${trip.durationText}",
                                                            fontSize = 10.sp,
                                                            color = if (isThisTrip) Gold500 else TextSecondary
                                                        )
                                                    }
                                                    if (isThisTrip) {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = Gold500,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Mục 3: Tạo lịch trình mới nhanh
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (tripAttachmentMode == 2) Brand100 else BgSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (tripAttachmentMode == 2) Brand700 else Color(0xFFD6DFDB)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tripAttachmentMode = 2
                                    selectedTrip = trips.firstOrNull()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = tripAttachmentMode == 2,
                                    onClick = {
                                        tripAttachmentMode = 2
                                        selectedTrip = trips.firstOrNull()
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Brand700)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "3. Tạo lịch trình mới đi kèm bài viết",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Tự động thiết lập khung lịch trình theo địa điểm check-in",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

