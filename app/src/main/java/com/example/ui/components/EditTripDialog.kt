package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PlaceCategory
import com.example.data.model.Trip
import com.example.data.model.TripDay
import com.example.data.model.TripItem
import com.example.ui.theme.*

@Composable
fun EditTripDialog(
    trip: Trip,
    onDismiss: () -> Unit,
    onSave: (Trip) -> Unit
) {
    var title by remember { mutableStateOf(trip.title) }
    var destination by remember { mutableStateOf(trip.destination) }
    var durationText by remember { mutableStateOf(trip.durationText) }
    var budgetText by remember { mutableStateOf((trip.totalEstimatedCostVnd / 1000).toString()) }
    var groupType by remember { mutableStateOf(trip.groupType) }

    // Mutable list of days & items
    var daysList by remember { mutableStateOf(trip.days) }

    // Add item form state
    var showAddItemSheet by remember { mutableStateOf(false) }
    var targetDayIndex by remember { mutableIntStateOf(0) }
    var newItemTime by remember { mutableStateOf("08:00 - 10:00") }
    var newItemTitle by remember { mutableStateOf("") }
    var newItemLocation by remember { mutableStateOf("") }
    var newItemCost by remember { mutableStateOf("50") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
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
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextPrimary)
                        }
                        Text(
                            text = "Chỉnh Sửa Lịch Trình",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Button(
                            onClick = {
                                val cost = (budgetText.toLongOrNull() ?: 2400) * 1000
                                val updatedTrip = trip.copy(
                                    title = title,
                                    destination = destination,
                                    durationText = durationText,
                                    totalEstimatedCostVnd = cost,
                                    groupType = groupType,
                                    days = daysList
                                )
                                onSave(updatedTrip)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(text = "Lưu", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgCanvas)
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Basic Info Form
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "THÔNG TIN CHUNG CHUYẾN ĐI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700
                            )

                            // Title
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Tên chuyến đi") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Destination
                            OutlinedTextField(
                                value = destination,
                                onValueChange = { destination = it },
                                label = { Text("Điểm đến (Tỉnh / Thành)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = durationText,
                                    onValueChange = { durationText = it },
                                    label = { Text("Thời lượng") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = budgetText,
                                    onValueChange = { budgetText = it },
                                    label = { Text("Chi phí (nghìn VNĐ)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Section 2: Days & Activities Timeline Builder
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LỊCH TRÌNH CÁC NGÀY (${daysList.size} NGÀY)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700
                        )

                        TextButton(
                            onClick = {
                                val nextDayNum = daysList.size + 1
                                val newDay = TripDay(
                                    dayNumber = nextDayNum,
                                    dateText = "Ngày $nextDayNum",
                                    title = "Khám phá ngày $nextDayNum",
                                    items = emptyList()
                                )
                                daysList = daysList + newDay
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Thêm ngày", fontSize = 12.sp)
                        }
                    }
                }

                itemsIndexed(daysList) { dayIndex, day ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📅 Ngày ${day.dayNumber}: ${day.title}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Brand900
                                )

                                IconButton(
                                    onClick = {
                                        targetDayIndex = dayIndex
                                        showAddItemSheet = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = "Thêm điểm đến", tint = Brand700)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (day.items.isEmpty()) {
                                Text(
                                    text = "Chưa có địa điểm nào. Bấm (+) để thêm.",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            } else {
                                day.items.forEachIndexed { itemIndex, item ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = BgCanvas,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "⏱ ${item.timeSlot} • ${item.title}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "📍 ${item.locationName} • ~${item.costVnd / 1000}k",
                                                    fontSize = 10.5.sp,
                                                    color = TextSecondary
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    val updatedDays = daysList.toMutableList()
                                                    val currentItems = updatedDays[dayIndex].items.toMutableList()
                                                    currentItems.removeAt(itemIndex)
                                                    updatedDays[dayIndex] = updatedDays[dayIndex].copy(items = currentItems)
                                                    daysList = updatedDays
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Xoá", tint = Color.Red, modifier = Modifier.size(16.dp))
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

        // Sub Dialog: Add item to a Day
        if (showAddItemSheet) {
            AlertDialog(
                onDismissRequest = { showAddItemSheet = false },
                title = {
                    Text(text = "Thêm Điểm Đến Mới", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newItemTitle,
                            onValueChange = { newItemTitle = it },
                            label = { Text("Tên hoạt động / Quán ăn / Điểm chụp") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newItemLocation,
                            onValueChange = { newItemLocation = it },
                            label = { Text("Địa điểm") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newItemTime,
                                onValueChange = { newItemTime = it },
                                label = { Text("Khung giờ") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newItemCost,
                                onValueChange = { newItemCost = it },
                                label = { Text("Chi phí (k)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newItemTitle.isNotBlank()) {
                                val item = TripItem(
                                    timeSlot = newItemTime,
                                    title = newItemTitle,
                                    description = "Ghé thăm $newItemLocation",
                                    locationName = newItemLocation.ifBlank { newItemTitle },
                                    category = PlaceCategory.PHOTO,
                                    costVnd = (newItemCost.toLongOrNull() ?: 50) * 1000
                                )
                                val updatedDays = daysList.toMutableList()
                                val currentItems = updatedDays[targetDayIndex].items.toMutableList()
                                currentItems.add(item)
                                updatedDays[targetDayIndex] = updatedDays[targetDayIndex].copy(items = currentItems)
                                daysList = updatedDays

                                newItemTitle = ""
                                newItemLocation = ""
                                showAddItemSheet = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700)
                    ) {
                        Text("Thêm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddItemSheet = false }) {
                        Text("Huỷ")
                    }
                }
            )
        }
    }
}
