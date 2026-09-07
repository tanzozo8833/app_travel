package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.example.data.model.Post
import com.example.data.model.Trip
import com.example.ui.components.PostCard
import com.example.ui.components.TripCard
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PostDetailScreen(
    post: Post,
    allPosts: List<Post> = emptyList(),
    followedTargetKeys: Set<String> = emptySet(),
    currentUserId: String = "",
    currentUsername: String = "",
    onFollowClick: (Post) -> Unit = {},
    onAuthorClick: (Post) -> Unit = {},
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
    val isFollowing = post.userId.isNotBlank() && post.userId in followedTargetKeys
    val isSelf = post.userId == currentUserId
    val mediaUrls = if (post.mediaUrls.isNotEmpty()) post.mediaUrls else listOf("https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop")
    val pagerState = rememberPagerState(pageCount = { mediaUrls.size })

    val context = LocalContext.current
    var containerAspectRatio by remember(mediaUrls) { mutableStateOf(post.aspectRatio) }

    // Measure all images in the post to find the tallest/largest photo (minimum aspect ratio = max height)
    LaunchedEffect(mediaUrls) {
        withContext(Dispatchers.IO) {
            try {
                var minRatio = post.aspectRatio
                val loader = ImageLoader(context)
                for (url in mediaUrls) {
                    var width = -1
                    var height = -1
                    if (url.startsWith("content://") || url.startsWith("file://")) {
                        val uri = Uri.parse(url)
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeStream(stream, null, options)
                            width = options.outWidth
                            height = options.outHeight
                        }
                    } else if (url.startsWith("http://") || url.startsWith("https://")) {
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .size(coil.size.Size.ORIGINAL)
                            .build()
                        val result = loader.execute(request)
                        if (result is SuccessResult) {
                            width = result.drawable.intrinsicWidth
                            height = result.drawable.intrinsicHeight
                        }
                    }
                    if (width > 0 && height > 0) {
                        val r = width.toFloat() / height.toFloat()
                        if (r < minRatio) {
                            minRatio = r
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    containerAspectRatio = minRatio
                }
            } catch (_: Exception) {}
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = TextPrimary)
                    }
                    Text(
                        text = stringResource(R.string.postdetail_title),
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
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onAuthorClick(post) },
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
                    }

                    if (!isSelf) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isFollowing) BgCanvas else Brand100,
                            border = if (isFollowing) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0DDD6)) else null,
                            modifier = Modifier.clickable { onFollowClick(post) }
                        ) {
                            Text(
                                text = stringResource(if (isFollowing) R.string.post_action_following else R.string.postdetail_follow_cta),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFollowing) TextSecondary else Brand700,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // 2. Photo Carousel with Multi-image Swiper & Dot Indicators (Flex height according to largest photo to eliminate jitter)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(containerAspectRatio)
                        .background(Color.Black)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = mediaUrls[page],
                                contentDescription = post.caption,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
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
                                contentDescription = stringResource(R.string.post_action_like),
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
                                contentDescription = stringResource(R.string.post_action_comment),
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
                                contentDescription = stringResource(R.string.common_share),
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
                            contentDescription = stringResource(R.string.postdetail_save_post),
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
                            text = stringResource(R.string.postdetail_attached_trip),
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
                                text = pluralStringResource(R.plurals.postdetail_view_comments, post.commentCount, post.commentCount),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = stringResource(R.string.postdetail_comments_hint),
                                fontSize = 11.5.sp,
                                color = TextSecondary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Brand700)
                    }
                }
            }

            // 7. RELATED POSTS / FEED SUGGESTIONS (Format identical to feed layout with 0 spacing)
            val relatedList = allPosts.filter { it.id != post.id }
            if (relatedList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                            Text(
                                text = stringResource(R.string.postdetail_related_title),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700
                            )
                            Text(
                                text = stringResource(R.string.postdetail_related_desc),
                                fontSize = 11.5.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2-Column paired feed stream with zero spacing
                        val chunked = relatedList.chunked(2)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            chunked.forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        val p0 = pair[0]
                                        val p0IsFollowing = p0.userId.isNotBlank() && p0.userId in followedTargetKeys
                                        val p0IsSelf = p0.userId == currentUserId
                                        PostCard(
                                            post = p0,
                                            isFollowing = p0IsFollowing,
                                            isSelf = p0IsSelf,
                                            onFollowClick = { onFollowClick(p0) },
                                            onAuthorClick = onAuthorClick,
                                            onPostClick = onPostClick,
                                            onTripClick = onTripClick,
                                            onLikeClick = onLikeClick,
                                            onSaveClick = onSaveClick,
                                            onShareClick = onShareClick,
                                            onCommentClick = onCommentClick
                                        )
                                    }
                                    if (pair.size > 1) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            val p1 = pair[1]
                                            val p1IsFollowing = p1.userId.isNotBlank() && p1.userId in followedTargetKeys
                                            val p1IsSelf = p1.userId == currentUserId
                                            PostCard(
                                                post = p1,
                                                isFollowing = p1IsFollowing,
                                                isSelf = p1IsSelf,
                                                onFollowClick = { onFollowClick(p1) },
                                                onAuthorClick = onAuthorClick,
                                                onPostClick = onPostClick,
                                                onTripClick = onTripClick,
                                                onLikeClick = onLikeClick,
                                                onSaveClick = onSaveClick,
                                                onShareClick = onShareClick,
                                                onCommentClick = onCommentClick
                                            )
                                        }
                                    } else {
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
}

