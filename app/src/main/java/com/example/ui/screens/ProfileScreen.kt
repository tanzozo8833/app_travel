package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.AchievementBadge
import com.example.data.model.Province
import com.example.data.model.SavedBoard
import com.example.data.model.UserProfile
import com.example.ui.components.StampBadge
import com.example.ui.components.VietnamMapGraphic
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    provinces: List<Province>,
    badges: List<AchievementBadge>,
    savedBoards: List<SavedBoard>,
    onOpenTravelCv: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onProvinceClick: (Province) -> Unit,
    modifier: Modifier = Modifier
) {
    val visitedProvinces = remember(provinces) { provinces.filter { it.visitedPlacesCount > 0 } }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgCanvas),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Profile Hero Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HỒ SƠ LỮ HÀNH",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onOpenTravelCv,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BgSunken, CircleShape)
                            ) {
                                Icon(Icons.Default.CardMembership, contentDescription = "Travel CV", tint = Brand700, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BgSunken, CircleShape)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Cài đặt & Tài khoản", tint = Brand700, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Avatar + Bio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = userProfile.avatarUrl,
                            contentDescription = userProfile.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, Gold500, CircleShape)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.displayName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                            }

                            Text(
                                text = "@${userProfile.username}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Gold500
                            ) {
                                Text(
                                    text = userProfile.tagTitle,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextOnGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = userProfile.bio,
                        fontSize = 12.5.sp,
                        color = TextPrimary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action: Travel CV Generator Pill Button
                    Button(
                        onClick = onOpenTravelCv,
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(999.dp))
                    ) {
                        Icon(Icons.Default.CardMembership, contentDescription = null, tint = Gold500, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Tạo Thẻ Dấu Chân Du Lịch (Travel CV) ➔", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Row
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BgSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val totalProvincesCount = if (provinces.isNotEmpty()) provinces.size else userProfile.totalProvincesCount
                        val totalVisitedProvinces = provinces.count { it.visitedPlacesCount > 0 }.coerceAtLeast(userProfile.visitedProvincesCount)
                        val totalStamps = provinces.sumOf { it.visitedPlacesCount }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileStatItem("Tỉnh thành", "$totalVisitedProvinces/$totalProvincesCount")
                            ProfileStatItem("Con dấu", "$totalStamps")
                            ProfileStatItem("Chuyến đi", "${userProfile.tripsCompletedCount}")
                            ProfileStatItem("Lượt lưu", "${userProfile.totalSavesCount}")
                        }
                    }
                }
            }

            // Vietnam Map Graphic (Interactive with Sovereignty Badge)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    VietnamMapGraphic(
                        provinces = provinces,
                        onProvinceClick = onProvinceClick
                    )
                }
            }

            // Achievement Badges
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "HUY HIỆU THÀNH TỰU",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(badges) { badge ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (badge.isEarned) Gold100 else BgSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (badge.isEarned) Gold500 else Color(0xFFD6DFDB)
                                ),
                                modifier = Modifier.width(130.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Text(text = badge.iconEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = badge.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (badge.isEarned) TextOnGold else TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (badge.isEarned) "Đã mở khoá" else "${badge.progressCurrent}/${badge.progressMax}",
                                        fontSize = 9.5.sp,
                                        color = if (badge.isEarned) Brand700 else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Saved Boards
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "BỘ SƯU TẬP ĐÃ LƯU",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(savedBoards) { board ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = BgSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
                                modifier = Modifier.width(140.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(85.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = board.coverUrl,
                                            contentDescription = board.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = board.name,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${board.postCount} bài viết",
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
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Brand900)
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
    }
}
