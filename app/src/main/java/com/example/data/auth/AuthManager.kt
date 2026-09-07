package com.example.data.auth

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.data.model.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

sealed class AuthResult {
    data class Success(val userProfile: UserProfile, val email: String, val isUpgrade: Boolean = false) : AuthResult()
    data object Cancelled : AuthResult()
    data class AccountCollision(
        val email: String,
        val credential: AuthCredential? = null
    ) : AuthResult()
    data class Error(
        val message: String,
        val isNetworkError: Boolean = false,
        val isNoCredential: Boolean = false,
        val canFallbackToBrowser: Boolean = false
    ) : AuthResult()
}

sealed class DeleteAccountResult {
    data object Success : DeleteAccountResult()
    data object Cancelled : DeleteAccountResult()
    data class Error(val message: String) : DeleteAccountResult()
}

class AuthManager(private val context: Context) {
    // KHONG doi ten "lumina_auth_prefs": day la ten tep luu phien dang nhap tren may.
    // Doi ten se khien moi nguoi dung dang bi dang xuat va mat lua chon da luu.
    private val prefs: SharedPreferences = context.getSharedPreferences("lumina_auth_prefs", Context.MODE_PRIVATE)
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isAnonymous = MutableStateFlow(firebaseAuth.currentUser?.isAnonymous == true)
    val isAnonymous: StateFlow<Boolean> = _isAnonymous.asStateFlow()

    private val _currentAuthUser = MutableStateFlow(buildProfileFromFirebaseUser(firebaseAuth.currentUser) ?: getSavedUser())
    val currentAuthUser: StateFlow<UserProfile?> = _currentAuthUser.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            val isUserLoggedIn = user != null
            _isLoggedIn.value = isUserLoggedIn
            _isAnonymous.value = user?.isAnonymous == true
            if (user != null) {
                val profile = buildProfileFromFirebaseUser(user)
                _currentAuthUser.value = profile
            } else {
                _currentAuthUser.value = null
            }
        }
    }

    fun getFirebaseUser(): FirebaseUser? = firebaseAuth.currentUser

    companion object {
        private const val TAG = "AuthManager"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_EMAIL = "auth_user_email"
        private const val KEY_DISPLAY_NAME = "auth_user_display_name"
        private const val KEY_USERNAME = "auth_user_username"
        private const val KEY_AVATAR_URL = "auth_user_avatar_url"
        private const val KEY_TAG_TITLE = "auth_user_tag_title"
        private const val KEY_BIO = "auth_user_bio"
        private const val KEY_IS_CUSTOM_NAME = "auth_user_is_custom_name"
        private const val KEY_IS_CUSTOM_AVATAR = "auth_user_is_custom_avatar"
    }

    /**
     * Reads default_web_client_id generated automatically by Google Services gradle plugin
     * from google-services.json, with a guaranteed valid fallback.
     */
    fun getWebClientId(): String {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val clientId = context.getString(resId)
                if (clientId.isNotBlank()) clientId else "50591347475-kbuer0hv544befn0276jp4h8n0u6ovui.apps.googleusercontent.com"
            } else {
                "50591347475-kbuer0hv544befn0276jp4h8n0u6ovui.apps.googleusercontent.com"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting default_web_client_id resource, using default fallback", e)
            "50591347475-kbuer0hv544befn0276jp4h8n0u6ovui.apps.googleusercontent.com"
        }
    }

    fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val network = connectivityManager.activeNetwork ?: return true
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return true
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true
        }
    }

    private fun buildProfileFromFirebaseUser(user: FirebaseUser?): UserProfile? {
        if (user == null) return null
        val isAnon = user.isAnonymous
        val email = user.email ?: ""
        val isCustomName = prefs.getBoolean(KEY_IS_CUSTOM_NAME, false)
        val isCustomAvatar = prefs.getBoolean(KEY_IS_CUSTOM_AVATAR, false)
        val savedDisplayName = prefs.getString(KEY_DISPLAY_NAME, null)
        val savedAvatarUrl = prefs.getString(KEY_AVATAR_URL, null)

        val displayName = if (isCustomName && !savedDisplayName.isNullOrBlank()) {
            savedDisplayName
        } else {
            user.displayName?.ifBlank { null }
                ?: savedDisplayName
                ?: if (isAnon) "Lữ khách Khám Phá" else email.substringBefore("@").ifBlank { "Lữ khách Levart" }
        }

        val username = if (isAnon) {
            prefs.getString(KEY_USERNAME, null) ?: "traveler_${user.uid.takeLast(6).lowercase()}"
        } else {
            email.substringBefore("@").replace(".", "_").lowercase().ifBlank { "traveler" }
        }

        val avatarUrl = if (isCustomAvatar && !savedAvatarUrl.isNullOrBlank()) {
            savedAvatarUrl
        } else {
            user.photoUrl?.toString() ?: savedAvatarUrl ?: ""
        }

        val savedBio = prefs.getString(KEY_BIO, null)
        val savedTag = prefs.getString(KEY_TAG_TITLE, null)

        return UserProfile(
            id = user.uid,
            username = username,
            displayName = displayName,
            avatarUrl = avatarUrl.ifBlank {
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
            },
            tagTitle = savedTag ?: "GEN Z EXPLORER",
            bio = savedBio ?: if (isAnon) {
                "Chế độ Lữ Khách • Khám phá 34 tỉnh thành Việt Nam 🇻🇳"
            } else {
                "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳"
            },
            isCustomName = isCustomName,
            isCustomAvatar = isCustomAvatar
        )
    }

    private fun getSavedUser(): UserProfile? {
        if (firebaseAuth.currentUser == null) return null

        val id = prefs.getString(KEY_USER_ID, null) ?: firebaseAuth.currentUser?.uid ?: return null
        val displayName = prefs.getString(KEY_DISPLAY_NAME, "Lữ khách Levart") ?: "Lữ khách Levart"
        val username = prefs.getString(KEY_USERNAME, "traveler") ?: "traveler"
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, "") ?: ""
        val tagTitle = prefs.getString(KEY_TAG_TITLE, "GEN Z EXPLORER") ?: "GEN Z EXPLORER"
        val bio = prefs.getString(KEY_BIO, "Khám phá 34 tỉnh thành Việt Nam cùng Levart ✨") ?: ""
        val isCustomName = prefs.getBoolean(KEY_IS_CUSTOM_NAME, false)
        val isCustomAvatar = prefs.getBoolean(KEY_IS_CUSTOM_AVATAR, false)

        return UserProfile(
            id = id,
            username = username,
            displayName = displayName,
            avatarUrl = avatarUrl.ifBlank {
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
            },
            tagTitle = tagTitle,
            bio = bio,
            isCustomName = isCustomName,
            isCustomAvatar = isCustomAvatar
        )
    }

    /**
     * Primary Sign-In Method: Uses CredentialManager (One-Tap / Google Play Services).
     * If currently in Guest Mode (Anonymous User), it upgrades the account via linkWithCredential.
     * If an existing account collision is detected, returns AuthResult.AccountCollision.
     */
    suspend fun signInWithGoogle(activity: Activity, allowBrowserFallback: Boolean = true): AuthResult = withContext(Dispatchers.IO) {
        // 1. Check network connectivity
        if (!isNetworkAvailable()) {
            return@withContext AuthResult.Error(
                message = "Không có kết nối mạng. Vui lòng kiểm tra Wi-Fi hoặc 4G và thử lại.",
                isNetworkError = true
            )
        }

        // 2. Check default_web_client_id from google-services.json
        val webClientId = getWebClientId()
        if (webClientId.isNullOrBlank()) {
            Log.w(TAG, "Missing default_web_client_id string resource. Attempting browser fallback directly.")
            if (allowBrowserFallback) {
                return@withContext signInWithGoogleBrowserInternal(activity)
            } else {
                return@withContext AuthResult.Error(
                    message = "Ứng dụng chưa được cấu hình Client ID. Vui lòng thử đăng nhập bằng trình duyệt web."
                )
            }
        }

        try {
            // 3. Setup GoogleIdOption / Credential Request with dynamic client ID
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d(TAG, "Requesting Google Sign-In with serverClientId=$webClientId via CredentialManager...")
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)

                val currentUser = firebaseAuth.currentUser
                val isCurrentlyAnonymous = currentUser != null && currentUser.isAnonymous

                if (isCurrentlyAnonymous) {
                    // ACCOUNT UPGRADE: Link existing anonymous guest account with Google
                    Log.d(TAG, "Upgrading anonymous guest account (UID: ${currentUser!!.uid}) via linkWithCredential...")
                    try {
                        val linkResult = currentUser.linkWithCredential(authCredential).await()
                        val upgradedUser = linkResult.user ?: currentUser

                        val email = upgradedUser.email ?: googleIdToken.id
                        val displayName = upgradedUser.displayName?.ifBlank { null }
                            ?: googleIdToken.displayName
                            ?: googleIdToken.givenName
                            ?: email.substringBefore("@")
                        val avatarUrl = upgradedUser.photoUrl?.toString()
                            ?: googleIdToken.profilePictureUri?.toString()
                            ?: ""
                        val username = email.substringBefore("@").replace(".", "_").lowercase()

                        val isCustomName = prefs.getBoolean(KEY_IS_CUSTOM_NAME, false)
                        val isCustomAvatar = prefs.getBoolean(KEY_IS_CUSTOM_AVATAR, false)
                        val savedDisplayName = prefs.getString(KEY_DISPLAY_NAME, null)
                        val savedAvatarUrl = prefs.getString(KEY_AVATAR_URL, null)

                        val resolvedDisplayName = if (isCustomName && !savedDisplayName.isNullOrBlank()) {
                            savedDisplayName
                        } else {
                            displayName
                        }
                        val resolvedAvatarUrl = if (isCustomAvatar && !savedAvatarUrl.isNullOrBlank()) {
                            savedAvatarUrl
                        } else {
                            avatarUrl
                        }

                        val profile = UserProfile(
                            id = upgradedUser.uid,
                            username = username,
                            displayName = resolvedDisplayName,
                            avatarUrl = resolvedAvatarUrl.ifBlank {
                                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
                            },
                            tagTitle = prefs.getString(KEY_TAG_TITLE, "GEN Z EXPLORER") ?: "GEN Z EXPLORER",
                            bio = prefs.getString(KEY_BIO, "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳") ?: "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳",
                            isCustomName = isCustomName,
                            isCustomAvatar = isCustomAvatar
                        )

                        saveSession(profile, email)
                        Log.d(TAG, "Account successfully upgraded from Guest to Google: ${profile.displayName}")
                        return@withContext AuthResult.Success(profile, email, isUpgrade = true)
                    } catch (e: Exception) {
                        val isCollision = e is FirebaseAuthUserCollisionException ||
                                (e is FirebaseAuthException && (e.errorCode == "ERROR_CREDENTIAL_ALREADY_IN_USE" || e.errorCode == "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL")) ||
                                e.message?.contains("already in use", ignoreCase = true) == true ||
                                e.message?.contains("collision", ignoreCase = true) == true

                        if (isCollision) {
                            val collisionEmail = (e as? FirebaseAuthUserCollisionException)?.email
                                ?: googleIdToken.id.takeIf { it.contains("@") }
                                ?: "tài khoản Google này"
                            Log.w(TAG, "Collision detected when upgrading guest: account $collisionEmail already exists.")
                            return@withContext AuthResult.AccountCollision(
                                email = collisionEmail,
                                credential = authCredential
                            )
                        }
                        throw e
                    }
                } else {
                    // Standard Google Sign-In
                    val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                    val firebaseUser = authResult.user
                        ?: throw IllegalStateException("Không nhận được dữ liệu xác thực người dùng từ Firebase Auth.")

                    val email = firebaseUser.email ?: googleIdToken.id
                    val displayName = firebaseUser.displayName?.ifBlank { null }
                        ?: googleIdToken.displayName
                        ?: googleIdToken.givenName
                        ?: email.substringBefore("@")
                    val avatarUrl = firebaseUser.photoUrl?.toString()
                        ?: googleIdToken.profilePictureUri?.toString()
                        ?: ""
                    val username = email.substringBefore("@").replace(".", "_").lowercase()

                    val isCustomName = prefs.getBoolean(KEY_IS_CUSTOM_NAME, false)
                    val isCustomAvatar = prefs.getBoolean(KEY_IS_CUSTOM_AVATAR, false)
                    val savedDisplayName = prefs.getString(KEY_DISPLAY_NAME, null)
                    val savedAvatarUrl = prefs.getString(KEY_AVATAR_URL, null)

                    val resolvedDisplayName = if (isCustomName && !savedDisplayName.isNullOrBlank()) {
                        savedDisplayName
                    } else {
                        displayName
                    }
                    val resolvedAvatarUrl = if (isCustomAvatar && !savedAvatarUrl.isNullOrBlank()) {
                        savedAvatarUrl
                    } else {
                        avatarUrl
                    }

                    val profile = UserProfile(
                        id = firebaseUser.uid,
                        username = username,
                        displayName = resolvedDisplayName,
                        avatarUrl = resolvedAvatarUrl.ifBlank {
                            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
                        },
                        tagTitle = prefs.getString(KEY_TAG_TITLE, "GEN Z EXPLORER") ?: "GEN Z EXPLORER",
                        bio = prefs.getString(KEY_BIO, "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳") ?: "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳",
                        isCustomName = isCustomName,
                        isCustomAvatar = isCustomAvatar
                    )

                    saveSession(profile, email)
                    return@withContext AuthResult.Success(profile, email, isUpgrade = false)
                }
            } else {
                if (allowBrowserFallback) {
                    Log.d(TAG, "Unsupported credential type, attempting browser fallback...")
                    return@withContext signInWithGoogleBrowserInternal(activity)
                }
                return@withContext AuthResult.Error("Loại xác thực không được hỗ trợ. Vui lòng thử đăng nhập bằng trình duyệt.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In dialog", e)
            return@withContext AuthResult.Cancelled
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google accounts on device or Play Services missing. Switching to Browser OAuth fallback...", e)
            if (allowBrowserFallback) {
                return@withContext signInWithGoogleBrowserInternal(activity)
            }
            return@withContext AuthResult.Error(
                message = "Không tìm thấy tài khoản Google trên thiết bị. Bạn có thể sử dụng nút 'Đăng nhập qua trình duyệt'.",
                isNoCredential = true,
                canFallbackToBrowser = true
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.type} - ${e.message}", e)
            val msg = e.message?.lowercase() ?: ""
            if (msg.contains("16:") || msg.contains("canceled") || msg.contains("cancelled")) {
                return@withContext AuthResult.Cancelled
            }
            if (allowBrowserFallback && (msg.contains("play services") || msg.contains("unavailable") || msg.contains("not found") || msg.contains("developer_error") || msg.contains("api_not_connected"))) {
                Log.w(TAG, "Play Services error encountered. Auto-falling back to browser login...")
                return@withContext signInWithGoogleBrowserInternal(activity)
            }
            val errorMsg = if (msg.contains("network")) {
                "Lỗi kết nối mạng khi liên hệ máy chủ Google. Vui lòng kiểm tra kết nối mạng."
            } else {
                "Đăng nhập nhanh không thành công (${e.message ?: "Mã lỗi: ${e.type}"}). Bạn có thể chuyển sang đăng nhập qua trình duyệt."
            }
            return@withContext AuthResult.Error(errorMsg, canFallbackToBrowser = true)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception during primary sign-in", e)
            if (allowBrowserFallback) {
                Log.w(TAG, "Fallback to browser sign-in after unexpected primary sign-in error...")
                return@withContext signInWithGoogleBrowserInternal(activity)
            }
            return@withContext AuthResult.Error(
                message = "Đã xảy ra sự cố: ${e.localizedMessage ?: "Vui lòng thử lại"}",
                canFallbackToBrowser = true
            )
        }
    }

    /**
     * Fallback & Alternative Sign-In Method:
     * Opens official Google Sign-In in web browser / Custom Tabs via Firebase OAuthProvider.
     */
    suspend fun signInWithGoogleBrowser(activity: Activity): AuthResult = withContext(Dispatchers.IO) {
        signInWithGoogleBrowserInternal(activity)
    }

    private suspend fun signInWithGoogleBrowserInternal(activity: Activity): AuthResult {
        // 1. Check network connectivity
        if (!isNetworkAvailable()) {
            return AuthResult.Error(
                message = "Không có kết nối mạng. Vui lòng kiểm tra Wi-Fi hoặc 4G và thử lại.",
                isNetworkError = true
            )
        }

        try {
            Log.d(TAG, "Starting Browser-based Google OAuth via Firebase OAuthProvider...")
            val provider = OAuthProvider.newBuilder("google.com")
            provider.scopes = listOf("profile", "email")
            provider.addCustomParameter("prompt", "select_account")

            val currentUser = firebaseAuth.currentUser
            val isCurrentlyAnonymous = currentUser != null && currentUser.isAnonymous

            val authResult = if (isCurrentlyAnonymous) {
                Log.d(TAG, "Upgrading anonymous guest account via Browser OAuth link...")
                try {
                    currentUser!!.startActivityForLinkWithProvider(activity, provider.build()).await()
                } catch (e: Exception) {
                    val isCollision = e is FirebaseAuthUserCollisionException ||
                            (e is FirebaseAuthException && (e.errorCode == "ERROR_CREDENTIAL_ALREADY_IN_USE" || e.errorCode == "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL")) ||
                            e.message?.contains("already in use", ignoreCase = true) == true ||
                            e.message?.contains("collision", ignoreCase = true) == true

                    if (isCollision) {
                        Log.w(TAG, "Collision detected during browser link with existing account.")
                        return AuthResult.AccountCollision(
                            email = "tài khoản Google bạn vừa chọn",
                            credential = null
                        )
                    }
                    throw e
                }
            } else {
                val pendingResultTask = firebaseAuth.pendingAuthResult
                if (pendingResultTask != null) {
                    pendingResultTask.await()
                } else {
                    firebaseAuth.startActivityForSignInWithProvider(activity, provider.build()).await()
                }
            }

            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Không nhận được dữ liệu xác thực từ Google Web OAuth.")

            val email = firebaseUser.email ?: ""
            val displayName = firebaseUser.displayName?.ifBlank { null }
                ?: email.substringBefore("@").ifBlank { "Lữ khách Levart" }
            val username = email.substringBefore("@").replace(".", "_").lowercase().ifBlank { "traveler" }
            val avatarUrl = firebaseUser.photoUrl?.toString() ?: ""

            val isCustomName = prefs.getBoolean(KEY_IS_CUSTOM_NAME, false)
            val isCustomAvatar = prefs.getBoolean(KEY_IS_CUSTOM_AVATAR, false)
            val savedDisplayName = prefs.getString(KEY_DISPLAY_NAME, null)
            val savedAvatarUrl = prefs.getString(KEY_AVATAR_URL, null)

            val resolvedDisplayName = if (isCustomName && !savedDisplayName.isNullOrBlank()) {
                savedDisplayName
            } else {
                displayName
            }
            val resolvedAvatarUrl = if (isCustomAvatar && !savedAvatarUrl.isNullOrBlank()) {
                savedAvatarUrl
            } else {
                avatarUrl
            }

            val profile = UserProfile(
                id = firebaseUser.uid,
                username = username,
                displayName = resolvedDisplayName,
                avatarUrl = resolvedAvatarUrl.ifBlank {
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
                },
                tagTitle = prefs.getString(KEY_TAG_TITLE, "GEN Z EXPLORER") ?: "GEN Z EXPLORER",
                bio = prefs.getString(KEY_BIO, "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳") ?: "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳",
                isCustomName = isCustomName,
                isCustomAvatar = isCustomAvatar
            )

            saveSession(profile, email)
            Log.d(TAG, "Browser sign-in successful for user: ${profile.displayName} (UID: ${firebaseUser.uid})")
            return AuthResult.Success(profile, email, isUpgrade = isCurrentlyAnonymous)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No browser application installed on device", e)
            return AuthResult.Error(
                message = "Không tìm thấy trình duyệt web trên thiết bị để mở trang đăng nhập Google. Vui lòng cài đặt Chrome hoặc một trình duyệt bất kỳ rồi thử lại."
            )
        } catch (e: FirebaseAuthException) {
            Log.w(TAG, "FirebaseAuthException during browser sign-in: ${e.errorCode} - ${e.message}", e)
            val msg = e.message?.lowercase() ?: ""
            if (e.errorCode == "ERROR_WEB_CONTEXT_CANCELED" ||
                msg.contains("canceled") ||
                msg.contains("cancelled") ||
                msg.contains("closed") ||
                msg.contains("user cancelled")
            ) {
                return AuthResult.Cancelled
            }
            if (e is FirebaseNetworkException || msg.contains("network") || msg.contains("timeout")) {
                return AuthResult.Error(
                    message = "Lỗi kết nối mạng khi tải trang đăng nhập Google. Vui lòng kiểm tra lại đường truyền mạng.",
                    isNetworkError = true
                )
            }
            return AuthResult.Error("Đăng nhập qua trình duyệt thất bại: ${e.localizedMessage ?: "Vui lòng thử lại."}")
        } catch (e: Exception) {
            Log.e(TAG, "General exception during browser sign-in", e)
            val msg = e.message?.lowercase() ?: ""
            if (e is ActivityNotFoundException || e.cause is ActivityNotFoundException || msg.contains("no activity found") || msg.contains("browser")) {
                return AuthResult.Error(
                    message = "Không tìm thấy trình duyệt web trên thiết bị để mở trang đăng nhập Google. Vui lòng cài đặt Chrome rồi thử lại."
                )
            }
            if (msg.contains("canceled") || msg.contains("cancelled") || msg.contains("closed")) {
                return AuthResult.Cancelled
            }
            if (msg.contains("network") || msg.contains("timeout")) {
                return AuthResult.Error(
                    message = "Lỗi kết nối mạng trong quá trình mở trình duyệt. Vui lòng thử lại.",
                    isNetworkError = true
                )
            }
            return AuthResult.Error("Đã xảy ra sự cố khi đăng nhập bằng trình duyệt: ${e.localizedMessage ?: "Vui lòng thử lại."}")
        }
    }

    /**
     * In case of account collision during linking, allows the user to switch to the existing Google account.
     */
    suspend fun switchToExistingGoogleAccount(credential: AuthCredential?, activity: Activity? = null): AuthResult = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext AuthResult.Error("Không có kết nối mạng. Vui lòng kiểm tra lại kết nối.", isNetworkError = true)
        }
        try {
            // Sign out from the anonymous session first
            try {
                firebaseAuth.signOut()
            } catch (e: Exception) {
                Log.w(TAG, "Error signing out anonymous user before switch", e)
            }

            val authResult = if (credential != null) {
                firebaseAuth.signInWithCredential(credential).await()
            } else if (activity != null) {
                val provider = OAuthProvider.newBuilder("google.com")
                provider.scopes = listOf("profile", "email")
                provider.addCustomParameter("prompt", "select_account")
                firebaseAuth.startActivityForSignInWithProvider(activity, provider.build()).await()
            } else {
                throw IllegalArgumentException("Không có thông tin xác thực để chuyển tài khoản.")
            }

            val firebaseUser = authResult.user ?: throw IllegalStateException("Không nhận được dữ liệu xác thực từ Firebase.")
            val email = firebaseUser.email ?: ""
            val displayName = firebaseUser.displayName?.ifBlank { null } ?: email.substringBefore("@").ifBlank { "Lữ khách Levart" }
            val username = email.substringBefore("@").replace(".", "_").lowercase().ifBlank { "traveler" }
            val avatarUrl = firebaseUser.photoUrl?.toString() ?: ""

            val isCustomName = prefs.getBoolean(KEY_IS_CUSTOM_NAME, false)
            val isCustomAvatar = prefs.getBoolean(KEY_IS_CUSTOM_AVATAR, false)
            val savedDisplayName = prefs.getString(KEY_DISPLAY_NAME, null)
            val savedAvatarUrl = prefs.getString(KEY_AVATAR_URL, null)

            val resolvedDisplayName = if (isCustomName && !savedDisplayName.isNullOrBlank()) {
                savedDisplayName
            } else {
                displayName
            }
            val resolvedAvatarUrl = if (isCustomAvatar && !savedAvatarUrl.isNullOrBlank()) {
                savedAvatarUrl
            } else {
                avatarUrl
            }

            val profile = UserProfile(
                id = firebaseUser.uid,
                username = username,
                displayName = resolvedDisplayName,
                avatarUrl = resolvedAvatarUrl.ifBlank {
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
                },
                tagTitle = prefs.getString(KEY_TAG_TITLE, "GEN Z EXPLORER") ?: "GEN Z EXPLORER",
                bio = prefs.getString(KEY_BIO, "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳") ?: "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳",
                isCustomName = isCustomName,
                isCustomAvatar = isCustomAvatar
            )

            saveSession(profile, email)
            Log.d(TAG, "Switched successfully to existing Google account: ${profile.displayName}")
            return@withContext AuthResult.Success(profile, email, isUpgrade = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error switching to existing Google account", e)
            return@withContext AuthResult.Error("Không thể đăng nhập vào tài khoản cũ: ${e.localizedMessage ?: "Vui lòng thử lại"}")
        }
    }

    /**
     * Sign In using Email and Password with Firebase Authentication.
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Vui lòng nhập đầy đủ Email và Mật khẩu.")
        }
        if (!isNetworkAvailable()) {
            return@withContext AuthResult.Error("Không có kết nối mạng. Vui lòng kiểm tra kết nối và thử lại.", isNetworkError = true)
        }

        try {
            Log.d(TAG, "Attempting Email/Password sign-in for: $trimmedEmail")
            val authResult = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, password).await()
            val user = authResult.user ?: throw IllegalStateException("Không nhận được dữ liệu xác thực.")

            val profile = buildProfileFromFirebaseUser(user) ?: UserProfile(
                id = user.uid,
                username = trimmedEmail.substringBefore("@").replace(".", "_").lowercase().ifBlank { "traveler" },
                displayName = trimmedEmail.substringBefore("@"),
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop",
                tagTitle = "GEN Z EXPLORER",
                bio = "Tài khoản Levart: $trimmedEmail • Yêu du lịch & Khám phá Việt Nam 🇻🇳"
            )
            saveSession(profile, trimmedEmail)
            Log.d(TAG, "Email/Password sign-in successful: ${profile.displayName}")
            AuthResult.Success(profile, trimmedEmail)
        } catch (e: FirebaseAuthException) {
            Log.w(TAG, "FirebaseAuthException during email sign-in: ${e.errorCode} - ${e.message}")
            val msg = when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "Không tìm thấy tài khoản với email này. Bạn có thể chọn tab 'Đăng ký' để tạo mới tài khoản."
                "ERROR_WRONG_PASSWORD" -> "Mật khẩu không chính xác. Vui lòng kiểm tra lại."
                "ERROR_INVALID_EMAIL" -> "Định dạng email không hợp lệ (ví dụ: ten@gmail.com)."
                "ERROR_USER_DISABLED" -> "Tài khoản này đã bị tạm khoá."
                else -> e.localizedMessage ?: "Đăng nhập bằng Email không thành công."
            }
            AuthResult.Error(msg)
        } catch (e: Exception) {
            Log.e(TAG, "Error during email sign in", e)
            val msg = e.message?.lowercase() ?: ""
            if (e is FirebaseNetworkException || msg.contains("network")) {
                AuthResult.Error("Lỗi kết nối mạng đến máy chủ xác thực. Vui lòng thử lại.", isNetworkError = true)
            } else {
                AuthResult.Error("Lỗi đăng nhập: ${e.localizedMessage ?: "Vui lòng thử lại"}")
            }
        }
    }

    /**
     * Sign Up / Register a new account with Email and Password.
     */
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): AuthResult = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.length < 6) {
            return@withContext AuthResult.Error("Mật khẩu phải có ít nhất 6 ký tự.")
        }
        if (!isNetworkAvailable()) {
            return@withContext AuthResult.Error("Không có kết nối mạng. Vui lòng kiểm tra kết nối và thử lại.", isNetworkError = true)
        }

        try {
            Log.d(TAG, "Attempting Email/Password sign-up for: $trimmedEmail")
            val authResult = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            val user = authResult.user ?: throw IllegalStateException("Không thể tạo tài khoản.")

            val name = displayName.trim().ifBlank { trimmedEmail.substringBefore("@") }
            val profile = UserProfile(
                id = user.uid,
                username = trimmedEmail.substringBefore("@").replace(".", "_").lowercase().ifBlank { "traveler" },
                displayName = name,
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop",
                tagTitle = "GEN Z EXPLORER",
                bio = "Tài khoản Levart: $trimmedEmail • Yêu du lịch & Khám phá Việt Nam 🇻🇳"
            )
            saveSession(profile, trimmedEmail)
            Log.d(TAG, "Email sign-up successful for: ${profile.displayName}")
            AuthResult.Success(profile, trimmedEmail)
        } catch (e: FirebaseAuthException) {
            Log.w(TAG, "FirebaseAuthException during email sign-up: ${e.errorCode} - ${e.message}")
            val msg = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email này đã được sử dụng. Vui lòng chuyển sang tab 'Đăng nhập' hoặc chọn email khác."
                "ERROR_WEAK_PASSWORD" -> "Mật khẩu quá yếu. Vui lòng chọn mật khẩu từ 6 ký tự trở lên."
                "ERROR_INVALID_EMAIL" -> "Định dạng email không hợp lệ (ví dụ: ten@gmail.com)."
                else -> e.localizedMessage ?: "Đăng ký tài khoản không thành công."
            }
            AuthResult.Error(msg)
        } catch (e: Exception) {
            Log.e(TAG, "Error during email sign up", e)
            val msg = e.message?.lowercase() ?: ""
            if (e is FirebaseNetworkException || msg.contains("network")) {
                AuthResult.Error("Lỗi kết nối mạng đến máy chủ xác thực. Vui lòng thử lại.", isNetworkError = true)
            } else {
                AuthResult.Error("Lỗi đăng ký: ${e.localizedMessage ?: "Vui lòng thử lại"}")
            }
        }
    }

    /**
     * Real Firebase Anonymous Authentication for Guest Mode ("Khám phá ngay (Chế độ Lữ Khách)"):
     * Issues a genuine anonymous Firebase user account. No fake ID or fake email.
     * Persists across app restarts.
     */
    suspend fun signInAsGuest(customName: String = "Lữ khách Khám Phá"): AuthResult = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext AuthResult.Error(
                message = "Không có kết nối mạng. Vui lòng kiểm tra Wi-Fi hoặc 4G để khởi tạo chế độ lữ khách.",
                isNetworkError = true
            )
        }
        try {
            Log.d(TAG, "Performing genuine Firebase signInAnonymously()...")
            val authResult = firebaseAuth.signInAnonymously().await()
            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Không nhận được dữ liệu xác thực ẩn danh từ Firebase Auth.")

            val name = customName.trim().ifBlank { "Lữ khách Khám Phá" }
            val username = "traveler_${firebaseUser.uid.takeLast(6).lowercase()}"

            val profile = UserProfile(
                id = firebaseUser.uid,
                username = username,
                displayName = name,
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop",
                tagTitle = "GEN Z EXPLORER",
                bio = "Chế độ Lữ Khách • Khám phá 34 tỉnh thành Việt Nam 🇻🇳"
            )
            saveSession(profile, email = "")
            Log.d(TAG, "Firebase Anonymous Sign-In successful. UID: ${firebaseUser.uid}")
            AuthResult.Success(profile, email = "")
        } catch (e: Exception) {
            Log.e(TAG, "Guest login error", e)
            val msg = e.message?.lowercase() ?: ""
            if (e is FirebaseNetworkException || msg.contains("network")) {
                AuthResult.Error("Lỗi kết nối mạng khi khởi tạo chế độ lữ khách. Vui lòng thử lại.", isNetworkError = true)
            } else {
                AuthResult.Error("Không thể kích hoạt chế độ lữ khách: ${e.localizedMessage ?: "Vui lòng thử lại"}")
            }
        }
    }

    private fun saveSession(profile: UserProfile, email: String) {
        prefs.edit()
            .putString(KEY_USER_ID, profile.id)
            .putString(KEY_EMAIL, email)
            .putString(KEY_DISPLAY_NAME, profile.displayName)
            .putString(KEY_USERNAME, profile.username)
            .putString(KEY_AVATAR_URL, profile.avatarUrl)
            .putString(KEY_TAG_TITLE, profile.tagTitle)
            .putString(KEY_BIO, profile.bio)
            .putBoolean(KEY_IS_CUSTOM_NAME, profile.isCustomName)
            .putBoolean(KEY_IS_CUSTOM_AVATAR, profile.isCustomAvatar)
            .apply()

        _isLoggedIn.value = true
        _isAnonymous.value = firebaseAuth.currentUser?.isAnonymous == true
        _currentAuthUser.value = profile
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        clearLocalState()
    }

    /**
     * Deletes user account completely from Firebase Auth.
     * If Firebase Auth raises FirebaseAuthRecentLoginRequiredException,
     * automatically requests re-authentication with Google, then resumes deleting the account.
     */
    suspend fun deleteAccount(activity: Activity): DeleteAccountResult = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            clearLocalState()
            return@withContext DeleteAccountResult.Success
        }

        try {
            Log.d(TAG, "Attempting to delete Firebase user: ${user.uid}")
            user.delete().await()
            clearLocalState()
            Log.d(TAG, "Firebase user deleted successfully")
            return@withContext DeleteAccountResult.Success
        } catch (e: Exception) {
            Log.w(TAG, "Exception during user.delete(): ${e.javaClass.simpleName} - ${e.message}", e)
            val msg = e.message?.lowercase() ?: ""
            val isRecentLoginRequired = e is FirebaseAuthRecentLoginRequiredException ||
                    (e is FirebaseAuthException && (e.errorCode == "ERROR_REQUIRES_RECENT_LOGIN" || msg.contains("recent") || msg.contains("requires recent authentication"))) ||
                    msg.contains("recent-login-required") ||
                    msg.contains("requires recent") ||
                    msg.contains("credential_too_old")

            if (isRecentLoginRequired) {
                Log.w(TAG, "Recent login required to delete account. Prompting re-authentication flow...")
                when (val reauthResult = signInWithGoogle(activity, allowBrowserFallback = true)) {
                    is AuthResult.Success -> {
                        val reAuthedUser = firebaseAuth.currentUser
                        if (reAuthedUser != null) {
                            try {
                                reAuthedUser.delete().await()
                                clearLocalState()
                                Log.d(TAG, "Firebase user deleted successfully after re-authentication")
                                return@withContext DeleteAccountResult.Success
                            } catch (e2: Exception) {
                                Log.e(TAG, "Delete failed after re-auth: ${e2.message}", e2)
                                return@withContext DeleteAccountResult.Error(
                                    "Không thể hoàn tất xoá tài khoản: ${e2.localizedMessage ?: "Vui lòng thử lại"}"
                                )
                            }
                        } else {
                            clearLocalState()
                            return@withContext DeleteAccountResult.Success
                        }
                    }
                    is AuthResult.Cancelled -> {
                        return@withContext DeleteAccountResult.Cancelled
                    }
                    is AuthResult.AccountCollision -> {
                        return@withContext DeleteAccountResult.Error("Phát hiện xung đột tài khoản trong quá trình xác thực lại.")
                    }
                    is AuthResult.Error -> {
                        return@withContext DeleteAccountResult.Error(
                            "Yêu cầu xác thực tài khoản trước khi xoá: ${reauthResult.message}"
                        )
                    }
                }
            } else {
                if (e is FirebaseNetworkException || msg.contains("network") || msg.contains("timeout")) {
                    return@withContext DeleteAccountResult.Error("Lỗi kết nối mạng khi liên hệ máy chủ để xoá tài khoản. Vui lòng kiểm tra kết nối mạng và thử lại.")
                }
                if (e is FirebaseAuthException && (e.errorCode == "ERROR_USER_NOT_FOUND" || msg.contains("no user"))) {
                    clearLocalState()
                    return@withContext DeleteAccountResult.Success
                }
                return@withContext DeleteAccountResult.Error("Không thể xoá tài khoản: ${e.localizedMessage ?: "Vui lòng thử lại sau"}")
            }
        }
    }

    private suspend fun clearLocalState() {
        try {
            credentialManager.clearCredentialState(
                androidx.credentials.ClearCredentialStateRequest()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear credential manager state", e)
        }

        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sign out of Firebase Auth", e)
        }

        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        _isAnonymous.value = false
        _currentAuthUser.value = null
    }

    fun updateSavedUserProfile(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_DISPLAY_NAME, profile.displayName)
            .putString(KEY_USERNAME, profile.username)
            .putString(KEY_AVATAR_URL, profile.avatarUrl)
            .putString(KEY_TAG_TITLE, profile.tagTitle)
            .putString(KEY_BIO, profile.bio)
            .putBoolean(KEY_IS_CUSTOM_NAME, profile.isCustomName)
            .putBoolean(KEY_IS_CUSTOM_AVATAR, profile.isCustomAvatar)
            .apply()

        _currentAuthUser.value = profile
    }
}
