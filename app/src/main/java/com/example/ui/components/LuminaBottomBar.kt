package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.theme.*

@Composable
fun LuminaBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Pill Container
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color(0xFFFFFFFF).copy(alpha = 0.96f),
            shadowElevation = 14.dp,
            tonalElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5E0)),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: AI Plan
                NavPillItem(
                    label = "AI Plan",
                    iconFilled = Icons.Default.AutoAwesome,
                    iconOutlined = Icons.Outlined.AutoAwesome,
                    isSelected = currentTab == MainTab.AI_PLAN,
                    onClick = { onTabSelected(MainTab.AI_PLAN) }
                )

                // Tab 2: Chuyến đi
                NavPillItem(
                    label = "Chuyến đi",
                    iconFilled = Icons.Default.Luggage,
                    iconOutlined = Icons.Outlined.Luggage,
                    isSelected = currentTab == MainTab.MY_TRIPS,
                    onClick = { onTabSelected(MainTab.MY_TRIPS) }
                )

                // Tab 3: Bảng Home Feed
                NavPillItem(
                    label = "Feed",
                    iconFilled = Icons.Default.Home,
                    iconOutlined = Icons.Outlined.Home,
                    isSelected = currentTab == MainTab.HOME,
                    onClick = { onTabSelected(MainTab.HOME) }
                )

                // Tab 4: Stamp
                NavPillItem(
                    label = "Stamp",
                    iconFilled = Icons.Default.CameraAlt,
                    iconOutlined = Icons.Outlined.CameraAlt,
                    isSelected = currentTab == MainTab.CHECK_IN,
                    onClick = { onTabSelected(MainTab.CHECK_IN) }
                )

                // Tab 5: Profile
                NavPillItem(
                    label = "Tôi",
                    iconFilled = Icons.Default.Person,
                    iconOutlined = Icons.Outlined.Person,
                    isSelected = currentTab == MainTab.PROFILE,
                    onClick = { onTabSelected(MainTab.PROFILE) }
                )
            }
        }
    }
}

@Composable
private fun NavPillItem(
    label: String,
    iconFilled: ImageVector,
    iconOutlined: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tintColor = if (isSelected) Brand700 else TextTertiary
    val bgColor = if (isSelected) Brand100 else Color.Transparent

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple()
            ) { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (isSelected) iconFilled else iconOutlined,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = label,
                fontSize = 9.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = tintColor
            )
        }
    }
}
