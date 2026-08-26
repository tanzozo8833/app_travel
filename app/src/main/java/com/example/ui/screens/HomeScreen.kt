package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.Province
import com.example.data.model.Trip
import com.example.ui.FeedSubTab
import com.example.ui.components.PostCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    posts: List<Post>,
    provinces: List<Province>,
    currentSubTab: FeedSubTab,
    onSubTabChanged: (FeedSubTab) -> Unit,
    onPostClick: (Post) -> Unit,
    onTripClick: (Trip) -> Unit,
    onLikeClick: (Post) -> Unit,
    onSaveClick: (Post) -> Unit,
    onShareClick: (Post) -> Unit,
    onCommentClick: (Post) -> Unit,
    onQuickCheckInClick: () -> Unit,
    onProvinceClick: (Province) -> Unit,
    onPlanTripWithAi: (destination: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                label = "Gần bạn",
                isSelected = currentSubTab == FeedSubTab.NEARBY,
                onClick = { onSubTabChanged(FeedSubTab.NEARBY) }
            )
        }

        // 2-Column Staggered Grid (Thác nước):
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Hero Banner: Quick Passport Stamp Prompt
            item(span = StaggeredGridItemSpan.FullLine) {
                HeroCheckInBanner(onQuickCheckIn = onQuickCheckInClick)
            }

            // 2. Province Stories / Stamp Ring Tray
            item(span = StaggeredGridItemSpan.FullLine) {
                ProvinceStoryTray(
                    provinces = provinces,
                    onProvinceClick = onProvinceClick
                )
            }

            // 3. Filtered Posts: 2-Column Staggered Waterfall Layout
            val filteredPosts = when (currentSubTab) {
                FeedSubTab.EXPLORE -> posts
                FeedSubTab.FOLLOWING -> posts.filter { it.authorHandle in listOf("linh.travels", "vy.explorer") }
                FeedSubTab.NEARBY -> posts.filter { it.provinceCode == "LDG" }
            }

            items(filteredPosts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    onPostClick = onPostClick,
                    onTripClick = onTripClick,
                    onLikeClick = onLikeClick,
                    onSaveClick = onSaveClick,
                    onShareClick = onShareClick,
                    onCommentClick = onCommentClick
                )
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
    onQuickCheckIn: () -> Unit
) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ĐANG Ở LÂM ĐỒNG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Gold500,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Gần Đồi Chè Cầu Đất (200m)",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Chạm để đóng dấu hộ chiếu & tích luỹ dấu chân",
                        fontSize = 11.sp,
                        color = Color(0xFFC7E2D7)
                    )
                }

                Button(
                    onClick = onQuickCheckIn,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
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
