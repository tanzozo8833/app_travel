package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.model.Trip
import com.example.data.model.TripDay
import com.example.data.model.TripItem
import com.example.ui.components.EditTripDialog
import com.example.ui.components.WeatherCard
import com.example.ui.theme.*

@Composable
fun TripDetailScreen(
    trip: Trip,
    onBack: () -> Unit,
    onSaveTrip: (Trip) -> Unit,
    onUpdateTrip: (Trip) -> Unit,
    onDuplicateTrip: (String) -> Unit,
    onShareTrip: (Trip) -> Unit,
    onCheckInSpot: (TripItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }

    val currentDay = if (trip.days.isNotEmpty()) {
        trip.days.getOrNull(selectedDayIndex) ?: trip.days.first()
    } else null

    if (showEditDialog) {
        EditTripDialog(
            trip = trip,
            onDismiss = { showEditDialog = false },
            onSave = { updatedTrip ->
                onUpdateTrip(updatedTrip)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        bottomBar = {
            Surface(
                color = BgSurface,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit Form Button
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.tripdetail_edit), fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onDuplicateTrip(trip.id) },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.tripdetail_duplicate), fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onSaveTrip(trip) },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.tripdetail_save_trip), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            // 1. Hero Cover Image & Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    AsyncImage(
                        model = trip.coverUrl,
                        contentDescription = trip.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0x88000000), Color.Transparent, Color(0xEE04201A))
                                )
                            )
                    )

                    // Top Bar
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
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = Color.White)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { showEditDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color.White)
                            }
                            IconButton(
                                onClick = { onShareTrip(trip) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Chia sẻ", tint = Color.White)
                            }
                        }
                    }

                    // Title & Metadata
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        if (trip.isAiGenerated) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Gold500,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = "✨ LEVART AI ITINERARY",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextOnGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = trip.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 26.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "📍 ${trip.destination}", fontSize = 12.sp, color = Color(0xFFD7EFE6))
                            Text(text = "•", color = Color.White)
                            Text(text = "⏱ ${trip.durationText}", fontSize = 12.sp, color = Gold100)
                            Text(text = "•", color = Color.White)
                            Text(text = "💰 ~${trip.totalEstimatedCostVnd / 1000}k", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            // 2. Weather Summary Card
            item {
                Box(modifier = Modifier.padding(16.dp)) {
                    WeatherCard(weather = trip.weatherSummary)
                }
            }

            // 3. Day Tabs selector
            if (trip.days.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Text(
                            text = stringResource(R.string.tripdetail_days_header),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(trip.days.indices.toList()) { index ->
                                val day = trip.days[index]
                                val isSelected = selectedDayIndex == index
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Brand700 else BgSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Gold500 else Color(0xFFD6DFDB)
                                    ),
                                    modifier = Modifier.clickable { selectedDayIndex = index }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.tripdetail_day_number, day.dayNumber),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                        Text(
                                            text = day.dateText,
                                            fontSize = 10.sp,
                                            color = if (isSelected) Gold100 else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (currentDay != null) {
                            Text(
                                text = "📌 ${currentDay.title}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Brand900
                            )
                        }
                    }
                }

                // 4. Timeline Items
                if (currentDay != null) {
                    items(currentDay.items) { item ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            TimelineSpotCard(
                                item = item,
                                onCheckIn = { onCheckInSpot(item) }
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = stringResource(R.string.tripdetail_loading_places),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineSpotCard(
    item: TripItem,
    onCheckIn: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Time & Category Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Brand100
                ) {
                    Text(
                        text = "⏱ ${item.timeSlot}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brand900,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = BgSunken
                ) {
                    Text(
                        text = "${item.category.iconEmoji} ${item.category.titleVi}",
                        fontSize = 10.5.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 ${item.locationName} • ~${item.costVnd / 1000}k",
                    fontSize = 11.sp,
                    color = Brand700,
                    fontWeight = FontWeight.Medium
                )

                if (item.bookingLabel != null) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Gold100,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold500),
                        modifier = Modifier.clickable { /* Booking link */ }
                    ) {
                        Text(
                            text = "🎟 ${item.bookingLabel}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
