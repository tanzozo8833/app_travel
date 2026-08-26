package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.Trip
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PostCard(
    post: Post,
    onPostClick: (Post) -> Unit,
    onTripClick: (Trip) -> Unit,
    onLikeClick: (Post) -> Unit,
    onSaveClick: (Post) -> Unit,
    onShareClick: (Post) -> Unit,
    onCommentClick: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var showHeartBurst by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0f) }

    fun triggerDoubleTapLike() {
        if (!post.isLiked) {
            onLikeClick(post)
        }
        coroutineScope.launch {
            showHeartBurst = true
            heartScale.snapTo(0.2f)
            heartScale.animateTo(
                targetValue = 1.3f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
            heartScale.animateTo(targetValue = 0.9f)
            delay(300)
            showHeartBurst = false
        }
    }

    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = Color(0xFFE4ECE8))
            .clickable { onPostClick(post) }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // 1. Author Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with 24h gold check-in ring
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = post.authorAvatarUrl,
                        contentDescription = post.authorName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (post.hasRecentCheckIn) 1.5.dp else 0.5.dp,
                                color = if (post.hasRecentCheckIn) Gold500 else Color(0xFFD3DCD5),
                                shape = CircleShape
                            )
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Trip Attached Pill
                        if (post.attachedTrip != null) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Brand100,
                                modifier = Modifier.clickable {
                                    onTripClick(post.attachedTrip)
                                }
                            ) {
                                Text(
                                    text = "Trip",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Brand700,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Location tag
                    Text(
                        text = post.locationLabel,
                        fontSize = 9.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 2. Photo Content Container with dynamic aspect ratio & double tap detector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(post.aspectRatio)
                    .background(BgSunken)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onPostClick(post) },
                            onDoubleTap = { triggerDoubleTapLike() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = post.mediaUrls.firstOrNull(),
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Multi-photo indicator
                if (post.mediaUrls.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "1/${post.mediaUrls.size}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Heart Burst Animation on Double Tap
                if (showHeartBurst) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Liked",
                        tint = HeartRed,
                        modifier = Modifier
                            .size(64.dp)
                            .scale(heartScale.value)
                            .shadow(8.dp, CircleShape)
                    )
                }
            }

            // 3. Caption Preview
            if (post.caption.isNotBlank()) {
                Text(
                    text = post.caption,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.5.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }

            // 4. Engagement Bar (♥ Like, ↗ Share, 💬 Comment, 🔖 Save)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                        tint = if (post.isLiked) HeartRed else TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${post.likeCount}",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (post.isLiked) HeartRed else TextSecondary
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
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${post.shareCount}",
                        fontSize = 9.5.sp,
                        color = TextSecondary
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
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${post.commentCount}",
                        fontSize = 9.5.sp,
                        color = TextSecondary
                    )
                }

                // Save
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSaveClick(post) }
                ) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Lưu",
                        tint = if (post.isSaved) Brand700 else TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${post.saveCount}",
                        fontSize = 9.5.sp,
                        color = if (post.isSaved) Brand700 else TextSecondary
                    )
                }
            }
        }
    }
}
