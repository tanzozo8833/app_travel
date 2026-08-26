package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CheckInRecord
import com.example.data.model.Place
import com.example.data.model.Province
import com.example.data.model.Trip
import com.example.data.model.TripStatus
import com.example.ui.TripSubTab
import com.example.ui.components.TripCard
import com.example.ui.theme.*

enum class TripViewMode {
    ITINERARIES, // Lịch trình
    VIETNAM_MAP  // Bản đồ Việt Nam
}

@Composable
fun MyTripsScreen(
    trips: List<Trip>,
    provinces: List<Province>,
    places: List<Place> = emptyList(),
    checkIns: List<CheckInRecord>,
    currentSubTab: TripSubTab,
    onSubTabChanged: (TripSubTab) -> Unit,
    onTripClick: (Trip) -> Unit,
    onCreateNewTrip: () -> Unit,
    onQuickCheckIn: () -> Unit,
    onMapQuickCheckIn: (placeName: String, provinceCode: String, provinceName: String) -> Unit = { _, _, _ -> },
    onProvinceClick: (Province) -> Unit = {},
    onCreateRecapPost: (Trip) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(TripViewMode.ITINERARIES) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
    ) {
        // Main Mode Toggle: Lịch trình 📋 | Bản đồ Việt Nam 🗺️
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                .background(BgSunken, RoundedCornerShape(999.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (viewMode == TripViewMode.ITINERARIES) Brand700 else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewMode = TripViewMode.ITINERARIES }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FormatListBulleted,
                        contentDescription = null,
                        tint = if (viewMode == TripViewMode.ITINERARIES) Color.White else TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lịch trình (${trips.size})",
                        fontSize = 12.5.sp,
                        fontWeight = if (viewMode == TripViewMode.ITINERARIES) FontWeight.Bold else FontWeight.Medium,
                        color = if (viewMode == TripViewMode.ITINERARIES) Color.White else TextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (viewMode == TripViewMode.VIETNAM_MAP) Brand700 else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewMode = TripViewMode.VIETNAM_MAP }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        tint = if (viewMode == TripViewMode.VIETNAM_MAP) Color.White else TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bản đồ Việt Nam 🇻🇳",
                        fontSize = 12.5.sp,
                        fontWeight = if (viewMode == TripViewMode.VIETNAM_MAP) FontWeight.Bold else FontWeight.Medium,
                        color = if (viewMode == TripViewMode.VIETNAM_MAP) Color.White else TextSecondary
                    )
                }
            }
        }

        if (viewMode == TripViewMode.VIETNAM_MAP) {
            // Render the Full Interactive Map
            InteractiveMapScreen(
                places = places,
                provinces = provinces,
                checkIns = checkIns,
                onQuickCheckIn = onMapQuickCheckIn,
                onProvinceClick = onProvinceClick,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Render the Itineraries List
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LUMINA ITINERARIES",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Chuyến Đi Của Tôi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = onCreateNewTrip,
                    colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Tạo mới", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Sub Tabs: Sắp đi | Đang đi | Đã đi
            val ongoingCount = trips.count { it.status == TripStatus.ONGOING }
            val upcomingCount = trips.count { it.status == TripStatus.UPCOMING }
            val pastCount = trips.count { it.status == TripStatus.PAST }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(BgSunken, RoundedCornerShape(999.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TripSubTabPill(
                    label = "Đang đi ($ongoingCount)",
                    isSelected = currentSubTab == TripSubTab.ONGOING,
                    onClick = { onSubTabChanged(TripSubTab.ONGOING) }
                )
                TripSubTabPill(
                    label = "Sắp đi ($upcomingCount)",
                    isSelected = currentSubTab == TripSubTab.UPCOMING,
                    onClick = { onSubTabChanged(TripSubTab.UPCOMING) }
                )
                TripSubTabPill(
                    label = "Đã đi ($pastCount)",
                    isSelected = currentSubTab == TripSubTab.PAST,
                    onClick = { onSubTabChanged(TripSubTab.PAST) }
                )
            }

            val filteredTrips = when (currentSubTab) {
                TripSubTab.ONGOING -> trips.filter { it.status == TripStatus.ONGOING }
                TripSubTab.UPCOMING -> trips.filter { it.status == TripStatus.UPCOMING }
                TripSubTab.PAST -> trips.filter { it.status == TripStatus.PAST }
            }

            // List
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredTrips.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎒", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Chưa có chuyến đi nào trong mục này",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onCreateNewTrip,
                                colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(text = "Tạo lịch trình bằng AI ngay", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(filteredTrips, key = { it.id }) { trip ->
                        TripCard(
                            trip = trip,
                            onClick = { onTripClick(trip) },
                            onCheckInClick = onQuickCheckIn,
                            onCreateRecapClick = { onCreateRecapPost(trip) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TripSubTabPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (isSelected) Brand700 else Color.Transparent,
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}
