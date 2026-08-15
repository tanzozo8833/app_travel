package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    post: Post,
    onDismiss: () -> Unit,
    onAddComment: (postId: String, text: String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    val sampleComments = remember(post.id) {
        mutableStateListOf(
            Comment(
                postId = post.id,
                authorName = "Minh Trí",
                authorHandle = "tri.wanderer",
                authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop",
                content = "Góc chụp này đỉnh thật sự! Bạn canh lúc mấy giờ sáng thế ạ?",
                timeAgo = "15 phút trước",
                likeCount = 8
            ),
            Comment(
                postId = post.id,
                authorName = "Khánh Vy",
                authorHandle = "vy.explorer",
                authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop",
                content = "Mê Việt Nam mình quá đi mất 🇻🇳 Tuần tới mình cũng lên lịch trình giống bạn!",
                timeAgo = "1 giờ trước",
                likeCount = 12
            ),
            Comment(
                postId = post.id,
                authorName = "Đức Anh",
                authorHandle = "anh.vietnam",
                authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop",
                content = "Đồ ăn ở đây giá cả thế nào bạn ơi? Có cần book trước không?",
                timeAgo = "3 giờ trước",
                likeCount = 4
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BgSurface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp),
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFFD6DFDB)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bình luận (${sampleComments.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextSecondary)
                }
            }

            HorizontalDivider(color = Color(0xFFE8EFEA))

            // Comments list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(sampleComments) { comment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = comment.authorAvatarUrl,
                            contentDescription = comment.authorName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = comment.authorName,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${comment.timeAgo}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = comment.content,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }

                        // Like comment
                        var isLiked by remember { mutableStateOf(comment.isLiked) }
                        var likeCount by remember { mutableIntStateOf(comment.likeCount) }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                isLiked = !isLiked
                                likeCount += if (isLiked) 1 else -1
                            }
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isLiked) HeartRed else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            if (likeCount > 0) {
                                Text(
                                    text = "$likeCount",
                                    fontSize = 9.sp,
                                    color = if (isLiked) HeartRed else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = BgCanvas,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Viết bình luận hoặc đặt câu hỏi...", fontSize = 12.5.sp) },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                sampleComments.add(
                                    0,
                                    Comment(
                                        postId = post.id,
                                        authorName = "Thảo Linh",
                                        authorHandle = "linh.travels",
                                        authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop",
                                        content = inputText,
                                        timeAgo = "Vừa xong",
                                        likeCount = 0
                                    )
                                )
                                onAddComment(post.id, inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Brand700, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Gửi",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
