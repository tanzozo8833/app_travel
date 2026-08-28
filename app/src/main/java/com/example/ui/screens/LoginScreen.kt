package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    isNetworkError: Boolean,
    canFallbackToBrowser: Boolean = false,
    onSignInWithGoogle: (Activity) -> Unit,
    onSignInWithBrowser: (Activity) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
    ) {
        // Background Hero Image & Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1528127269322-539801943592?w=1080&auto=format&fit=crop",
                contentDescription = "Lumina Vietnam Travel",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.65f),
                                BgCanvas
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // App Brand Logo Badge
            Surface(
                shape = CircleShape,
                color = Brand700,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(76.dp)
                    .border(3.dp, Gold500, CircleShape)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Lumina Logo",
                        tint = Gold500,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Name & Tagline
            Text(
                text = "LUMINA TRAVEL",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                color = Color.White
            )

            Text(
                text = "Hộ Chiếu & Hành Trình Lữ Hành Việt Nam 🇻🇳",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Main Card: Login Area
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CHÀO MỪNG BẠN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Brand700,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Đăng Nhập Để Bắt Đầu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Đăng nhập tài khoản Google để tự động đồng bộ tên, ảnh đại diện và lưu dấu hộ chiếu 34 tỉnh thành.",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error Alert (If Any)
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isNetworkError) Color(0xFFFFF3CD) else Color(0xFFFFEBEE),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isNetworkError) Color(0xFFFFEEBA) else Color(0xFFFFCDD2)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 18.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (isNetworkError) Icons.Outlined.WifiOff else Icons.Outlined.ErrorOutline,
                                        contentDescription = null,
                                        tint = if (isNetworkError) Color(0xFF856404) else DangerRed,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isNetworkError) "Lỗi kết nối mạng" else "Đăng nhập không thành công",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNetworkError) Color(0xFF856404) else DangerRed
                                        )
                                        Text(
                                            text = errorMessage ?: "",
                                            fontSize = 12.sp,
                                            color = TextPrimary,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = onClearError,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Đóng",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (canFallbackToBrowser && activity != null) {
                                        TextButton(
                                            onClick = {
                                                onClearError()
                                                onSignInWithBrowser(activity)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.OpenInBrowser,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = Brand700
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Mở trình duyệt",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Brand700
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onClearError()
                                            activity?.let { onSignInWithGoogle(it) }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Thử lại", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 1. Primary Google Sign-In Button (One-Tap / Play Services)
                    Button(
                        onClick = {
                            if (activity != null) {
                                onSignInWithGoogle(activity)
                            }
                        },
                        enabled = !isLoading && activity != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF3C4043),
                            disabledContainerColor = Color(0xFFF1F3F4)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_signin_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = Brand700
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Đang xử lý đăng nhập...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        } else {
                            GoogleGIcon(modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Đăng nhập nhanh bằng Google",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider with "HOẶC" (OR)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE2EBE5)
                        )
                        Text(
                            text = "HOẶC DỰ PHÒNG",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE2EBE5)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Secondary / Explicit Browser Sign-In Button (Web OAuth)
                    OutlinedButton(
                        onClick = {
                            if (activity != null) {
                                onSignInWithBrowser(activity)
                            }
                        },
                        enabled = !isLoading && activity != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Brand700
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Brand700.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("browser_signin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = null,
                            tint = Brand700,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Đăng nhập bằng cách khác (Trình duyệt web)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Brand700
                            )
                            Text(
                                text = "Tự gõ email & mật khẩu trên trang Google",
                                fontSize = 10.5.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notice on unified profile & offline support
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BgSunken,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = Brand700,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dù đăng nhập bằng cách nào, toàn bộ hồ sơ và dấu mộc hành trình của bạn đều được đồng bộ trọn vẹn vào một tài khoản duy nhất.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Feature Highlights
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FeatureBadge(title = "34 Tỉnh Thành", subtitle = "Dấu hộ chiếu thật")
                FeatureBadge(title = "Lịch Trình AI", subtitle = "Tối ưu chi phí")
                FeatureBadge(title = "Bản Đồ Offline", subtitle = "Không lo mất sóng")
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Security & Privacy Guarantee Note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Bảo mật an toàn qua Google OAuth & Firebase Auth",
                    fontSize = 11.5.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun FeatureBadge(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = Brand100,
            modifier = Modifier.size(32.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Brand700,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun GoogleGIcon(modifier: Modifier = Modifier) {
    // Beautiful pure Compose rendering of Google "G" logo
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val blue = Color(0xFF4285F4)
        val red = Color(0xFFEA4335)
        val yellow = Color(0xFFFBBC05)
        val green = Color(0xFF34A853)

        val strokeWidth = w * 0.22f

        // Blue right arc & bar
        drawArc(
            color = blue,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(w - strokeWidth, h - strokeWidth)
        )
        // Green bottom arc
        drawArc(
            color = green,
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(w - strokeWidth, h - strokeWidth)
        )
        // Yellow left arc
        drawArc(
            color = yellow,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(w - strokeWidth, h - strokeWidth)
        )
        // Red top arc
        drawArc(
            color = red,
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(w - strokeWidth, h - strokeWidth)
        )

        // Middle horizontal blue bar
        drawRect(
            color = blue,
            topLeft = androidx.compose.ui.geometry.Offset(w / 2f, h / 2f - strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(w / 2f, strokeWidth)
        )
    }
}
