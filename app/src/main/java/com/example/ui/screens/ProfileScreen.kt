package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CheckInRecord
import com.example.data.model.Place
import com.example.data.model.Post
import com.example.data.model.Province
import com.example.data.model.Trip
import com.example.data.model.TripStatus
import com.example.data.model.UserProfile
import com.example.ui.components.VietnamMapGraphic
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

enum class ProfileSubTab {
    TRIPS,
    SAVED
}

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    provinces: List<Province>,
    trips: List<Trip>,
    posts: List<Post>,
    checkIns: List<CheckInRecord> = emptyList(),
    places: List<Place> = emptyList(),
    onOpenTravelCv: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onProvinceClick: (Province) -> Unit,
    onTripClick: (Trip) -> Unit,
    onPostClick: (Post) -> Unit,
    onCreateTrip: () -> Unit = {},
    onExploreFeed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ProfileSubTab.TRIPS) }

    // Compute REAL Live Data from Room Database States
    val totalProvincesCount = if (provinces.isNotEmpty()) provinces.size else 34
    val visitedProvincesCount = remember(provinces, checkIns) {
        provinces.count { it.visitedPlacesCount > 0 }
            .coerceAtLeast(checkIns.map { it.provinceCode }.distinct().size)
    }
    val totalStamps = remember(checkIns, provinces) {
        if (checkIns.isNotEmpty()) checkIns.size else provinces.sumOf { it.visitedPlacesCount }
    }
    val savedPosts = remember(posts) {
        posts.filter { it.isSaved }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgCanvas),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Profile Hero Header
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
                                Icon(
                                    Icons.Default.CardMembership,
                                    contentDescription = "Travel CV",
                                    tint = Brand700,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BgSunken, CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Cài đặt & Tài khoản",
                                    tint = Brand700,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Avatar + Info
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
                            Text(
                                text = userProfile.displayName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )

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

                    // Action: Travel CV Generator Button
                    Button(
                        onClick = onOpenTravelCv,
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(999.dp))
                    ) {
                        Icon(
                            Icons.Default.CardMembership,
                            contentDescription = null,
                            tint = Gold500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tạo Thẻ Dấu Chân Du Lịch (Travel CV) ➔",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real Data Stats Row
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BgSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileStatItem("Tỉnh thành", "$visitedProvincesCount/$totalProvincesCount")
                            ProfileStatItem("Con dấu", "$totalStamps")
                            ProfileStatItem("Chuyến đi", "${trips.size}")
                            ProfileStatItem("Lượt lưu", "${savedPosts.size}")
                        }
                    }
                }
            }

            // 2. Vietnam Map Graphic
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    VietnamMapGraphic(
                        provinces = provinces,
                        onProvinceClick = onProvinceClick
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Segmented Profile Navigation Tabs (Chuyến đi & Bộ sưu tập đã lưu)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgSunken, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ProfileTabButton(
                            title = "Chuyến đi (${trips.size})",
                            icon = Icons.Default.Luggage,
                            isSelected = selectedTab == ProfileSubTab.TRIPS,
                            onClick = { selectedTab = ProfileSubTab.TRIPS },
                            modifier = Modifier.weight(1f)
                        )
                        ProfileTabButton(
                            title = "Đã lưu (${savedPosts.size})",
                            icon = Icons.Default.Bookmark,
                            isSelected = selectedTab == ProfileSubTab.SAVED,
                            onClick = { selectedTab = ProfileSubTab.SAVED },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 4. Tab Content: Either Vertical Scrolling Trips List OR Instagram/TikTok 3-Column Square Grid
            when (selectedTab) {
                ProfileSubTab.TRIPS -> {
                    if (trips.isEmpty()) {
                        item {
                            EmptyTripsState(onCreateTrip = onCreateTrip)
                        }
                    } else {
                        items(trips, key = { it.id }) { trip ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                ProfileTripCard(
                                    trip = trip,
                                    onClick = { onTripClick(trip) }
                                )
                            }
                        }
                    }
                }
                ProfileSubTab.SAVED -> {
                    if (savedPosts.isEmpty()) {
                        item {
                            EmptySavedState(onExploreFeed = onExploreFeed)
                        }
                    } else {
                        // 3-Column Instagram/TikTok Square Grid for Saved Posts
                        val chunkedPosts = savedPosts.chunked(3)
                        items(chunkedPosts) { rowPosts ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 1.5.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                for (post in rowPosts) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                    ) {
                                        InstaTikTokPostGridItem(
                                            post = post,
                                            onClick = { onPostClick(post) }
                                        )
                                    }
                                }
                                // Fill remaining spaces if row has < 3 items
                                for (i in 0 until (3 - rowPosts.size)) {
                                    Spacer(modifier = Modifier.weight(1f))
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
private fun ProfileTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) BgSurface else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Brand700 else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Brand700 else TextSecondary
            )
        }
    }
}

@Composable
private fun ProfileTripCard(
    trip: Trip,
    onClick: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getInstance(Locale("vi", "VN")) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Trip Image Header with Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                AsyncImage(
                    model = trip.coverUrl,
                    contentDescription = trip.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                // Status & AI Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (statusBg, statusText, statusColor) = when (trip.status) {
                        TripStatus.ONGOING -> Triple(Brand700, "Đang diễn ra", Color.White)
                        TripStatus.UPCOMING -> Triple(Gold500, "Sắp tới", TextOnGold)
                        TripStatus.PAST -> Triple(Color(0xFF616161), "Đã hoàn thành", Color.White)
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (trip.isAiGenerated) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Gold500,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "AI Thiết kế",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Destination & Duration at bottom of image
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = Gold500,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = trip.destination,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "${trip.startDate} - ${trip.endDate}",
                        fontSize = 10.sp,
                        color = Color(0xFFE0E0E0)
                    )
                }
            }

            // Trip Details
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = trip.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱ ${trip.durationText}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "📍 ${trip.spotsCount} địa điểm",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "💰 ${currencyFormatter.format(trip.totalEstimatedCostVnd)}đ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brand700
                        )
                    }

                    Text(
                        text = "Chi tiết ➔",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brand700
                    )
                }
            }
        }
    }
}

/**
 * 3-Column Square Grid Item styled like Instagram profile posts / TikTok saved bookmarks.
 */
@Composable
private fun InstaTikTokPostGridItem(
    post: Post,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .background(BgSunken)
    ) {
        AsyncImage(
            model = post.mediaUrls.firstOrNull() ?: "",
            contentDescription = post.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient overlay at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        // Multi-photo indicator (like Instagram / TikTok)
        if (post.mediaUrls.size > 1) {
            Icon(
                imageVector = Icons.Default.Collections,
                contentDescription = "Nhiều ảnh",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(13.dp)
                    .shadow(2.dp)
            )
        }

        // Attached trip indicator icon
        if (post.attachedTrip != null) {
            Surface(
                shape = CircleShape,
                color = Brand700,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Luggage,
                        contentDescription = "Có lịch trình",
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }
        }

        // Bottom Stats: Like Count (Instagram/TikTok style)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = if (post.isLiked) Color(0xFFFF5252) else Color.White,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = "${post.likeCount}",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EmptyTripsState(onCreateTrip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Luggage,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Chưa có chuyến đi nào",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Lên kế hoạch và lưu các hành trình khám phá Việt Nam của riêng bạn.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onCreateTrip,
            colors = ButtonDefaults.buttonColors(containerColor = Brand700),
            shape = RoundedCornerShape(999.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Gold500, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Lên lịch trình với AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptySavedState(onExploreFeed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.BookmarkBorder,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Chưa có bài viết nào được lưu",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Chạm vào biểu tượng bookmark ở bất kỳ bài viết nào trên Bảng tin để lưu lại xem sau.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onExploreFeed,
            colors = ButtonDefaults.buttonColors(containerColor = Brand700),
            shape = RoundedCornerShape(999.dp)
        ) {
            Text("Khám phá bảng tin ngay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
