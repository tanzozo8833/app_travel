package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.*

enum class AuthTab {
    GOOGLE_QUICK,
    EMAIL_PASSWORD
}

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    isNetworkError: Boolean,
    canFallbackToBrowser: Boolean = false,
    onSignInWithGoogle: (Activity) -> Unit,
    onSignInWithBrowser: (Activity) -> Unit,
    onSignInWithEmail: (String, String) -> Unit,
    onSignUpWithEmail: (String, String, String) -> Unit,
    onSignInAsGuest: (String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current

    var selectedTab by remember { mutableStateOf(AuthTab.GOOGLE_QUICK) }
    var isSignUpMode by remember { mutableStateOf(false) }

    // Form inputs for Email / Password
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

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
                contentDescription = stringResource(R.string.auth_hero_desc),
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // App Brand Logo Badge
            Surface(
                shape = CircleShape,
                color = Brand700,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(72.dp)
                    .border(3.dp, Gold500, CircleShape)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = stringResource(R.string.auth_logo_desc),
                        tint = Gold500,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App Name & Tagline
            Text(
                text = stringResource(R.string.auth_app_name),
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                color = Color.White
            )

            Text(
                text = stringResource(R.string.auth_tagline),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Segmented Control: Tab Selection
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BgSunken,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            // Tab 1: Google & Trải Nghiệm
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = if (selectedTab == AuthTab.GOOGLE_QUICK) Brand700 else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedTab = AuthTab.GOOGLE_QUICK
                                        onClearError()
                                    }
                            ) {
                                Text(
                                    text = stringResource(R.string.auth_tab_google_quick),
                                    fontSize = 12.5.sp,
                                    fontWeight = if (selectedTab == AuthTab.GOOGLE_QUICK) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == AuthTab.GOOGLE_QUICK) Color.White else TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            // Tab 2: Email & Mật Khẩu
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = if (selectedTab == AuthTab.EMAIL_PASSWORD) Brand700 else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedTab = AuthTab.EMAIL_PASSWORD
                                        onClearError()
                                    }
                            ) {
                                Text(
                                    text = stringResource(R.string.auth_tab_email_password),
                                    fontSize = 12.5.sp,
                                    fontWeight = if (selectedTab == AuthTab.EMAIL_PASSWORD) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == AuthTab.EMAIL_PASSWORD) Color.White else TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

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
                                .padding(bottom = 16.dp)
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
                                            .size(20.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isNetworkError) stringResource(R.string.auth_network_error_title) else stringResource(R.string.auth_error_title),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNetworkError) Color(0xFF856404) else DangerRed
                                        )
                                        Text(
                                            text = errorMessage ?: "",
                                            fontSize = 11.5.sp,
                                            color = TextPrimary,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = onClearError,
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(R.string.common_close),
                                            tint = Color.Gray,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

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
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.padding(end = 6.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.OpenInBrowser,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = Brand700
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                stringResource(R.string.auth_open_browser),
                                                fontSize = 11.sp,
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
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.common_retry), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // TAB CONTENT
                    if (selectedTab == AuthTab.GOOGLE_QUICK) {
                        // 1. Primary Google Sign-In Button (Credential Manager / One-Tap)
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
                                .height(50.dp)
                                .testTag("google_signin_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Brand700
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.common_processing),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            } else {
                                GoogleGIcon(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.auth_google_signin_btn),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1F1F1F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. Browser Google OAuth Button
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
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Brand700.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("browser_signin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = Brand700,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.auth_browser_signin_btn),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Brand700
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Divider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2EBE5))
                            Text(
                                text = stringResource(R.string.auth_or_divider),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2EBE5))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Fast Guest / Instant Access Button (Zero friction, no Google account needed)
                        val guestDefaultName = stringResource(R.string.auth_guest_default_name)
                        FilledTonalButton(
                            onClick = {
                                onSignInAsGuest(guestDefaultName)
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Brand100,
                                contentColor = Brand700
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("guest_signin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FlightTakeoff,
                                contentDescription = null,
                                tint = Brand700,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.auth_guest_signin_btn),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Brand700
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.auth_guest_caption),
                            fontSize = 10.5.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // TAB: EMAIL / PASSWORD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            FilterChip(
                                selected = !isSignUpMode,
                                onClick = {
                                    isSignUpMode = false
                                    onClearError()
                                },
                                label = { Text(stringResource(R.string.auth_chip_login), fontSize = 12.sp) },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            FilterChip(
                                selected = isSignUpMode,
                                onClick = {
                                    isSignUpMode = true
                                    onClearError()
                                },
                                label = { Text(stringResource(R.string.auth_chip_signup), fontSize = 12.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = displayNameInput,
                                onValueChange = { displayNameInput = it },
                                label = { Text(stringResource(R.string.auth_field_fullname)) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Brand700)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text(stringResource(R.string.auth_field_email)) },
                            placeholder = { Text(stringResource(R.string.auth_field_email_placeholder)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Email, contentDescription = null, tint = Brand700)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(stringResource(R.string.auth_field_password)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = Brand700)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (isPasswordVisible) stringResource(R.string.auth_password_hide) else stringResource(R.string.auth_password_show)
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (isSignUpMode) {
                                        onSignUpWithEmail(emailInput, passwordInput, displayNameInput)
                                    } else {
                                        onSignInWithEmail(emailInput, passwordInput)
                                    }
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (isSignUpMode) {
                                    onSignUpWithEmail(emailInput, passwordInput, displayNameInput)
                                } else {
                                    onSignInWithEmail(emailInput, passwordInput)
                                }
                            },
                            enabled = !isLoading && emailInput.isNotBlank() && passwordInput.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Brand700,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(stringResource(R.string.common_processing), fontSize = 13.sp)
                            } else {
                                Text(
                                    text = if (isSignUpMode) stringResource(R.string.auth_action_create_account) else stringResource(R.string.auth_action_signin),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                text = stringResource(R.string.auth_security_note),
                                fontSize = 10.5.sp,
                                color = TextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Feature Highlights
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FeatureBadge(title = stringResource(R.string.auth_badge_provinces_title), subtitle = stringResource(R.string.auth_badge_provinces_sub))
                FeatureBadge(title = stringResource(R.string.auth_badge_ai_title), subtitle = stringResource(R.string.auth_badge_ai_sub))
                FeatureBadge(title = stringResource(R.string.auth_badge_offline_title), subtitle = stringResource(R.string.auth_badge_offline_sub))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Security & Privacy Guarantee Note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.auth_footer_security),
                    fontSize = 11.sp,
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
