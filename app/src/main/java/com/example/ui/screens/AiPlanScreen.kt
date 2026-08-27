package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlaceCategory
import com.example.data.model.Trip
import com.example.data.model.TripDay
import com.example.data.model.TripItem
import com.example.ui.AiWizardState
import com.example.ui.ChatMessage
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

enum class PlanMode {
    AI_GENERATOR,
    MANUAL_CREATE
}

@Composable
fun AiPlanScreen(
    wizardState: AiWizardState,
    chatMessages: List<ChatMessage>,
    onUpdateWizard: (
        destination: String?,
        durationDays: Int?,
        budgetVnd: Long?,
        groupType: String?,
        styleTags: List<String>?,
        customNotes: String?
    ) -> Unit,
    onGeneratePlan: () -> Unit,
    onCreateManualPlan: (title: String, destination: String, durationDays: Int, budgetVnd: Long, notes: String) -> Unit,
    onSaveManualTrip: (Trip) -> Unit,
    onSendMessage: (text: String, plugin: String?, contextInfo: String?) -> Unit,
    onViewTrip: (Trip) -> Unit,
    modifier: Modifier = Modifier
) {
    var planMode by remember { mutableStateOf(PlanMode.AI_GENERATOR) }

    // Text inputs for duration and budget (no sliders)
    var destinationInput by remember(wizardState.destination) { mutableStateOf(wizardState.destination) }
    var durationDaysInput by remember(wizardState.durationDays) { mutableStateOf(wizardState.durationDays.toString()) }
    var budgetInput by remember(wizardState.budgetVnd) { mutableStateOf(wizardState.budgetVnd.toString()) }
    var groupPeopleInput by remember(wizardState.groupType) {
        val digits = wizardState.groupType.filter { it.isDigit() }
        mutableStateOf(if (digits.isNotBlank()) digits else "2")
    }
    var customNotesInput by remember(wizardState.customNotes) { mutableStateOf(wizardState.customNotes) }

    // Manual plan state with rich days & activities list (Giống hệt UI xem/chỉnh sửa lịch trình)
    var manualTitle by remember { mutableStateOf("Chuyến đi ${wizardState.destination}") }
    var manualDestination by remember { mutableStateOf(wizardState.destination) }
    var selectedDayIndex by remember { mutableIntStateOf(0) }

    var manualDays by remember {
        mutableStateOf(
            listOf(
                TripDay(
                    dayNumber = 1,
                    dateText = "Ngày 1",
                    title = "Ngày 1: Khám phá trung tâm & ẩm thực",
                    items = listOf(
                        TripItem(
                            timeSlot = "08:00 - 10:00",
                            title = "Ăn sáng đặc sản địa phương",
                            description = "Thưởng thức món ngon đặc trưng của vùng miền",
                            locationName = "Trung tâm thành phố",
                            category = PlaceCategory.FOOD,
                            costVnd = 60000L
                        ),
                        TripItem(
                            timeSlot = "14:00 - 17:00",
                            title = "Check-in điểm tham quan nổi tiếng",
                            description = "Chụp ảnh lưu niệm và ngắm cảnh đẹp",
                            locationName = "Điểm tham quan chính",
                            category = PlaceCategory.PHOTO,
                            costVnd = 120000L
                        ),
                        TripItem(
                            timeSlot = "19:00 - 21:30",
                            title = "Dạo chợ đêm & ăn vặt",
                            description = "Trải nghiệm không khí sôi động về đêm",
                            locationName = "Khu phố đi bộ / Chợ đêm",
                            category = PlaceCategory.FOOD,
                            costVnd = 100000L
                        )
                    )
                ),
                TripDay(
                    dayNumber = 2,
                    dateText = "Ngày 2",
                    title = "Ngày 2: Trải nghiệm thiên nhiên & săn ảnh đẹp",
                    items = listOf(
                        TripItem(
                            timeSlot = "06:00 - 09:00",
                            title = "Săn mây / Ngắm bình minh",
                            description = "Đón những tia nắng sớm và không khí trong lành",
                            locationName = "Đồi ngắm cảnh",
                            category = PlaceCategory.PHOTO,
                            costVnd = 80000L
                        ),
                        TripItem(
                            timeSlot = "14:30 - 17:30",
                            title = "Ghé quán cà phê view đẹp",
                            description = "Nghỉ ngơi, chill và chụp ảnh hoàng hôn",
                            locationName = "Quán cà phê đồi",
                            category = PlaceCategory.CAFE,
                            costVnd = 75000L
                        )
                    )
                ),
                TripDay(
                    dayNumber = 3,
                    dateText = "Ngày 3",
                    title = "Ngày 3: Mua quà lưu niệm & tạm biệt",
                    items = listOf(
                        TripItem(
                            timeSlot = "09:00 - 11:30",
                            title = "Mua đặc sản làm quà",
                            description = "Chọn mua các món quà thủ công và đặc sản",
                            locationName = "Chợ địa phương",
                            category = PlaceCategory.CULTURE,
                            costVnd = 200000L
                        )
                    )
                )
            )
        )
    }

    // Dialog state for adding or editing an activity item
    var showItemDialog by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableStateOf<Int?>(null) } // null = adding new
    var itemDialogTitle by remember { mutableStateOf("") }
    var itemDialogLocation by remember { mutableStateOf("") }
    var itemDialogTime by remember { mutableStateOf("08:00 - 10:00") }
    var itemDialogCost by remember { mutableStateOf("50") }
    var itemDialogDescription by remember { mutableStateOf("") }
    var itemDialogCategory by remember { mutableStateOf(PlaceCategory.PHOTO) }

    // Dialog for editing day info
    var showEditDayDialog by remember { mutableStateOf(false) }
    var editDayTitleInput by remember { mutableStateOf("") }

    // Chat states
    var chatInput by remember { mutableStateOf("") }
    var selectedChatContext by remember { mutableStateOf("Điểm đến: ${wizardState.destination}") }

    val popularDestinations = listOf("Đà Lạt", "Hà Giang", "Đà Nẵng", "Hà Nội", "Phú Quốc", "Ninh Bình", "Sa Pa", "Huế")
    val durationOptions = listOf(2 to "2N1Đ", 3 to "3N2Đ", 4 to "4N3Đ", 5 to "5N4Đ")
    val groupTypes = listOf("Một mình", "Cặp đôi", "Bạn bè", "Gia đình")
    val availableStyles = listOf("Chill sương mù", "Săn mây", "Sống ảo", "Foodtour", "Phượt xe máy", "Biển xanh", "Di tích văn hoá")

    val chatContextOptions = listOf(
        "Điểm đến: ${destinationInput.ifBlank { "Đà Lạt" }}",
        "Thời lượng: ${durationDaysInput} ngày",
        "Ngân sách: ${try { (budgetInput.toLong() / 1000000.0).toString() + "tr" } catch (_: Exception) { "Tiết kiệm" }}",
        "Ăn uống & Đặc sản",
        "Góc ảnh & Sống ảo",
        "Toàn bộ kế hoạch"
    )

    fun formatVndDisplay(amountStr: String): String {
        return try {
            val num = amountStr.replace(".", "").replace(",", "").toLong()
            val formatter = NumberFormat.getNumberInstance(Locale.GERMANY)
            "${formatter.format(num)} đ (${num / 1000000.0} triệu VNĐ)"
        } catch (_: Exception) {
            "3.500.000 đ"
        }
    }

    // Auto calculate total cost from manual items
    val manualCalculatedCost = remember(manualDays) {
        manualDays.sumOf { day -> day.items.sumOf { it.costVnd } }
    }

    val currentSelectedDay = manualDays.getOrNull(selectedDayIndex) ?: manualDays.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "LUMINA AI & LỊCH TRÌNH DU LỊCH",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Brand700,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "Kế Hoạch & Trợ Lý Du Lịch",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Plan Mode Switcher (AI vs Manual)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgSunken, RoundedCornerShape(999.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (planMode == PlanMode.AI_GENERATOR) Brand700 else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { planMode = PlanMode.AI_GENERATOR }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    ) {
                        Text(
                            text = "Lập lịch trình bằng AI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (planMode == PlanMode.AI_GENERATOR) Color.White else TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (planMode == PlanMode.MANUAL_CREATE) Brand700 else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { planMode = PlanMode.MANUAL_CREATE }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    ) {
                        Text(
                            text = "Tạo lịch trình thủ công",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (planMode == PlanMode.MANUAL_CREATE) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        // Unified Single Screen (Scrollable Form + Integrated Itinerary Builder + AI Chat)
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Loading State Card for AI
            if (wizardState.isGenerating && planMode == PlanMode.AI_GENERATOR) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Brand900),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Gold500, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "AI ĐANG THIẾT LẬP LỊCH TRÌNH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Gold500
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = wizardState.generationProgressMessage,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ==========================================
            // MODE 1: AI GENERATOR FORM
            // ==========================================
            if (planMode == PlanMode.AI_GENERATOR) {
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
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Destination Input (Typed + Quick Selection Chips)
                            Column {
                                Text(
                                    text = "ĐIỂM ĐẾN DU LỊCH",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Brand700
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = destinationInput,
                                    onValueChange = {
                                        destinationInput = it
                                        onUpdateWizard(it, null, null, null, null, null)
                                    },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Brand700) },
                                    placeholder = { Text("Nhập địa điểm (Đà Lạt, Hà Giang, Phú Quốc...)", fontSize = 13.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(popularDestinations) { dest ->
                                        val isSelected = destinationInput.trim().equals(dest, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (isSelected) Brand700 else BgCanvas,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) Gold500 else Color(0xFFD6DFDB)
                                            ),
                                            modifier = Modifier.clickable {
                                                destinationInput = dest
                                                onUpdateWizard(dest, null, null, null, null, null)
                                            }
                                        ) {
                                            Text(
                                                text = dest,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else TextPrimary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. Duration Input (Typed number)
                            Column {
                                Text(
                                    text = "THỜI LƯỢNG (SỐ NGÀY)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Brand700
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = durationDaysInput,
                                        onValueChange = {
                                            durationDaysInput = it
                                            val days = it.toIntOrNull() ?: 1
                                            onUpdateWizard(null, days, null, null, null, null)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Brand700) },
                                        label = { Text("Số ngày") },
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    durationOptions.forEach { (days, label) ->
                                        val isSelected = durationDaysInput == days.toString()
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) Brand700 else BgCanvas,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) Gold500 else Color(0xFFD6DFDB)
                                            ),
                                            modifier = Modifier.clickable {
                                                durationDaysInput = days.toString()
                                                onUpdateWizard(null, days, null, null, null, null)
                                            }
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else TextPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Budget Input (Typed number)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "NGÂN SÁCH DỰ KIẾN (VNĐ)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Brand700
                                    )
                                    Text(
                                        text = formatVndDisplay(budgetInput),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Brand900
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = budgetInput,
                                    onValueChange = {
                                        budgetInput = it
                                        val budget = it.replace(".", "").replace(",", "").toLongOrNull() ?: 3000000L
                                        onUpdateWizard(null, null, budget, null, null, null)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Brand700) },
                                    placeholder = { Text("Nhập ngân sách ví dụ: 3500000", fontSize = 13.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // 4. Group Size (Number of people input)
                            Column {
                                Text(
                                    text = "SỐ LƯỢNG NGƯỜI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Brand700
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = groupPeopleInput,
                                        onValueChange = { input ->
                                            val digits = input.filter { it.isDigit() }
                                            groupPeopleInput = digits
                                            val count = digits.toIntOrNull() ?: 1
                                            onUpdateWizard(null, null, null, "$count người", null, null)
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = Brand700) },
                                        label = { Text("Số người tham gia") },
                                        placeholder = { Text("Ví dụ: 1, 2, 4, 8...") },
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Quick stepper buttons (- and +)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        FilledTonalIconButton(
                                            onClick = {
                                                val current = groupPeopleInput.toIntOrNull() ?: 2
                                                val newCount = (current - 1).coerceAtLeast(1)
                                                groupPeopleInput = newCount.toString()
                                                onUpdateWizard(null, null, null, "$newCount người", null, null)
                                            },
                                            modifier = Modifier.size(46.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = Brand700)
                                        }

                                        FilledTonalIconButton(
                                            onClick = {
                                                val current = groupPeopleInput.toIntOrNull() ?: 2
                                                val newCount = current + 1
                                                groupPeopleInput = newCount.toString()
                                                onUpdateWizard(null, null, null, "$newCount người", null, null)
                                            },
                                            modifier = Modifier.size(46.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Tăng", tint = Brand700)
                                        }
                                    }
                                }
                            }

                            Column {
                                Text(
                                    text = "PHONG CÁCH TRẢI NGHIỆM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Brand700
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(availableStyles) { style ->
                                        val isSelected = wizardState.styleTags.contains(style)
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (isSelected) Brand100 else BgCanvas,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) Brand700 else Color(0xFFD6DFDB)
                                            ),
                                            modifier = Modifier.clickable {
                                                val newTags = if (isSelected) {
                                                    wizardState.styleTags - style
                                                } else {
                                                    wizardState.styleTags + style
                                                }
                                                onUpdateWizard(null, null, null, null, newTags, null)
                                            }
                                        ) {
                                            Text(
                                                text = if (isSelected) "✓ $style" else "+ $style",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Brand900 else TextPrimary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // 5. Notes input
                            Column {
                                Text(
                                    text = "GHI CHÚ THÊM CHO KẾ HOẠCH",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Brand700
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = customNotesInput,
                                    onValueChange = {
                                        customNotesInput = it
                                        onUpdateWizard(null, null, null, null, null, it)
                                    },
                                    placeholder = { Text("Ví dụ: Thích ghé quán cà phê view đồi thông, muốn dậy sớm săn mây...", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(75.dp)
                                )
                            }

                            Button(
                                onClick = onGeneratePlan,
                                colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Gold500, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TẠO LỊCH TRÌNH BẰNG AI",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // MODE 2: FULL MANUAL ITINERARY BUILDER
            // (UI GIỐNG HỆT XEM VÀ CHỈNH SỬA LỊCH TRÌNH CŨ: THÊM BỚT SỬA XOÁ CÁC NGÀY VÀ CÁC HOẠT ĐỘNG TRONG NGÀY)
            // ==========================================
            if (planMode == PlanMode.MANUAL_CREATE) {
                // Section 1: Trip General Info Card
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
                                text = "THÔNG TIN CHUNG CHUYẾN ĐI",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700
                            )

                            // Title
                            OutlinedTextField(
                                value = manualTitle,
                                onValueChange = { manualTitle = it },
                                label = { Text("Tên chuyến đi") },
                                placeholder = { Text("Ví dụ: Hành trình khám phá Đà Lạt 3N2Đ") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Destination
                            OutlinedTextField(
                                value = manualDestination,
                                onValueChange = { manualDestination = it },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Brand700) },
                                label = { Text("Điểm đến (Tỉnh / Thành phố)") },
                                placeholder = { Text("Ví dụ: Đà Lạt, Lâm Đồng") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Estimated budget display & summary bar
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
                                            text = "TỔNG SỐ NGÀY: ${manualDays.size} NGÀY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Brand700
                                        )
                                        Text(
                                            text = "Tổng chi phí dự tính:",
                                            fontSize = 11.5.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Text(
                                        text = "${NumberFormat.getNumberInstance(Locale.GERMANY).format(manualCalculatedCost.coerceAtLeast(100000L))} đ",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Brand900
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Day Tabs Selector (Ngày 1, Ngày 2, Ngày 3, + Thêm ngày)
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LỊCH TRÌNH CÁC NGÀY (${manualDays.size} NGÀY)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700
                            )

                            Button(
                                onClick = {
                                    val nextDayNum = manualDays.size + 1
                                    val newDay = TripDay(
                                        dayNumber = nextDayNum,
                                        dateText = "Ngày $nextDayNum",
                                        title = "Ngày $nextDayNum: Khám phá $manualDestination",
                                        items = listOf(
                                            TripItem(
                                                timeSlot = "08:00 - 10:00",
                                                title = "Ăn sáng & khởi hành",
                                                description = "Bắt đầu ngày mới tại $manualDestination",
                                                locationName = manualDestination,
                                                category = PlaceCategory.FOOD,
                                                costVnd = 50000L
                                            )
                                        )
                                    )
                                    manualDays = manualDays + newDay
                                    selectedDayIndex = manualDays.size - 1
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Gold500)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Thêm ngày", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day Tabs Row (Identical to Trip Detail Screen)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            itemsIndexed(manualDays) { index, day ->
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
                                            text = "Ngày ${day.dayNumber}",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                        Text(
                                            text = "${day.items.size} địa điểm",
                                            fontSize = 10.sp,
                                            color = if (isSelected) Gold100 else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Selected Day Content & Activities Timeline
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
                                // Day Title & Actions (Edit Title, Delete Day, Add Activity)
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
                                            text = "Ngày ${currentSelectedDay.dayNumber} • ${currentSelectedDay.items.size} hoạt động",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Edit day title button
                                        IconButton(
                                            onClick = {
                                                editDayTitleInput = currentSelectedDay.title
                                                showEditDayDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Đổi tên ngày", tint = Brand700, modifier = Modifier.size(16.dp))
                                        }

                                        // Delete day button (if > 1 day)
                                        if (manualDays.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    val updated = manualDays.toMutableList()
                                                    updated.removeAt(selectedDayIndex)
                                                    // re-number days
                                                    val renumbered = updated.mapIndexed { idx, d ->
                                                        d.copy(
                                                            dayNumber = idx + 1,
                                                            dateText = "Ngày ${idx + 1}"
                                                        )
                                                    }
                                                    manualDays = renumbered
                                                    selectedDayIndex = selectedDayIndex.coerceAtMost(renumbered.size - 1)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Xoá ngày", tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFFE8EFEA))
                                Spacer(modifier = Modifier.height(10.dp))

                                // List of Timeline Spots in this Day (Styling matching TripDetailScreen)
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
                                                text = "Chưa có hoạt động nào trong Ngày ${currentSelectedDay.dayNumber}",
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
                                                                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Brand700, modifier = Modifier.size(15.dp))
                                                            }

                                                            IconButton(
                                                                onClick = {
                                                                    val updatedDays = manualDays.toMutableList()
                                                                    val currentItems = updatedDays[selectedDayIndex].items.toMutableList()
                                                                    currentItems.removeAt(itemIdx)
                                                                    updatedDays[selectedDayIndex] = updatedDays[selectedDayIndex].copy(items = currentItems)
                                                                    manualDays = updatedDays
                                                                },
                                                                modifier = Modifier.size(26.dp)
                                                            ) {
                                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Xoá", tint = Color.Red, modifier = Modifier.size(15.dp))
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
                                                            text = "~${NumberFormat.getNumberInstance(Locale.GERMANY).format(item.costVnd)} đ",
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
                                        itemDialogLocation = manualDestination
                                        itemDialogTime = "08:00 - 10:00"
                                        itemDialogCost = "60"
                                        itemDialogDescription = ""
                                        itemDialogCategory = PlaceCategory.PHOTO
                                        showItemDialog = true
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Brand700),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Thêm hoạt động cho Ngày ${currentSelectedDay.dayNumber}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 4: Final Save Trip Button
                item {
                    Button(
                        onClick = {
                            val finalTrip = Trip(
                                id = "trip_custom_${System.currentTimeMillis()}",
                                title = manualTitle.ifBlank { "Chuyến đi $manualDestination" },
                                destination = manualDestination.ifBlank { "Đà Lạt" },
                                provinceCode = "VN",
                                coverUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
                                startDate = "Hôm nay",
                                endDate = "Ngày ${manualDays.size}",
                                durationText = "${manualDays.size}N${(manualDays.size - 1).coerceAtLeast(1)}Đ",
                                totalEstimatedCostVnd = manualCalculatedCost.coerceAtLeast(100000L),
                                styleTags = listOf("Tự thiết kế", "Khám phá"),
                                isAiGenerated = false,
                                days = manualDays
                            )
                            onSaveManualTrip(finalTrip)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = TextOnGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LƯU VÀ XEM CHI TIẾT LỊCH TRÌNH",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextOnGold
                        )
                    }
                }
            }

            // ==========================================
            // SECTION 3: INTEGRATED AI ASSISTANT CHAT
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
                            .padding(16.dp)
                    ) {
                        // Context Selection Title & Row
                        Text(
                            text = "CHỌN CONTEXT CHO TRỢ LÝ AI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(chatContextOptions) { ctx ->
                                val isSelected = selectedChatContext == ctx
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSelected) Brand700 else BgCanvas,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Gold500 else Color(0xFFD6DFDB)
                                    ),
                                    modifier = Modifier.clickable { selectedChatContext = ctx }
                                ) {
                                    Text(
                                        text = ctx,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Query Plugins
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Brand100,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onSendMessage(
                                            "Gợi ý 5 quán ăn ngon chuẩn vị địa phương tại ${destinationInput.ifBlank { "Đà Lạt" }}",
                                            "food",
                                            selectedChatContext
                                        )
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                    Text("Ăn gì ngon", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Brand900)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Brand100,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onSendMessage(
                                            "Gợi ý outfit chụp ảnh và thời tiết thích hợp tại ${destinationInput.ifBlank { "Đà Lạt" }}",
                                            "outfit",
                                            selectedChatContext
                                        )
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                    Text("Mặc gì đẹp", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Brand900)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Brand100,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onSendMessage(
                                            "Bảng ước tính chi phí tối ưu theo ngân sách $budgetInput VNĐ",
                                            "cost",
                                            selectedChatContext
                                        )
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                    Text("Dự toán chi phí", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Brand900)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Chat Messages Stream
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            chatMessages.takeLast(6).forEach { msg ->
                                ChatBubble(msg)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chat Input Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Hỏi AI với context: $selectedChatContext", fontSize = 12.sp) },
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.weight(1f),
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (chatInput.isNotBlank()) {
                                        val q = chatInput
                                        chatInput = ""
                                        onSendMessage(q, null, selectedChatContext)
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Brand700, CircleShape)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Gửi", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG: ADD OR EDIT ACTIVITY ITEM
    // ==========================================
    if (showItemDialog) {
        AlertDialog(
            onDismissRequest = { showItemDialog = false },
            title = {
                Text(
                    text = if (editingItemIndex == null) "Thêm Hoạt Động Mới" else "Chỉnh Sửa Hoạt Động",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brand900
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = itemDialogTitle,
                        onValueChange = { itemDialogTitle = it },
                        label = { Text("Tên hoạt động / Điểm đến") },
                        placeholder = { Text("Ví dụ: Ăn sáng bún bò, check-in đồi chè...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = itemDialogLocation,
                        onValueChange = { itemDialogLocation = it },
                        label = { Text("Địa chỉ / Tên địa điểm") },
                        placeholder = { Text("Ví dụ: Cầu Đất, Đà Lạt") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = itemDialogTime,
                            onValueChange = { itemDialogTime = it },
                            label = { Text("Khung giờ") },
                            placeholder = { Text("08:00 - 10:00") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        )

                        OutlinedTextField(
                            value = itemDialogCost,
                            onValueChange = { itemDialogCost = it },
                            label = { Text("Chi phí (k)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = itemDialogDescription,
                        onValueChange = { itemDialogDescription = it },
                        label = { Text("Mô tả / Kinh nghiệm") },
                        placeholder = { Text("Góc chụp đẹp, món ăn nên gọi...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category selection
                    Column {
                        Text(text = "Danh mục:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(PlaceCategory.values().toList()) { cat ->
                                val isSelected = itemDialogCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
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
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (itemDialogTitle.isNotBlank()) {
                            val costNum = (itemDialogCost.toLongOrNull() ?: 50L) * 1000L
                            val newItem = TripItem(
                                timeSlot = itemDialogTime.ifBlank { "08:00 - 10:00" },
                                title = itemDialogTitle,
                                description = itemDialogDescription,
                                locationName = itemDialogLocation.ifBlank { itemDialogTitle },
                                category = itemDialogCategory,
                                costVnd = costNum
                            )

                            val updatedDays = manualDays.toMutableList()
                            val currentItems = updatedDays[selectedDayIndex].items.toMutableList()

                            if (editingItemIndex != null && editingItemIndex!! in currentItems.indices) {
                                currentItems[editingItemIndex!!] = newItem
                            } else {
                                currentItems.add(newItem)
                            }

                            updatedDays[selectedDayIndex] = updatedDays[selectedDayIndex].copy(items = currentItems)
                            manualDays = updatedDays
                            showItemDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(if (editingItemIndex == null) "Thêm" else "Cập nhật", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showItemDialog = false }) {
                    Text("Huỷ")
                }
            }
        )
    }

    // ==========================================
    // DIALOG: EDIT DAY TITLE
    // ==========================================
    if (showEditDayDialog && currentSelectedDay != null) {
        AlertDialog(
            onDismissRequest = { showEditDayDialog = false },
            title = {
                Text(
                    text = "Đổi Tiêu Đề Ngày ${currentSelectedDay.dayNumber}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brand900
                )
            },
            text = {
                OutlinedTextField(
                    value = editDayTitleInput,
                    onValueChange = { editDayTitleInput = it },
                    label = { Text("Tiêu đề ngày") },
                    placeholder = { Text("Ví dụ: Ngày 1: Săn mây và ngắm hoàng hôn") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editDayTitleInput.isNotBlank()) {
                            val updated = manualDays.toMutableList()
                            updated[selectedDayIndex] = updated[selectedDayIndex].copy(title = editDayTitleInput)
                            manualDays = updated
                            showEditDayDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Lưu", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDayDialog = false }) {
                    Text("Huỷ")
                }
            }
        )
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (msg.isUser) 14.dp else 4.dp,
                bottomEnd = if (msg.isUser) 4.dp else 14.dp
            ),
            color = if (msg.isUser) Brand700 else BgCanvas,
            border = if (!msg.isUser) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)) else null,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.text,
                fontSize = 12.sp,
                color = if (msg.isUser) Color.White else TextPrimary,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
