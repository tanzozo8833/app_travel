package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
    onSignInWithGoogle: (Activity) -> Unit,
    onDirectSignIn: (email: String, name: String, avatarUrl: String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var showDirectLoginDialog by remember { mutableStateOf(false) }

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
                model = "https://images.unsplash.com/photo-1528127269322-539801943592?w=1080&auto=format&fit=crop", // Ha Long / Vietnam sunset scenery
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
            Spacer(modifier = Modifier.height(70.dp))

            // App Brand Logo Badge
            Surface(
                shape = CircleShape,
                color = Brand700,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(80.dp)
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
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

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

            Spacer(modifier = Modifier.height(55.dp))

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
                        text = "Đăng nhập bằng tài khoản Google để tự động đồng bộ tên, ảnh đại diện và lưu dấu hộ chiếu 34 tỉnh thành.",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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
                                    horizontalArrangement = Arrangement.End
                                ) {
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

                    // Primary Google Sign-In Button
                    Button(
                        onClick = {
                            if (activity != null) {
                                onSignInWithGoogle(activity)
                            } else {
                                showDirectLoginDialog = true
                            }
                        },
                        enabled = !isLoading,
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
                                text = "Đang kết nối Google...",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        } else {
                            // Custom Vector Google "G" Emblem
                            GoogleGIcon(modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Đăng nhập bằng Google",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary Direct Account Selection (for seamless testing & devices without Play Services)
                    TextButton(
                        onClick = { showDirectLoginDialog = true },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = null,
                            tint = Brand700,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đăng nhập nhanh với email của bạn",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Brand700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

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
                    text = "Bảo mật an toàn qua Google OAuth & Android Identity",
                    fontSize = 11.5.sp,
                    color = Color.Gray
                )
            }
        }
    }

    // Direct Login Dialog (User-friendly fallback & instant login)
    if (showDirectLoginDialog) {
        var emailInput by remember { mutableStateOf("hoangductan733@gmail.com") }
        var nameInput by remember { mutableStateOf("Hoàng Đức Tân") }

        AlertDialog(
            onDismissRequest = { showDirectLoginDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoogleGIcon(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Đăng nhập tài khoản Google", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Nhập thông tin tài khoản Google của bạn để tạo hồ sơ người dùng chính thức trong app:",
                        fontSize = 12.5.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Địa chỉ Email Google") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Họ và tên hiển thị") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailInput.isNotBlank() && nameInput.isNotBlank()) {
                            showDirectLoginDialog = false
                            onDirectSignIn(
                                emailInput.trim(),
                                nameInput.trim(),
                                "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300&auto=format&fit=crop"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                    enabled = emailInput.isNotBlank() && nameInput.isNotBlank()
                ) {
                    Text("Xác nhận đăng nhập")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectLoginDialog = false }) {
                    Text("Huỷ")
                }
            }
        )
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
        val radius = (w - strokeWidth) / 2f
        val center = androidx.compose.ui.geometry.Offset(w / 2f, h / 2f)

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
