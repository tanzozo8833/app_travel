package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.example.data.model.Post
import com.example.data.model.Trip
import com.example.ui.components.TripCard
import com.example.ui.theme.*

@Composable
fun PostDetailScreen(
    post: Post,
    allPosts: List<Post> = emptyList(),
    onPostClick: (Post) -> Unit = {},
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
    val mediaUrls = if (post.mediaUrls.isNotEmpty()) post.mediaUrls else listOf("https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop")
    val pagerState = rememberPagerState(pageCount = { mediaUrls.size })

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
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BgCanvas)
                .padding(innerPadding)
                .padding(bottom = 32.dp)
        ) {
            // 1. Author header
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
                            text = "${post.locationLabel} • ${post.timeAgo}",
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

            // 2. Photo Carousel with Multi-image Swiper & Dot Indicators
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(post.aspectRatio)
                        .background(BgSunken)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = mediaUrls[page],
                            contentDescription = post.caption,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Multi-photo count badge (e.g. 1/4)
                    if (mediaUrls.size > 1) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${mediaUrls.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }

                        // Dot indicators
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            repeat(mediaUrls.size) { index ->
                                val isCurrent = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .size(if (isCurrent) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isCurrent) Gold500 else Color.White.copy(alpha = 0.6f))
                                )
                            }
                        }
                    }
                }
            }

            // 3. Action toolbar (Like, Comment popup trigger, Share, Save)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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

                        // Comment
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

                    // Save Bookmark
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

            // 4. Caption & Tags
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Text(
                        text = post.caption,
                        fontSize = 14.5.sp,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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

            // 5. Attached Trip Card
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
                            fontFamily = FontFamily.Monospace,
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

            // 6. Comments Preview
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
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
                                text = "Xem ${post.commentCount} bình luận & thảo luận",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Chạm để mở khung trao đổi hoặc hỏi kinh nghiệm",
                                fontSize = 11.5.sp,
                                color = TextSecondary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Brand700)
                    }
                }
            }

            // 7. RELATED POSTS / FEED SUGGESTIONS (Vertical downward list with rich cards)
            val relatedList = allPosts.filter { it.id != post.id }
            if (relatedList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "GỢI Ý BÀI VIẾT LIÊN QUAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700
                        )
                        Text(
                            text = "Khám phá thêm góc nhìn và mẹo từ các tín đồ xê dịch khác",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            relatedList.forEach { relatedPost ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPostClick(relatedPost) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 110.dp, height = 95.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(BgSunken)
                                        ) {
                                            AsyncImage(
                                                model = relatedPost.mediaUrls.firstOrNull(),
                                                contentDescription = relatedPost.caption,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            if (relatedPost.mediaUrls.size > 1) {
                                                Surface(
                                                    shape = RoundedCornerShape(999.dp),
                                                    color = Color.Black.copy(alpha = 0.65f),
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(4.dp)
                                                ) {
                                                    Text(
                                                        text = "1/${relatedPost.mediaUrls.size}",
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = relatedPost.authorName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = relatedPost.timeAgo,
                                                    fontSize = 10.sp,
                                                    color = TextSecondary
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = Brand700,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = relatedPost.locationLabel,
                                                    fontSize = 10.5.sp,
                                                    color = Brand700,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Text(
                                                text = relatedPost.caption,
                                                fontSize = 11.5.sp,
                                                color = TextPrimary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 16.sp
                                            )

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Favorite,
                                                        contentDescription = null,
                                                        tint = HeartRed,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "${relatedPost.likeCount}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextSecondary
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.ChatBubbleOutline,
                                                        contentDescription = null,
                                                        tint = TextSecondary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "${relatedPost.commentCount}",
                                                        fontSize = 10.sp,
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
            }
        }
    }
}

