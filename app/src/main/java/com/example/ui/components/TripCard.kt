package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.example.data.model.Trip
import com.example.data.model.TripStatus
import com.example.ui.theme.*

@Composable
fun TripCard(
    trip: Trip,
    onClick: () -> Unit,
    onCheckInClick: (() -> Unit)? = null,
    onCreateRecapClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 16:10 Cover image with gradient text overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Brand900)
            ) {
                AsyncImage(
                    model = trip.coverUrl,
                    contentDescription = trip.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0x99000000), Color(0xEE04201A)),
                                startY = 80f
                            )
                        )
                )

                // Status & AI Badge at top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = when (trip.status) {
                            TripStatus.ONGOING -> Gold500
                            TripStatus.UPCOMING -> Brand700
                            TripStatus.PAST -> Color(0xFF5A6B64)
                        }
                    ) {
                        Text(
                            text = trip.status.titleVi,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (trip.status == TripStatus.ONGOING) TextOnGold else Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (trip.isAiGenerated) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xCC043027),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold500)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = Gold500,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "✨ AI Plan",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold500
                                )
                            }
                        }
                    }
                }

                // Bottom Content on Cover
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = trip.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📅 ${trip.startDate} - ${trip.endDate}",
                            fontSize = 11.sp,
                            color = Color(0xFFD6F0E2)
                        )
                        Text(text = "•", color = Color.LightGray, fontSize = 11.sp)
                        Text(
                            text = pluralStringResource(R.plurals.tripcard_spots, trip.spotsCount, trip.spotsCount),
                            fontSize = 11.sp,
                            color = Gold100
                        )
                    }
                }
            }

            // Ongoing Progress / Actions
            if (trip.status == TripStatus.ONGOING) {
                Surface(
                    color = Brand100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.tripcard_ongoing_badge),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700
                            )
                            Text(
                                text = "Điểm kế tiếp: Cà phê Thung Lũng",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Brand900
                            )
                        }

                        Button(
                            onClick = { onCheckInClick?.invoke() },
                            colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                            shape = RoundedCornerShape(999.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(text = stringResource(R.string.tripcard_stamp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (trip.status == TripStatus.PAST) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.tripcard_cost_per_person, (trip.totalEstimatedCostVnd / 1000).toInt()),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )

                    TextButton(
                        onClick = { onCreateRecapClick?.invoke() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Brand700)
                    ) {
                        Text(text = stringResource(R.string.tripcard_create_recap), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.tripcard_budget, (trip.totalEstimatedCostVnd / 1000000).toInt()),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Brand700
                    )
                    Text(
                        text = stringResource(R.string.tripcard_view_detail),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
