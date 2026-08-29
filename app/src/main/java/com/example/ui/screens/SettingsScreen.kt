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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
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
                            text = stringResource(R.string.settings_category_header),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Brand700,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = stringResource(R.string.settings_title),
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
                            contentDescription = stringResource(R.string.common_back),
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
                                Text(text = stringResource(R.string.common_edit), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                                            text = stringResource(R.string.settings_guest_badge_title),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_guest_badge_desc),
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
                                        Text(stringResource(R.string.settings_guest_link_google_btn), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Giao diện & Chế độ sáng/tối (Dark / Light Mode)
            item {
                SettingsSectionContainer(title = stringResource(R.string.settings_section_display)) {
                    SettingsSwitchRow(
                        icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Outlined.LightMode,
                        title = stringResource(R.string.settings_dark_mode_title),
                        subtitle = if (isDarkMode) stringResource(R.string.settings_dark_mode_subtitle_on) else stringResource(R.string.settings_dark_mode_subtitle_off),
                        isChecked = isDarkMode,
                        onCheckedChange = onToggleDarkMode
                    )
                }
            }

            // 4. Ngôn ngữ & Khu vực (Language & Currency)
            item {
                SettingsSectionContainer(title = stringResource(R.string.settings_section_language)) {
                    SettingsClickableRow(
                        icon = Icons.Outlined.Language,
                        title = stringResource(R.string.settings_language_title),
                        value = if (appLanguage == "vi") stringResource(R.string.settings_language_vietnamese) else stringResource(R.string.settings_language_english),
                        onClick = {
                            onSetLanguage(if (appLanguage == "vi") "en" else "vi")
                        }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsClickableRow(
                        icon = Icons.Outlined.Paid,
                        title = stringResource(R.string.settings_currency_title),
                        value = selectedCurrency,
                        onClick = { showCurrencyDialog = true }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsClickableRow(
                        icon = Icons.Outlined.Straighten,
                        title = stringResource(R.string.settings_distance_unit_title),
                        value = stringResource(R.string.settings_distance_unit_value),
                        onClick = {}
                    )
                }
            }

            // 5. Thông báo & Lời nhắc
            item {
                SettingsSectionContainer(title = stringResource(R.string.settings_section_notifications)) {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.NotificationsActive,
                        title = stringResource(R.string.settings_notify_trip_title),
                        subtitle = stringResource(R.string.settings_notify_trip_sub),
                        isChecked = notifyTripAlarm,
                        onCheckedChange = { notifyTripAlarm = it }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Place,
                        title = stringResource(R.string.settings_notify_nearby_title),
                        subtitle = stringResource(R.string.settings_notify_nearby_sub),
                        isChecked = notifyNearbyPlaces,
                        onCheckedChange = { notifyNearbyPlaces = it }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsSwitchRow(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = stringResource(R.string.settings_notify_social_title),
                        subtitle = stringResource(R.string.settings_notify_social_sub),
                        isChecked = notifySocialInteractions,
                        onCheckedChange = { notifySocialInteractions = it }
                    )
                }
            }

            // 6. Bản đồ & Quyền riêng tư
            item {
                SettingsSectionContainer(title = stringResource(R.string.settings_section_privacy)) {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Map,
                        title = stringResource(R.string.settings_privacy_public_checkins_title),
                        subtitle = stringResource(R.string.settings_privacy_public_checkins_sub),
                        isChecked = publicCheckIns,
                        onCheckedChange = { publicCheckIns = it }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsSwitchRow(
                        icon = Icons.Outlined.CardMembership,
                        title = stringResource(R.string.settings_privacy_cv_share_title),
                        subtitle = stringResource(R.string.settings_privacy_cv_share_sub),
                        isChecked = allowCvSharing,
                        onCheckedChange = { allowCvSharing = it }
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsSwitchRow(
                        icon = Icons.Outlined.DownloadForOffline,
                        title = stringResource(R.string.settings_privacy_offline_map_title),
                        subtitle = stringResource(R.string.settings_privacy_offline_map_sub),
                        isChecked = offlineMapsEnabled,
                        onCheckedChange = { offlineMapsEnabled = it }
                    )
                }
            }

            // 7. Bộ nhớ & Quản lý dữ liệu
            item {
                SettingsSectionContainer(title = stringResource(R.string.settings_section_storage)) {
                    SettingsClickableRow(
                        icon = Icons.Outlined.CleaningServices,
                        title = stringResource(R.string.settings_clear_cache_title),
                        value = "48.6 MB",
                        onClick = onClearCache
                    )
                    HorizontalDivider(color = Color(0xFFEFF4F1), modifier = Modifier.padding(horizontal = 14.dp))
                    SettingsClickableRow(
                        icon = Icons.Outlined.CloudUpload,
                        title = stringResource(R.string.settings_cloud_sync_title),
                        value = stringResource(R.string.settings_cloud_sync_synced),
                        onClick = { onClearCache() }
                    )
                }
            }

            // 8. Đăng xuất & Quản trị tài khoản
            item {
                SettingsSectionContainer(title = stringResource(R.string.settings_section_account)) {
                    SettingsClickableRow(
                        icon = Icons.Outlined.Lock,
                        title = stringResource(R.string.settings_change_password_title),
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
                            text = stringResource(R.string.settings_logout_title),
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
                            text = stringResource(R.string.settings_delete_account_title),
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
                        text = stringResource(R.string.settings_footer_version),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = stringResource(R.string.settings_footer_tagline),
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
                    text = stringResource(R.string.settings_edit_profile_dialog_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(stringResource(R.string.settings_field_display_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text(stringResource(R.string.settings_field_travel_title)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text(stringResource(R.string.settings_field_bio)) },
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
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
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
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.settings_logout_guest_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = DangerRed
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.settings_logout_guest_desc),
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
                                text = stringResource(R.string.settings_logout_guest_tip),
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
                        Text(stringResource(R.string.settings_guest_link_google_btn), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { showLogoutConfirmDialog = false }) {
                            Text(stringResource(R.string.settings_stay_btn), color = TextPrimary)
                        }
                        TextButton(
                            onClick = {
                                showLogoutConfirmDialog = false
                                onLogout()
                            }
                        ) {
                            Text(stringResource(R.string.settings_still_logout_btn), color = DangerRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmDialog = false },
                title = { Text(stringResource(R.string.settings_logout_normal_title), fontWeight = FontWeight.Bold) },
                text = {
                    Text(stringResource(R.string.settings_logout_normal_desc))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirmDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text(stringResource(R.string.settings_logout_title), color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirmDialog = false }) {
                        Text(stringResource(R.string.settings_stay_btn))
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
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.settings_delete_account_dialog_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DangerRed
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.settings_delete_account_dialog_desc),
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
                                text = stringResource(R.string.settings_delete_account_loss_summary),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                            Text(stringResource(R.string.settings_delete_account_loss_1), fontSize = 11.5.sp, color = TextPrimary)
                            Text(stringResource(R.string.settings_delete_account_loss_2), fontSize = 11.5.sp, color = TextPrimary)
                            Text(stringResource(R.string.settings_delete_account_loss_3), fontSize = 11.5.sp, color = TextPrimary)
                            Text(stringResource(R.string.settings_delete_account_loss_4), fontSize = 11.5.sp, color = TextPrimary)
                            Text(stringResource(R.string.settings_delete_account_loss_5), fontSize = 11.5.sp, color = TextPrimary)
                        }
                    }

                    Text(
                        text = stringResource(R.string.settings_delete_account_reauth_note),
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
                    Text(stringResource(R.string.settings_delete_account_confirm_btn), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteAccountConfirmDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.settings_delete_account_cancel_btn), color = TextPrimary, fontSize = 13.sp)
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
                    text = stringResource(R.string.settings_deleting_progress_title),
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
                        text = stringResource(R.string.settings_deleting_progress_desc),
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
        val currVnd = stringResource(R.string.settings_currency_vnd)
        val currUsd = stringResource(R.string.settings_currency_usd)
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text(stringResource(R.string.settings_select_currency_title), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(currVnd, currUsd).forEach { curr ->
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
