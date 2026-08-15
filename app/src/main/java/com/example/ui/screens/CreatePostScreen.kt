package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Trip
import com.example.ui.theme.*

@Composable
fun CreatePostScreen(
    trips: List<Trip>,
    onClose: () -> Unit,
    onPublish: (caption: String, location: String, attachedTrip: Trip?, mediaUrls: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Đà Lạt, Lâm Đồng") }
    var selectedTrip by remember { mutableStateOf<Trip?>(trips.firstOrNull()) }

    // Uploaded photos list from device
    var selectedImages by remember {
        mutableStateOf(
            listOf("https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop")
        )
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val uriStrings = uris.map { it.toString() }
            selectedImages = uriStrings
        }
    }

    // Fallback getContent launcher
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImages = listOf(it.toString())
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
                                onPublish(caption, location, selectedTrip, selectedImages)
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BgCanvas)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Photo Preview & selector with device upload
            Column {
                Text(
                    text = "ẢNH BÀI VIẾT (TẢI TỪ THIẾT BỊ HOẶC CHỤP)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brand700
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upload button
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Brand100,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Brand700),
                            modifier = Modifier
                                .size(width = 110.dp, height = 140.dp)
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
                                    text = "Tải ảnh từ máy",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Brand700
                                )
                            }
                        }
                    }

                    // Display picked photos
                    itemsIndexed(selectedImages) { index, imgUrl ->
                        Box(
                            modifier = Modifier
                                .size(width = 110.dp, height = 140.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(BgSunken)
                        ) {
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Remove button
                            if (selectedImages.size > 1) {
                                IconButton(
                                    onClick = {
                                        selectedImages = selectedImages.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xoá ảnh",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Caption Field
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                placeholder = { Text("Chia sẻ khoảnh khắc, mẹo du lịch, gợi ý góc chụp đẹp...", fontSize = 13.sp) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Location Selector
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Brand700) },
                label = { Text("Địa điểm check-in") },
                shape = RoundedCornerShape(999.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Attach Trip Selector
            Text(
                text = "ĐÍNH KÈM LỊCH TRÌNH CỦA BẠN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Brand700
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (selectedTrip == null) Brand700 else BgSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
                        modifier = Modifier.clickable { selectedTrip = null }
                    ) {
                        Text(
                            text = "Không đính kèm",
                            fontSize = 12.sp,
                            color = if (selectedTrip == null) Color.White else TextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                items(trips) { trip ->
                    val isSelected = selectedTrip?.id == trip.id
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isSelected) Brand700 else BgSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Gold500 else Color(0xFFD6DFDB)
                        ),
                        modifier = Modifier.clickable { selectedTrip = trip }
                    ) {
                        Text(
                            text = "📅 ${trip.title}",
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else TextPrimary,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
