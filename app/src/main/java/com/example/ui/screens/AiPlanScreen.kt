package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trip
import com.example.ui.AiWizardState
import com.example.ui.ChatMessage
import com.example.ui.theme.*

enum class AiPlanSubTab {
    WIZARD,
    CHAT
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
    onSendMessage: (text: String, plugin: String?) -> Unit,
    onViewTrip: (Trip) -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableStateOf(AiPlanSubTab.WIZARD) }
    var chatInput by remember { mutableStateOf("") }

    val popularDestinations = listOf("Đà Lạt", "Hà Giang", "Đà Nẵng", "Hà Nội", "Phú Quốc", "Ninh Bình", "Sa Pa", "Huế")
    val durationOptions = listOf(2 to "2N1Đ", 3 to "3N2Đ", 4 to "4N3Đ", 5 to "5N4Đ")
    val groupTypes = listOf("Một mình", "Cặp đôi", "Bạn bè", "Gia đình")
    val availableStyles = listOf("Chill sương mù", "Săn mây", "Sống ảo", "Foodtour", "Phượt xe máy", "Biển xanh", "Di tích văn hoá")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "LUMINA AI TRIP ENGINE",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Brand700,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "Lập Lịch Trình Thông Minh",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sub tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgSunken, RoundedCornerShape(999.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (subTab == AiPlanSubTab.WIZARD) Brand700 else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { subTab = AiPlanSubTab.WIZARD }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    ) {
                        Text(
                            text = "✨ Tạo lịch trình (5 bước)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (subTab == AiPlanSubTab.WIZARD) Color.White else TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (subTab == AiPlanSubTab.CHAT) Brand700 else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { subTab = AiPlanSubTab.CHAT }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    ) {
                        Text(
                            text = "💬 Trợ lý AI du lịch",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (subTab == AiPlanSubTab.CHAT) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        if (subTab == AiPlanSubTab.WIZARD) {
            // WIZARD FORM
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Loading State Card
                if (wizardState.isGenerating) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Brand900),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = Gold500, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "AI ĐANG TẠO LỊCH TRÌNH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Gold500
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = wizardState.generationProgressMessage,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // 1. Điểm đến
                item {
                    Column {
                        Text(
                            text = "1. BẠN MUỐN ĐI ĐÂU?",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(popularDestinations) { dest ->
                                val isSelected = wizardState.destination == dest
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSelected) Brand700 else BgSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Gold500 else Color(0xFFD6DFDB)
                                    ),
                                    modifier = Modifier.clickable {
                                        onUpdateWizard(dest, null, null, null, null, null)
                                    }
                                ) {
                                    Text(
                                        text = "📍 $dest",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Thời lượng
                item {
                    Column {
                        Text(
                            text = "2. THỜI LƯỢNG CHUYẾN ĐI",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            durationOptions.forEach { (days, label) ->
                                val isSelected = wizardState.durationDays == days
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Brand700 else BgSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Gold500 else Color(0xFFD6DFDB)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            onUpdateWizard(null, days, null, null, null, null)
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Ngân sách
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. NGÂN SÁCH DỰ KIẾN",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700
                            )
                            Text(
                                text = "~${wizardState.budgetVnd / 1000000.0} triệu VNĐ / người",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Brand900
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Slider(
                            value = wizardState.budgetVnd.toFloat(),
                            onValueChange = {
                                onUpdateWizard(null, null, it.toLong(), null, null, null)
                            },
                            valueRange = 1000000f..8000000f,
                            steps = 14,
                            colors = SliderDefaults.colors(
                                thumbColor = Gold500,
                                activeTrackColor = Brand700,
                                inactiveTrackColor = Color(0xFFD3DCD5)
                            )
                        )
                    }
                }

                // 4. Bạn đồng hành
                item {
                    Column {
                        Text(
                            text = "4. BẠN ĐI CÙNG AI?",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            groupTypes.forEach { gType ->
                                val isSelected = wizardState.groupType == gType
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Brand700 else BgSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Gold500 else Color(0xFFD6DFDB)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            onUpdateWizard(null, null, null, gType, null, null)
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = gType,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Phong cách du lịch
                item {
                    Column {
                        Text(
                            text = "5. PHONG CÁCH YÊU THÍCH",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(availableStyles) { style ->
                                val isSelected = wizardState.styleTags.contains(style)
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSelected) Brand100 else BgSurface,
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
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Brand900 else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    Button(
                        onClick = onGeneratePlan,
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(6.dp, RoundedCornerShape(999.dp))
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Gold500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TẠO LỊCH TRÌNH BẰNG AI ✨",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // CHAT INTERFACE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                // Quick Plugin Pills
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        PluginButton(label = "🍜 Ăn gì?", onClick = { onSendMessage("Gợi ý quán ăn ngon đặc sản", "food") })
                    }
                    item {
                        PluginButton(label = "👗 Mặc gì?", onClick = { onSendMessage("Gợi ý outfit chụp ảnh đẹp", "outfit") })
                    }
                    item {
                        PluginButton(label = "💰 Chi phí?", onClick = { onSendMessage("Bảng chi phí chi tiết", "cost") })
                    }
                    item {
                        PluginButton(label = "📸 Góc chụp?", onClick = { onSendMessage("Góc chụp ảnh đẹp", "photo") })
                    }
                }

                // Messages list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        ChatBubble(msg)
                    }
                }

                // Input Box
                Surface(
                    color = BgSurface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Hỏi AI về quán cà phê, outfit, mẹo săn mây...", fontSize = 12.sp) },
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.weight(1f),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    val q = chatInput
                                    chatInput = ""
                                    onSendMessage(q, null)
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Brand700, CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Gửi", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PluginButton(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = BgSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = Brand900,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (msg.isUser) 16.dp else 4.dp,
                bottomEnd = if (msg.isUser) 4.dp else 16.dp
            ),
            color = if (msg.isUser) Brand700 else BgSurface,
            border = if (!msg.isUser) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)) else null,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Text(
                text = msg.text,
                fontSize = 13.sp,
                color = if (msg.isUser) Color.White else TextPrimary,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}
