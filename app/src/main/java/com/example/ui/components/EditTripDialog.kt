package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.data.model.PlaceCategory
import com.example.data.model.Trip
import com.example.data.model.TripDay
import com.example.data.model.TripItem
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun EditTripDialog(
    trip: Trip,
    onDismiss: () -> Unit,
    onSave: (Trip) -> Unit
) {
    // General Trip Information
    var tripTitle by remember { mutableStateOf(trip.title) }
    var tripDestination by remember { mutableStateOf(trip.destination) }
    var groupPeopleCount by remember {
        val digits = trip.groupType.filter { it.isDigit() }
        mutableStateOf(if (digits.isNotBlank()) digits else "2")
    }

    // Days & Activities List (preserved from existing trip)
    var daysList by remember {
        mutableStateOf(
            if (trip.days.isNotEmpty()) {
                trip.days
            } else {
                listOf(
                    TripDay(
                        dayNumber = 1,
                        dateText = "Ngày 1",
                        title = "Ngày 1: Khám phá ${trip.destination.ifBlank { "điểm đến" }}",
                        items = emptyList()
                    )
                )
            }
        )
    }

    var selectedDayIndex by remember { mutableIntStateOf(0) }
    val currentSelectedDay = daysList.getOrNull(selectedDayIndex) ?: daysList.firstOrNull()

    // Calculated total cost across all spots
    val calculatedTotalCost = remember(daysList) {
        val sum = daysList.sumOf { d -> d.items.sumOf { it.costVnd } }
        if (sum > 0) sum else trip.totalEstimatedCostVnd
    }

    // Dialog state for adding/editing spot
    var showItemDialog by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableStateOf<Int?>(null) }
    var itemDialogTitle by remember { mutableStateOf("") }
    var itemDialogLocation by remember { mutableStateOf("") }
    var itemDialogTime by remember { mutableStateOf("08:00 - 10:00") }
    var itemDialogCost by remember { mutableStateOf("50") }
    var itemDialogDescription by remember { mutableStateOf("") }
    var itemDialogCategory by remember { mutableStateOf(PlaceCategory.PHOTO) }

    // Dialog state for editing Day title
    var showEditDayTitleDialog by remember { mutableStateOf(false) }
    var editDayTitleInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = BgCanvas,
                    shadowElevation = 3.dp,
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
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_close), tint = TextPrimary)
                        }
                        Text(
                            text = stringResource(R.string.edittrip_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Button(
                            onClick = {
                                val durationStr = if (daysList.size > 1) "${daysList.size}N${daysList.size - 1}Đ" else "1N"
                                val updatedTrip = trip.copy(
                                    title = tripTitle.ifBlank { "Chuyến đi $tripDestination" },
                                    destination = tripDestination.ifBlank { "Việt Nam" },
                                    durationText = durationStr,
                                    totalEstimatedCostVnd = calculatedTotalCost,
                                    groupType = "${groupPeopleCount.ifBlank { "2" }} người",
                                    days = daysList
                                )
                                onSave(updatedTrip)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                            shape = RoundedCornerShape(999.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Gold500, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.common_save_short), fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                // ==========================================
                // Section 1: Trip General Info Card
                // ==========================================
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.edittrip_section_general),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700
                            )

                            // Title
                            OutlinedTextField(
                                value = tripTitle,
                                onValueChange = { tripTitle = it },
                                label = { Text(stringResource(R.string.edittrip_field_name)) },
                                placeholder = { Text(stringResource(R.string.edittrip_field_name_hint, tripDestination)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Destination
                            OutlinedTextField(
                                value = tripDestination,
                                onValueChange = { tripDestination = it },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Brand700) },
                                label = { Text(stringResource(R.string.edittrip_field_destination)) },
                                placeholder = { Text(stringResource(R.string.edittrip_field_destination_hint)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Number of people
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = groupPeopleCount,
                                    onValueChange = { input ->
                                        groupPeopleCount = input.filter { it.isDigit() }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = Brand700) },
                                    label = { Text(stringResource(R.string.edittrip_field_group_size)) },
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    FilledTonalIconButton(
                                        onClick = {
                                            val current = groupPeopleCount.toIntOrNull() ?: 2
                                            val newCount = (current - 1).coerceAtLeast(1)
                                            groupPeopleCount = newCount.toString()
                                        },
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.edittrip_decrease), tint = Brand700)
                                    }

                                    FilledTonalIconButton(
                                        onClick = {
                                            val current = groupPeopleCount.toIntOrNull() ?: 2
                                            val newCount = current + 1
                                            groupPeopleCount = newCount.toString()
                                        },
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.edittrip_increase), tint = Brand700)
                                    }
                                }
                            }

                            // Estimated budget summary bar
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Brand100,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = pluralStringResource(R.plurals.edittrip_total_days, daysList.size, daysList.size),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Brand700
                                        )
                                        Text(
                                            text = stringResource(R.string.edittrip_total_cost),
                                            fontSize = 11.5.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Text(
                                        text = stringResource(R.string.edittrip_cost_value, NumberFormat.getNumberInstance(Locale.GERMANY).format(calculatedTotalCost.coerceAtLeast(100000L))),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Brand900
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // Section 2: Day Tabs Selector (Ngày 1, Ngày 2, + Thêm ngày)
                // ==========================================
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pluralStringResource(R.plurals.edittrip_days_header, daysList.size, daysList.size),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700
                            )

                            Button(
                                onClick = {
                                    val nextDayNum = daysList.size + 1
                                    val newDay = TripDay(
                                        dayNumber = nextDayNum,
                                        dateText = "Ngày $nextDayNum",
                                        title = "Ngày $nextDayNum: Khám phá $tripDestination",
                                        items = listOf(
                                            TripItem(
                                                timeSlot = "08:00 - 10:00",
                                                title = "Ăn sáng & Khởi hành",
                                                description = "Bắt đầu ngày mới tại $tripDestination",
                                                locationName = tripDestination,
                                                category = PlaceCategory.FOOD,
                                                costVnd = 50000L
                                            )
                                        )
                                    )
                                    daysList = daysList + newDay
                                    selectedDayIndex = daysList.size - 1
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Gold500)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = stringResource(R.string.edittrip_add_day), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day Tabs Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            itemsIndexed(daysList) { index, day ->
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
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.edittrip_day_label, day.dayNumber),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                        Text(
                                            text = pluralStringResource(R.plurals.edittrip_day_places, day.items.size, day.items.size),
                                            fontSize = 10.sp,
                                            color = if (isSelected) Gold100 else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // Section 3: Selected Day Content & Activities Timeline
                // ==========================================
                if (currentSelectedDay != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = BgSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                // Day Title & Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentSelectedDay.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Brand900
                                        )
                                        Text(
                                            text = pluralStringResource(R.plurals.edittrip_day_summary, currentSelectedDay.items.size, currentSelectedDay.dayNumber, currentSelectedDay.items.size),
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Edit day title button
                                        IconButton(
                                            onClick = {
                                                editDayTitleInput = currentSelectedDay.title
                                                showEditDayTitleDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edittrip_rename_day), tint = Brand700, modifier = Modifier.size(16.dp))
                                        }

                                        // Delete day button (if > 1 day)
                                        if (daysList.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    val updated = daysList.toMutableList()
                                                    updated.removeAt(selectedDayIndex)
                                                    // re-number days
                                                    val renumbered = updated.mapIndexed { idx, d ->
                                                        d.copy(
                                                            dayNumber = idx + 1,
                                                            dateText = "Ngày ${idx + 1}"
                                                        )
                                                    }
                                                    daysList = renumbered
                                                    selectedDayIndex = selectedDayIndex.coerceAtMost(renumbered.size - 1)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.edittrip_delete_day), tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFFE8EFEA))
                                Spacer(modifier = Modifier.height(10.dp))

                                // List of Timeline Spots in this Day
                                if (currentSelectedDay.items.isEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = BgCanvas,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = stringResource(R.string.edittrip_day_empty, currentSelectedDay.dayNumber),
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        currentSelectedDay.items.forEachIndexed { itemIdx, item ->
                                            Card(
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = BgCanvas),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0EAE5)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    // Time slot badge & category + Action buttons
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Surface(
                                                                shape = RoundedCornerShape(999.dp),
                                                                color = Brand100
                                                            ) {
                                                                Text(
                                                                    text = "⏱ ${item.timeSlot}",
                                                                    fontSize = 10.5.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Brand900,
                                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                                                )
                                                            }

                                                            Surface(
                                                                shape = RoundedCornerShape(999.dp),
                                                                color = BgSunken
                                                            ) {
                                                                Text(
                                                                    text = "${item.category.iconEmoji} ${item.category.titleVi}",
                                                                    fontSize = 9.5.sp,
                                                                    color = TextSecondary,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                                                                )
                                                            }
                                                        }

                                                        // Edit & Delete Activity item buttons
                                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                            IconButton(
                                                                onClick = {
                                                                    editingItemIndex = itemIdx
                                                                    itemDialogTitle = item.title
                                                                    itemDialogLocation = item.locationName
                                                                    itemDialogTime = item.timeSlot
                                                                    itemDialogCost = (item.costVnd / 1000).toString()
                                                                    itemDialogDescription = item.description
                                                                    itemDialogCategory = item.category
                                                                    showItemDialog = true
                                                                },
                                                                modifier = Modifier.size(26.dp)
                                                            ) {
                                                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.common_edit), tint = Brand700, modifier = Modifier.size(15.dp))
                                                            }

                                                            IconButton(
                                                                onClick = {
                                                                    val updatedDays = daysList.toMutableList()
                                                                    val currentItems = updatedDays[selectedDayIndex].items.toMutableList()
                                                                    currentItems.removeAt(itemIdx)
                                                                    updatedDays[selectedDayIndex] = updatedDays[selectedDayIndex].copy(items = currentItems)
                                                                    daysList = updatedDays
                                                                },
                                                                modifier = Modifier.size(26.dp)
                                                            ) {
                                                                Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.common_delete), tint = Color.Red, modifier = Modifier.size(15.dp))
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Text(
                                                        text = item.title,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )

                                                    if (item.description.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = item.description,
                                                            fontSize = 11.5.sp,
                                                            color = TextSecondary
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "📍 ${item.locationName}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Brand700
                                                        )
                                                        Text(
                                                            text = stringResource(R.string.edittrip_cost_value_approx, NumberFormat.getNumberInstance(Locale.GERMANY).format(item.costVnd)),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Brand900
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Button to Add Activity/Spot to this Day
                                OutlinedButton(
                                    onClick = {
                                        editingItemIndex = null
                                        itemDialogTitle = ""
                                        itemDialogLocation = tripDestination
                                        itemDialogTime = "08:00 - 10:00"
                                        itemDialogCost = "60"
                                        itemDialogDescription = ""
                                        itemDialogCategory = PlaceCategory.PHOTO
                                        showItemDialog = true
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Brand700),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Brand700),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.edittrip_add_activity_to_day, currentSelectedDay.dayNumber),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Save Button
                item {
                    Button(
                        onClick = {
                            val durationStr = if (daysList.size > 1) "${daysList.size}N${daysList.size - 1}Đ" else "1N"
                            val updatedTrip = trip.copy(
                                title = tripTitle.ifBlank { "Chuyến đi $tripDestination" },
                                destination = tripDestination.ifBlank { "Việt Nam" },
                                durationText = durationStr,
                                totalEstimatedCostVnd = calculatedTotalCost,
                                groupType = "${groupPeopleCount.ifBlank { "2" }} người",
                                days = daysList
                            )
                            onSave(updatedTrip)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Gold500, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.edittrip_save_itinerary),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ==========================================
        // Sub-Dialog 1: Add or Edit Activity Spot Dialog
        // ==========================================
        if (showItemDialog) {
            val isEditing = editingItemIndex != null
            AlertDialog(
                onDismissRequest = { showItemDialog = false },
                title = {
                    Text(
                        text = stringResource(if (isEditing) R.string.edittrip_item_edit_title else R.string.edittrip_item_add_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brand900
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = itemDialogTitle,
                                onValueChange = { itemDialogTitle = it },
                                label = { Text(stringResource(R.string.edittrip_item_name)) },
                                placeholder = { Text(stringResource(R.string.edittrip_item_name_hint)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = itemDialogLocation,
                                onValueChange = { itemDialogLocation = it },
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = Brand700) },
                                label = { Text(stringResource(R.string.edittrip_item_address)) },
                                placeholder = { Text(stringResource(R.string.edittrip_item_address_hint)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = itemDialogTime,
                                    onValueChange = { itemDialogTime = it },
                                    label = { Text(stringResource(R.string.edittrip_item_timeslot)) },
                                    placeholder = { Text("08:00 - 10:00") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = itemDialogCost,
                                    onValueChange = { itemDialogCost = it.filter { c -> c.isDigit() } },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    label = { Text(stringResource(R.string.edittrip_item_cost)) },
                                    placeholder = { Text("50") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Text(
                                text = stringResource(R.string.edittrip_item_category),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(PlaceCategory.entries.toTypedArray().toList()) { cat ->
                                    val isSelected = itemDialogCategory == cat
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Brand700 else BgCanvas,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) Gold500 else Color(0xFFD6DFDB)
                                        ),
                                        modifier = Modifier.clickable { itemDialogCategory = cat }
                                    ) {
                                        Text(
                                            text = "${cat.iconEmoji} ${cat.titleVi}",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = itemDialogDescription,
                                onValueChange = { itemDialogDescription = it },
                                label = { Text(stringResource(R.string.edittrip_item_note)) },
                                placeholder = { Text(stringResource(R.string.edittrip_item_note_hint)) },
                                minLines = 2,
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (itemDialogTitle.isNotBlank()) {
                                val costVal = (itemDialogCost.toLongOrNull() ?: 50L) * 1000L
                                val newItem = TripItem(
                                    timeSlot = itemDialogTime.ifBlank { "08:00 - 10:00" },
                                    title = itemDialogTitle,
                                    description = itemDialogDescription.ifBlank { "Trải nghiệm tại $itemDialogLocation" },
                                    locationName = itemDialogLocation.ifBlank { itemDialogTitle },
                                    category = itemDialogCategory,
                                    costVnd = costVal
                                )

                                val updatedDays = daysList.toMutableList()
                                val currentItems = updatedDays[selectedDayIndex].items.toMutableList()

                                if (isEditing) {
                                    currentItems[editingItemIndex!!] = newItem
                                } else {
                                    currentItems.add(newItem)
                                }

                                updatedDays[selectedDayIndex] = updatedDays[selectedDayIndex].copy(items = currentItems)
                                daysList = updatedDays
                                showItemDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(stringResource(if (isEditing) R.string.common_update else R.string.common_add))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showItemDialog = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }

        // ==========================================
        // Sub-Dialog 2: Edit Day Title Dialog
        // ==========================================
        if (showEditDayTitleDialog && currentSelectedDay != null) {
            AlertDialog(
                onDismissRequest = { showEditDayTitleDialog = false },
                title = {
                    Text(stringResource(R.string.edittrip_rename_day_title, currentSelectedDay.dayNumber), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                },
                text = {
                    OutlinedTextField(
                        value = editDayTitleInput,
                        onValueChange = { editDayTitleInput = it },
                        label = { Text(stringResource(R.string.edittrip_day_title_field)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editDayTitleInput.isNotBlank()) {
                                val updatedDays = daysList.toMutableList()
                                updatedDays[selectedDayIndex] = updatedDays[selectedDayIndex].copy(title = editDayTitleInput)
                                daysList = updatedDays
                            }
                            showEditDayTitleDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(stringResource(R.string.common_save_short))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDayTitleDialog = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }
    }
}
