package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.Trip
import com.example.data.model.UserProfile
import com.example.ui.UserProfileTarget
import com.example.ui.components.PostCard
import com.example.ui.theme.*

enum class OtherProfileTab {
    POSTS_FEED,
    POSTS_GRID
}

@Composable
fun UserProfileDetailScreen(
    target: UserProfileTarget,
    dbProfile: UserProfile?,
    allPosts: List<Post>,
    followedTargetKeys: Set<String>,
    currentUserId: String,
    currentUsername: String,
    followerCount: Int,
    followingCount: Int,
    onFollowClick: (userId: String, handle: String, name: String, avatarUrl: String) -> Unit,
    onBack: () -> Unit,
    onPostClick: (Post) -> Unit,
    onTripClick: (Trip) -> Unit,
    onLikeClick: (Post) -> Unit,
    onSaveClick: (Post) -> Unit,
    onShareClick: (Post) -> Unit,
    onCommentClick: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Resolve Profile Info with Fallback to Post Data
    val resolvedUserId = dbProfile?.id ?: target.userId.ifBlank { "u_fallback" }
    val resolvedUsername = dbProfile?.username ?: target.username.ifBlank { "traveler" }
    val resolvedDisplayName = dbProfile?.displayName ?: target.displayName.ifBlank { "Lữ khách Levart" }
    val resolvedAvatarUrl = dbProfile?.avatarUrl ?: target.avatarUrl.ifBlank {
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
    }
    val resolvedBio = dbProfile?.bio ?: target.bio.ifBlank {
        "Thành viên yêu thích du lịch, check-in và khám phá 34 tỉnh thành Việt Nam trên Levart."
    }
    val resolvedTagTitle = dbProfile?.tagTitle ?: "Lữ Khách Levart"

    // 2. Identify if this is the current logged-in user
    val isSelf = resolvedUserId == currentUserId

    // 3. Follow state synced with followedTargetKeys (strictly userId)
    val isFollowing = resolvedUserId.isNotBlank() && resolvedUserId in followedTargetKeys

    // 4. Follower and Following counts directly from server
    val effectiveFollowers = followerCount
    val effectiveFollowing = followingCount

    // 5. Filter user's posts
    val userPosts = remember(allPosts, resolvedUserId, resolvedUsername, resolvedDisplayName) {
        allPosts.filter { post ->
            (resolvedUserId.isNotBlank() && post.userId == resolvedUserId) ||
            (resolvedUsername.isNotBlank() && (post.authorHandle.equals(resolvedUsername, ignoreCase = true) || post.authorHandle.equals("@$resolvedUsername", ignoreCase = true))) ||
            (resolvedDisplayName.isNotBlank() && post.authorName.equals(resolvedDisplayName, ignoreCase = true))
        }
    }

    var selectedTab by remember { mutableStateOf(OtherProfileTab.POSTS_FEED) }

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
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = TextPrimary
                        )
                    }

                    Text(
                        text = stringResource(R.string.profiledetail_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = { onShareClick(userPosts.firstOrNull() ?: Post(id = "p_profile", authorName = resolvedDisplayName, authorHandle = resolvedUsername, authorAvatarUrl = resolvedAvatarUrl, locationLabel = "Việt Nam", timeAgo = "", caption = resolvedBio)) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.profiledetail_share),
                            tint = TextSecondary
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgCanvas)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            // 1. Profile Header Hero
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar with gold border
                        AsyncImage(
                            model = resolvedAvatarUrl,
                            contentDescription = resolvedDisplayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, Gold500, CircleShape)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = resolvedDisplayName,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )

                            Text(
                                text = "@$resolvedUsername",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Gold500
                            ) {
                                Text(
                                    text = resolvedTagTitle,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextOnGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bio
                    Text(
                        text = resolvedBio,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Follow / Following Action Button (Only if not self)
                    if (!isSelf) {
                        Button(
                            onClick = {
                                onFollowClick(
                                    resolvedUserId,
                                    resolvedUsername,
                                    resolvedDisplayName,
                                    resolvedAvatarUrl
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) BgSurface else Brand700,
                                contentColor = if (isFollowing) TextPrimary else Color.White
                            ),
                            border = if (isFollowing) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)) else null,
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("user_profile_follow_button")
                        ) {
                            if (isFollowing) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Brand700,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.profile_stat_following),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Gold500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.postdetail_follow_cta),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = BgSunken,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.profiledetail_own),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Social & Activity Stats Card
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
                            ProfileHeaderStatItem("Đang theo dõi", "$effectiveFollowing")
                            ProfileHeaderStatItem("Người theo dõi", "$effectiveFollowers")
                            ProfileHeaderStatItem("Bài viết", "${userPosts.size}")
                            if (dbProfile != null && dbProfile.visitedProvincesCount > 0) {
                                ProfileHeaderStatItem("Tỉnh thành", "${dbProfile.visitedProvincesCount}/34")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 2. Tab switcher: Dòng bảng tin | Lưới ảnh
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
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedTab == OtherProfileTab.POSTS_FEED) BgSurface else Color.Transparent,
                            shadowElevation = if (selectedTab == OtherProfileTab.POSTS_FEED) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = OtherProfileTab.POSTS_FEED }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewAgenda,
                                    contentDescription = null,
                                    tint = if (selectedTab == OtherProfileTab.POSTS_FEED) Brand700 else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.profiledetail_tab_feed, userPosts.size),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == OtherProfileTab.POSTS_FEED) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == OtherProfileTab.POSTS_FEED) Brand700 else TextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedTab == OtherProfileTab.POSTS_GRID) BgSurface else Color.Transparent,
                            shadowElevation = if (selectedTab == OtherProfileTab.POSTS_GRID) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = OtherProfileTab.POSTS_GRID }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = if (selectedTab == OtherProfileTab.POSTS_GRID) Brand700 else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.profiledetail_tab_grid, userPosts.size),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == OtherProfileTab.POSTS_GRID) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == OtherProfileTab.POSTS_GRID) Brand700 else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. Post Content Display
            if (userPosts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.profiledetail_empty_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.profiledetail_empty_desc),
                            fontSize = 12.5.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                when (selectedTab) {
                    OtherProfileTab.POSTS_FEED -> {
                        // 2-column waterfall cards for user's posts
                        val chunked = userPosts.chunked(2)
                        items(chunked) { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    val p0 = pair[0]
                                    val p0IsFollowing = p0.userId.isNotBlank() && p0.userId in followedTargetKeys
                                    val p0IsSelf = p0.userId == currentUserId
                                    PostCard(
                                        post = p0,
                                        isFollowing = p0IsFollowing,
                                        isSelf = p0IsSelf,
                                        onFollowClick = {
                                            onFollowClick(p0.userId, p0.authorHandle, p0.authorName, p0.authorAvatarUrl)
                                        },
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
                                            onFollowClick = {
                                                onFollowClick(p1.userId, p1.authorHandle, p1.authorName, p1.authorAvatarUrl)
                                            },
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

                    OtherProfileTab.POSTS_GRID -> {
                        // 3-Column Instagram/TikTok Square Grid
                        val chunked = userPosts.chunked(3)
                        items(chunked) { rowPosts ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                for (post in rowPosts) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BgSunken)
                                            .clickable { onPostClick(post) }
                                    ) {
                                        AsyncImage(
                                            model = post.mediaUrls.firstOrNull() ?: "",
                                            contentDescription = post.caption,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Dark bottom gradient
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            Color.Transparent,
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.6f)
                                                        )
                                                    )
                                                )
                                        )

                                        // Likes count at bottom
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = null,
                                                tint = if (post.isLiked) HeartRed else Color.White,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = "${post.likeCount}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
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
private fun ProfileHeaderStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Brand900
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary
        )
    }
}
