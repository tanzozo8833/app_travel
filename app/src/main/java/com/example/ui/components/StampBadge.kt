package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun StampBadge(
    provinceCode: String,
    provinceShortName: String,
    dateText: String,
    symbolEmoji: String = "★",
    size: Dp = 90.dp,
    isEarned: Boolean = true,
    animateDrop: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(if (animateDrop) 2.2f else 1f) }
    val rotation = remember { Animatable(if (animateDrop) -18f else -4f) }

    LaunchedEffect(animateDrop) {
        if (animateDrop) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            rotation.animateTo(
                targetValue = -5f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val borderColor = if (isEarned) Gold500 else Color(0xFFC4D1CB)
    val innerColor = if (isEarned) Gold100 else Color(0xFFECEFEF)
    val textColor = if (isEarned) TextPrimary else TextTertiary

    Box(
        modifier = modifier
            .size(size)
            .scale(scale.value)
            .rotate(rotation.value)
            .shadow(if (isEarned) 6.dp else 0.dp, CircleShape)
            .background(if (isEarned) BgSurface else Color(0xFFF0F3F2), CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Stamp border canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val outerRadius = (this.size.width / 2) - 2.dp.toPx()
            val innerRadius = outerRadius - 4.dp.toPx()

            // Outer ring
            drawCircle(
                color = borderColor,
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner dashed ring
            drawCircle(
                color = borderColor.copy(alpha = 0.7f),
                radius = innerRadius,
                center = center,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            )
        }

        // Stamp Inner Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(6.dp)
        ) {
            Text(
                text = "LUMINA STAMP",
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = borderColor,
                letterSpacing = 0.6.sp
            )

            Text(
                text = symbolEmoji,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 1.dp)
            )

            Text(
                text = provinceShortName.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                maxLines = 1
            )

            Text(
                text = dateText,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )
        }
    }
}
