package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LanguageDropdownButton(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    isDarkOverlay: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val isEn = currentLanguage.equals("en", ignoreCase = true)
    val displayFlag = if (isEn) "🇺🇸" else "🇻🇳"
    val displayCode = if (isEn) "US" else "VI"

    val containerBg = if (isDarkOverlay) {
        Color.Black.copy(alpha = 0.45f)
    } else {
        BgSunken
    }

    val contentColor = if (isDarkOverlay) {
        Color.White
    } else {
        Brand900
    }

    val borderColor = if (isDarkOverlay) {
        Color.White.copy(alpha = 0.3f)
    } else {
        TextTertiary.copy(alpha = 0.35f)
    }

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = containerBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { expanded = !expanded }
                .testTag("language_dropdown_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = displayFlag,
                    fontSize = 14.sp
                )
                Text(
                    text = displayCode,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Chọn ngôn ngữ / Select language",
                    tint = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = 4.dp),
            shape = RoundedCornerShape(14.dp),
            containerColor = BgSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TextTertiary.copy(alpha = 0.25f)),
            modifier = Modifier.widthIn(min = 140.dp)
        ) {
            // Option 1: Vietnamese (VI)
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🇻🇳", fontSize = 16.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "VI",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isEn) Brand700 else TextPrimary
                            )
                            Text(
                                text = "Tiếng Việt",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        if (!isEn) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Brand700,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                onClick = {
                    expanded = false
                    if (isEn) {
                        onLanguageSelected("vi")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lang_option_vi")
            )

            HorizontalDivider(
                color = TextTertiary.copy(alpha = 0.2f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Option 2: English (US)
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🇺🇸", fontSize = 16.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "US",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEn) Brand700 else TextPrimary
                            )
                            Text(
                                text = "English",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        if (isEn) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Brand700,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                onClick = {
                    expanded = false
                    if (!isEn) {
                        onLanguageSelected("en")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lang_option_en")
            )
        }
    }
}
