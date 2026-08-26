package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CheckInRecord
import com.example.data.model.LuminaFrame
import com.example.data.model.Place
import com.example.data.model.Province
import com.example.ui.components.PhotoFilters
import com.example.ui.components.StampBadge
import com.example.ui.theme.*

enum class CheckInTab {
    QUICK_STAMP,
    FRAME_STUDIO
}

@Composable
fun CheckInScreen(
    provinces: List<Province>,
    places: List<Place>,
    checkIns: List<CheckInRecord>,
    frames: List<LuminaFrame> = emptyList(),
    onQuickCheckIn: (placeName: String, provinceCode: String, provinceName: String) -> Unit,
    onPublishPost: (caption: String, location: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(CheckInTab.QUICK_STAMP) }
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
                address = "Đà Lạt",
                description = "Điểm săn mây",
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
    var selectedFrame by remember(frames) {
        mutableStateOf(
            frames.firstOrNull() ?: LuminaFrame(
                id = "frame_1",
                name = "Quốc Khánh 2/9",
                badgeLabel = "🇻🇳 Quốc Khánh 2/9",
                subtitle = "2/9/1945 - 2/9/2026",
                topTitle = "★ VIỆT NAM TRONG TIM TÔI ★",
                bottomTagline = "LUMINA PASSPORT",
                previewColor = 0xFFD93025,
                provinceCode = null,
                isSeasonal = true
            )
        )
    }
    var userCaption by remember { mutableStateOf("") }
    var showStampSuccessDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "PASSPORT CHECK-IN & KHUNG ẢNH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Brand700,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Đóng dấu Hộ Chiếu Dấu Chân",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSunken, RoundedCornerShape(999.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (currentTab == CheckInTab.QUICK_STAMP) Brand700 else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { currentTab = CheckInTab.QUICK_STAMP }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "⦿ Đóng dấu tức thì",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == CheckInTab.QUICK_STAMP) Color.White else TextSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (currentTab == CheckInTab.FRAME_STUDIO) Brand700 else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { currentTab = CheckInTab.FRAME_STUDIO }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "📸 Khung ảnh & Bộ lọc màu",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == CheckInTab.FRAME_STUDIO) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (currentTab == CheckInTab.QUICK_STAMP) {
            // TAB 1: QUICK STAMP SECTION
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // GPS Location Detection Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Brand900),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(20.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Brand900, Color(0xFF03221B))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Gold500, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "GPS CHECK-IN ENGINE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Gold500
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color(0x334CAF50)
                                    ) {
                                        Text(
                                            text = "● Độ tin cậy: 100%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF81C784),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = selectedPlace.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${selectedPlace.address} · Bán kính 200m",
                                    fontSize = 12.sp,
                                    color = Color(0xFFBEDBD0)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Stamp Badge Showcase
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    StampBadge(
                                        provinceCode = selectedProvince.code,
                                        provinceShortName = selectedProvince.shortName,
                                        dateText = "15/08/2026",
                                        symbolEmoji = selectedProvince.stampSymbol,
                                        size = 96.dp,
                                        isEarned = true,
                                        animateDrop = false
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Big Stamp Button
                                Button(
                                    onClick = {
                                        onQuickCheckIn(selectedPlace.name, selectedProvince.code, selectedProvince.shortName)
                                        showStampSuccessDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                                    shape = RoundedCornerShape(999.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text(
                                        text = "ĐÓNG DẤU VÀO HỘ CHIẾU NGAY ⦿",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextOnGold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Places near you carousel
                    Text(
                        text = "ĐỊA ĐIỂM GỢI Ý TẠI ${selectedProvince.shortName.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val provincePlaces = places.filter { it.provinceCode == selectedProvince.code }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(provincePlaces) { place ->
                            val isSelected = selectedPlace.id == place.id
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) Brand100 else BgSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) Brand700 else Color(0xFFD6DFDB)
                                ),
                                modifier = Modifier
                                    .width(160.dp)
                                    .clickable { selectedPlace = place }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(90.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        AsyncImage(
                                            model = place.coverUrl,
                                            contentDescription = place.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = Color.Black.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = "${place.category.iconEmoji} ${place.category.titleVi}",
                                                fontSize = 8.5.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = place.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${place.priceRangeVnd / 1000}k • ~${place.checkinRadiusMeters}m",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Recent Stamps Row
                    Text(
                        text = "LỊCH SỬ DẤU CHÂN GẦN ĐÂY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(checkIns) { chk ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = BgSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
                                modifier = Modifier.width(130.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    StampBadge(
                                        provinceCode = chk.provinceCode,
                                        provinceShortName = chk.provinceName,
                                        dateText = chk.formattedDate,
                                        symbolEmoji = "★",
                                        size = 54.dp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = chk.placeName,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = chk.formattedDate,
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 2: FRAME STUDIO SECTION (Both Frame and Photo Color Tone change together!)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Frame Selection Pills
                    Text(
                        text = "CHỌN KHUNG ẢNH & BỘ LỌC MÀU NGHỆ THUẬT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(frames) { frame ->
                            val isSelected = selectedFrame.id == frame.id
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSelected) Brand700 else BgSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Gold500 else Color(0xFFD6DFDB)
                                ),
                                modifier = Modifier.clickable { selectedFrame = frame }
                            ) {
                                Text(
                                    text = frame.badgeLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Color Tone Label Badge
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Brand100,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8DEC9))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "🎨 Đang áp dụng: ", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = PhotoFilters.getFilterName(selectedFrame.id),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Brand900
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Frame Live Visual Card Preview (Transforms both frame and inner photo color matrix!)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Framed Photo Simulation Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.85f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(selectedFrame.previewColor))
                                    .padding(10.dp)
                            ) {
                                // Inner Image with dynamic color filter matching the frame
                                AsyncImage(
                                    model = selectedPlace.coverUrl,
                                    contentDescription = selectedFrame.name,
                                    contentScale = ContentScale.Crop,
                                    colorFilter = PhotoFilters.getFilterForFrame(selectedFrame.id),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp))
                                )

                                // Frame Top Title Overlay
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.65f),
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 10.dp)
                                ) {
                                    Text(
                                        text = selectedFrame.topTitle,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Gold500,
                                        letterSpacing = 0.8.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }

                                // Frame Bottom Stamp Seal & Tagline
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                            )
                                        )
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = selectedFrame.subtitle,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = selectedFrame.bottomTagline,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Gold100
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Caption textfield
                            OutlinedTextField(
                                value = userCaption,
                                onValueChange = { userCaption = it },
                                placeholder = { Text("Thêm cảm nghĩ về khoảnh khắc này...", fontSize = 12.sp) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        // Save to Vault
                                        onQuickCheckIn(selectedPlace.name, selectedProvince.code, selectedProvince.shortName)
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Lưu City Vault", fontSize = 11.5.sp)
                                }

                                Button(
                                    onClick = {
                                        val caption = if (userCaption.isNotBlank()) userCaption else "Check-in ${selectedPlace.name} với khung ${selectedFrame.name} ✨"
                                        onPublishPost(caption, "${selectedPlace.name}, ${selectedProvince.shortName}")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                    shape = RoundedCornerShape(999.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Đăng Feed ➔", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
