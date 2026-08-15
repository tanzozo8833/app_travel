package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.Trip
import com.example.ui.components.TripCard
import com.example.ui.theme.*

@Composable
fun PostDetailScreen(
    post: Post,
    onBack: () -> Unit,
    onTripClick: (Trip) -> Unit,
    onLikeClick: (Post) -> Unit,
    onSaveClick: (Post) -> Unit,
    onShareClick: (Post) -> Unit,
    onCommentClick: (Post) -> Unit,
    onAddComment: (postId: String, content: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFollowed by remember { mutableStateOf(false) }

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
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                    }
                    Text(
                        text = "Bài Viết Chi Tiết",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { onShareClick(post) }) {
                        Icon(Icons.Default.Share, contentDescription = "Chia sẻ", tint = TextPrimary)
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = BgSurface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick button opening the comment bottom sheet popup
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = BgCanvas,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onCommentClick(post) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bình luận về bài viết...",
                                fontSize = 12.5.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Instant Save/Bookmark
                    IconButton(
                        onClick = { onSaveClick(post) },
                        modifier = Modifier
                            .size(38.dp)
                            .background(if (post.isSaved) Brand100 else BgCanvas, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Lưu bài viết",
                            tint = if (post.isSaved) Brand700 else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
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
        ) {
            // Author header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = post.authorAvatarUrl,
                        contentDescription = post.authorName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                if (post.hasRecentCheckIn) Gold500 else Color(0xFFD6DFDB),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.authorName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "📍 ${post.locationLabel} • ${post.timeAgo}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isFollowed) BgCanvas else Brand100,
                        border = if (isFollowed) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0DDD6)) else null,
                        modifier = Modifier.clickable { isFollowed = !isFollowed }
                    ) {
                        Text(
                            text = if (isFollowed) "Đã theo dõi" else "+ Theo dõi",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFollowed) TextSecondary else Brand700,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Photo: Flexible ratio matching image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(post.aspectRatio)
                        .background(BgSunken)
                ) {
                    AsyncImage(
                        model = post.mediaUrls.firstOrNull(),
                        contentDescription = post.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Action toolbar (Like, Comment popup trigger, Share, Save)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onLikeClick(post) }
                        ) {
                            Icon(
                                imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Thích",
                                tint = if (post.isLiked) HeartRed else TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${post.likeCount}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (post.isLiked) HeartRed else TextPrimary
                            )
                        }

                        // Comment (Clicking opens Popup Bottom Sheet)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onCommentClick(post) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Bình luận",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${post.commentCount}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Share
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onShareClick(post) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Chia sẻ",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${post.shareCount}",
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    // Save Bookmark (Instant state change)
                    IconButton(onClick = { onSaveClick(post) }) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Lưu bài viết",
                            tint = if (post.isSaved) Brand700 else TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Caption
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Text(
                        text = post.caption,
                        fontSize = 14.5.sp,
                        color = TextPrimary,
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        post.tags.forEach { tag ->
                            Text(
                                text = "#$tag",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Brand700
                            )
                        }
                    }
                }
            }

            // Attached Trip Card
            if (post.attachedTrip != null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "LỊCH TRÌNH ĐƯỢC ĐÍNH KÈM",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brand700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TripCard(
                            trip = post.attachedTrip,
                            onClick = { onTripClick(post.attachedTrip) }
                        )
                    }
                }
            }

            // Comments Preview Banner (Clicking opens full comment sheet)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { onCommentClick(post) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "💬 Xem ${post.commentCount} bình luận & chia sẻ",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Chạm để mở khung bình luận hoặc đặt câu hỏi",
                                fontSize = 11.5.sp,
                                color = TextSecondary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Brand700)
                    }
                }
            }
        }
    }
}
