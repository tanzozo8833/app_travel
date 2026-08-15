package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.data.model.NotificationItem
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
    notifications: List<NotificationItem>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSocialTab by remember { mutableStateOf(false) }

    val filtered = notifications.filter { it.isSocial == isSocialTab }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
            }
            Text(
                text = "Thông Báo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Sub tabs: Của bạn (Hành trình, Dấu chân) | Hoạt động (Likes, Comments)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(BgSunken, RoundedCornerShape(999.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (!isSocialTab) Brand700 else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clickable { isSocialTab = false }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 7.dp)
                ) {
                    Text(
                        text = "🗺 Của bạn (Hành trình)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isSocialTab) Color.White else TextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (isSocialTab) Brand700 else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clickable { isSocialTab = true }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 7.dp)
                ) {
                    Text(
                        text = "💬 Hoạt động cộng đồng",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSocialTab) Color.White else TextSecondary
                    )
                }
            }
        }

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { notif ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (notif.actorAvatarUrl != null) {
                            AsyncImage(
                                model = notif.actorAvatarUrl,
                                contentDescription = notif.actorName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = Brand100,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "✦", color = Brand700, fontSize = 18.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notif.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = notif.message,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notif.timeAgo,
                                fontSize = 10.sp,
                                color = Brand700
                            )
                        }
                    }
                }
            }
        }
    }
}
