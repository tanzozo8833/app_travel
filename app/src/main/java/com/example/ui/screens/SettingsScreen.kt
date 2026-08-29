package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    isDarkMode: Boolean,
    appLanguage: String,
    isAnonymous: Boolean = false,
    isDeletingAccount: Boolean = false,
    onBack: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSetLanguage: (String) -> Unit,
    onUpdateProfile: (displayName: String, bio: String, tagTitle: String) -> Unit,
    onClearCache: () -> Unit,
    onLogout: () -> Unit,
    onUpgradeAccount: () -> Unit = {},
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("VNĐ (₫)") }

    var notifyTripAlarm by remember { mutableStateOf(true) }
    var notifyNearbyPlaces by remember { mutableStateOf(true) }
    var notifySocialInteractions by remember { mutableStateOf(true) }

    var publicCheckIns by remember { mutableStateOf(true) }
    var allowCvSharing by remember { mutableStateOf(true) }
    var offlineMapsEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CÀI ĐẶT & TÀI KHOẢN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Tùy Chọn Ứng Dụng",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCanvas)
            )
        },
        containerColor = BgCanvas,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Current Account Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2EBE5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                AsyncImage(
                                    model = userProfile.avatarUrl,
                                    contentDescription = userProfile.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Gold500, CircleShape)
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = Brand700,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.padding(3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userProfile.displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "@${userProfile.username}",
                                    fontSize = 12.5.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Gold500
                                ) {
                                    Text(
                                        text = userProfile.tagTitle,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextOnGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showEditProfileDialog = true },
                                shape = RoundedCornerShape(999.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Sửa", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (userProfile.bio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = userProfile.bio,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }

                        if (isAnonymous) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Gold500.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Gold500.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.WarningAmber,
                                            contentDescription = null,
                                            tint = Gold500,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Tài khoản Lữ Khách (Chưa liên kết)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Hành trình và con dấu sẽ mất nếu bạn đăng xuất. Hãy liên kết với Google để bảo vệ dữ liệu!",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = onUpgradeAccount,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Liên kết Google ngay (Lưu dữ liệu)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Giao diện & Chế độ sáng/tối (Dark / Light Mode)
            item {
                SettingsSectionContainer(title = "GIAO DIỆN & HIỂN THỊ") {
                    SettingsSwitchRow(
                        icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Outlined.LightMode,
                        title = "Chế độ nền tối (Dark Mode)",
                        subtitle = if (isDarkMode) "Đang bật giao diện ban đêm" else "Đang dùng giao diện ban ngày",
                        isChecked = isDarkMode,
                        onCheckedChange = onToggleDarkMode
                    )
                }
            }

            // 4. Ngôn ngữ & Khu vực (Language & Currency)
            item {
                SettingsSectionContainer(title = "NGÔN NGỮ & KHU VỰC") {
                    SettingsClickableRow(
                        icon = Icons.Outlined.Language,
                        title = "Ngôn ngữ ứng dụng",
                        value = if (appLanguage == "vi") "Tiếng Việt (VN) 🇻🇳" else "English (US) 🇬🇧",
                        onClick = {
                            onSetLanguage(if (appLanguage == "vi") "en" else "vi")
                        }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsClickableRow(
                        icon = Icons.Outlined.Paid,
                        title = "Đơn vị tiền tệ dự toán",
                        value = selectedCurrency,
                        onClick = { showCurrencyDialog = true }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsClickableRow(
                        icon = Icons.Outlined.Straighten,
                        title = "Đơn vị khoảng cách",
                        value = "Kilômét (km)",
                        onClick = {}
                    )
                }
            }

            // 5. Thông báo & Lời nhắc
            item {
                SettingsSectionContainer(title = "THÔNG BÁO & NHẮC NHỞ") {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.NotificationsActive,
                        title = "Nhắc nhở lịch trình chuyến đi",
                        subtitle = "Báo trước 1 ngày & 2 giờ khởi hành",
                        isChecked = notifyTripAlarm,
                        onCheckedChange = { notifyTripAlarm = it }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Place,
                        title = "Gợi ý điểm check-in gần bạn",
                        subtitle = "Nhận diện geofence khi đến tỉnh thành mới",
                        isChecked = notifyNearbyPlaces,
                        onCheckedChange = { notifyNearbyPlaces = it }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsSwitchRow(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = "Tương tác cộng đồng",
                        subtitle = "Thông báo khi có người thả tim hoặc bình luận",
                        isChecked = notifySocialInteractions,
                        onCheckedChange = { notifySocialInteractions = it }
                    )
                }
            }

            // 6. Bản đồ & Quyền riêng tư
            item {
                SettingsSectionContainer(title = "BẢN ĐỒ & QUYỀN RIÊNG TƯ") {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Map,
                        title = "Hiển thị vị trí check-in công khai",
                        subtitle = "Cho phép bạn bè xem dấu hộ chiếu của bạn",
                        isChecked = publicCheckIns,
                        onCheckedChange = { publicCheckIns = it }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsSwitchRow(
                        icon = Icons.Outlined.CardMembership,
                        title = "Chia sẻ Travel CV",
                        subtitle = "Tạo liên kết chia sẻ hồ sơ lữ hành",
                        isChecked = allowCvSharing,
                        onCheckedChange = { allowCvSharing = it }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsSwitchRow(
                        icon = Icons.Outlined.DownloadForOffline,
                        title = "Tải bản đồ ngoại tuyến (Offline Map)",
                        subtitle = "Lưu bản đồ 34 tỉnh thành để xem khi mất sóng",
                        isChecked = offlineMapsEnabled,
                        onCheckedChange = { offlineMapsEnabled = it }
                    )
                }
            }

            // 7. Bộ nhớ & Quản lý dữ liệu
            item {
                SettingsSectionContainer(title = "DỮ LIỆU & BỘ NHỚ") {
                    SettingsClickableRow(
                        icon = Icons.Outlined.CleaningServices,
                        title = "Dọn dẹp bộ nhớ đệm hình ảnh",
                        value = "48.6 MB",
                        onClick = onClearCache
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsClickableRow(
                        icon = Icons.Outlined.CloudUpload,
                        title = "Sao lưu lịch trình lên đám mây",
                        value = "Đã đồng bộ",
                        onClick = { onClearCache() }
                    )
                }
            }

            // 8. Đăng xuất & Quản trị tài khoản
            item {
                SettingsSectionContainer(title = "QUẢN TRỊ TÀI KHOẢN") {
                    SettingsClickableRow(
                        icon = Icons.Outlined.Lock,
                        title = "Đổi mật khẩu & Bảo mật",
                        value = "",
                        onClick = {}
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutConfirmDialog = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Logout,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Đăng xuất tài khoản",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DangerRed
                        )
                    }
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteAccountConfirmDialog = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.DeleteForever,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Xoá tài khoản vĩnh viễn",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // 9. App version info footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LUMINA TRAVEL • PHIÊN BẢN 2.5.0",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Thiết kế cho thế hệ Lữ hành Việt Nam 🇻🇳",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    // Dialog 1: Sửa hồ sơ cá nhân
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userProfile.displayName) }
        var editBio by remember { mutableStateOf(userProfile.bio) }
        var editTitle by remember { mutableStateOf(userProfile.tagTitle) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Chỉnh Sửa Hồ Sơ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Tên hiển thị") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Danh hiệu lữ hành") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Tiểu sử (Bio)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(editName, editBio, editTitle)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand700)
                ) {
                    Text("Lưu thay đổi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Huỷ")
                }
            }
        )
    }

    // Dialog 2: Xác nhận đăng xuất
    if (showLogoutConfirmDialog) {
        if (isAnonymous) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmDialog = false },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = DangerRed.copy(alpha = 0.12f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = "Cảnh báo",
                                tint = DangerRed,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Mất Dữ Liệu Khách Vĩnh Viễn?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = DangerRed
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tài khoản khách không có cách nào đăng nhập lại. Nếu bạn bấm Đăng xuất, bạn sẽ MẤT VĨNH VIỄN mọi thứ đã làm (bài viết, con dấu 34 tỉnh thành, lịch trình).",
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Brand100,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text(
                                text = "💡 Lời khuyên: Hãy liên kết với tài khoản Google để lưu giữ vĩnh viễn dữ liệu của bạn!",
                                fontSize = 12.sp,
                                color = Brand700,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirmDialog = false
                            onUpgradeAccount()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Liên kết Google (Lưu dữ liệu)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { showLogoutConfirmDialog = false }) {
                            Text("Ở lại", color = TextPrimary)
                        }
                        TextButton(
                            onClick = {
                                showLogoutConfirmDialog = false
                                onLogout()
                            }
                        ) {
                            Text("Vẫn đăng xuất", color = DangerRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmDialog = false },
                title = { Text("Đăng xuất khỏi Lumina?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Dữ liệu check-in và lịch trình của bạn đã được sao lưu an toàn. Bạn có thể đăng nhập lại bất kỳ lúc nào.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirmDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Đăng xuất", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirmDialog = false }) {
                        Text("Ở lại")
                    }
                }
            )
        }
    }

    // Dialog 4: Xác nhận xoá tài khoản vĩnh viễn
    if (showDeleteAccountConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeletingAccount) showDeleteAccountConfirmDialog = false
            },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = DangerRed.copy(alpha = 0.12f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.WarningAmber,
                            contentDescription = "Cảnh báo xoá tài khoản",
                            tint = DangerRed,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Xoá Vĩnh Viễn Tài Khoản?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DangerRed
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Hành động này sẽ xoá hoàn toàn tài khoản của bạn khỏi hệ thống xác thực và không thể hoàn tác.",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DangerRed.copy(alpha = 0.06f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Dữ liệu cá nhân của bạn sẽ bị xoá sạch:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                            Text("• Toàn bộ hồ sơ cá nhân và phiên đăng nhập", fontSize = 11.5.sp, color = TextPrimary)
                            Text("• Tất cả bài viết chia sẻ và bình luận", fontSize = 11.5.sp, color = TextPrimary)
                            Text("• Toàn bộ con dấu hộ chiếu 34 tỉnh thành", fontSize = 11.5.sp, color = TextPrimary)
                            Text("• Tất cả lịch trình chuyến đi đã tạo", fontSize = 11.5.sp, color = TextPrimary)
                            Text("• Quan hệ theo dõi, thông báo & bộ sưu tập đã lưu", fontSize = 11.5.sp, color = TextPrimary)
                        }
                    }

                    Text(
                        text = "Lưu ý: Nếu phiên đăng nhập đã cũ, bạn có thể được yêu cầu đăng nhập lại một lần để xác thực quyền sở hữu trước khi xoá.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountConfirmDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tôi hiểu & Xoá vĩnh viễn", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteAccountConfirmDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Huỷ & Giữ tài khoản", color = TextPrimary, fontSize = 13.sp)
                }
            }
        )
    }

    // Dialog: Loading khi đang thực hiện xoá tài khoản
    if (isDeletingAccount) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Đang Xử Lý Xoá Tài Khoản",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        color = DangerRed,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = "Đang xoá tài khoản trên hệ thống và dọn dẹp toàn bộ dữ liệu cá nhân trong máy...",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Dialog 5: Chọn đơn vị tiền tệ
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Chọn đơn vị tiền tệ", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("VNĐ (₫) - Đồng Việt Nam", "USD ($) - Đô la Mỹ").forEach { curr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCurrency = curr.substringBefore(" - ")
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCurrency == curr.substringBefore(" - "),
                                onClick = {
                                    selectedCurrency = curr.substringBefore(" - ")
                                    showCurrencyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = curr, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun SettingsSectionContainer(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Brand700,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4EDE7)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Brand700,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Brand700
            )
        )
    }
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    value: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Brand700,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotBlank()) {
            Text(
                text = value,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Brand700
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
