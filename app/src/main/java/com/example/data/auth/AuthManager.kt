package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.data.model.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

sealed class AuthResult {
    data class Success(val userProfile: UserProfile, val email: String) : AuthResult()
    data object Cancelled : AuthResult()
    data class Error(val message: String, val isNetworkError: Boolean = false, val isNoCredential: Boolean = false) : AuthResult()
}

class AuthManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lumina_auth_prefs", Context.MODE_PRIVATE)
    private val credentialManager = CredentialManager.create(context)

    private val _isLoggedIn = MutableStateFlow(getSavedLoginState())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentAuthUser = MutableStateFlow(getSavedUser())
    val currentAuthUser: StateFlow<UserProfile?> = _currentAuthUser.asStateFlow()

    companion object {
        private const val TAG = "AuthManager"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_EMAIL = "auth_user_email"
        private const val KEY_DISPLAY_NAME = "auth_user_display_name"
        private const val KEY_USERNAME = "auth_user_username"
        private const val KEY_AVATAR_URL = "auth_user_avatar_url"
        private const val KEY_TAG_TITLE = "auth_user_tag_title"
        private const val KEY_BIO = "auth_user_bio"

        // Default OAuth Web Client ID for Lumina App (can be injected via BuildConfig or backend)
        private const val DEFAULT_WEB_CLIENT_ID = "193537204977-lumina-vietnam.apps.googleusercontent.com"
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun getSavedLoginState(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun getSavedUser(): UserProfile? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val displayName = prefs.getString(KEY_DISPLAY_NAME, "Lữ khách Lumina") ?: "Lữ khách Lumina"
        val username = prefs.getString(KEY_USERNAME, "traveler") ?: "traveler"
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, "") ?: ""
        val tagTitle = prefs.getString(KEY_TAG_TITLE, "GEN Z EXPLORER") ?: "GEN Z EXPLORER"
        val bio = prefs.getString(KEY_BIO, "Khám phá 34 tỉnh thành Việt Nam cùng Lumina ✨") ?: ""

        return UserProfile(
            id = id,
            username = username,
            displayName = displayName,
            avatarUrl = avatarUrl.ifBlank {
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
            },
            tagTitle = tagTitle,
            bio = bio
        )
    }

    suspend fun signInWithGoogle(activity: Activity): AuthResult = withContext(Dispatchers.IO) {
        // 1. Check network connectivity
        if (!isNetworkAvailable()) {
            return@withContext AuthResult.Error(
                message = "Không có kết nối mạng. Vui lòng kiểm tra Wi-Fi hoặc 4G và thử lại.",
                isNetworkError = true
            )
        }

        try {
            // 2. Setup GoogleIdOption / Credential Request
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(DEFAULT_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d(TAG, "Requesting Google Sign-In via CredentialManager...")
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdToken.id
                val displayName = googleIdToken.displayName ?: googleIdToken.givenName ?: email.substringBefore("@")
                val avatarUrl = googleIdToken.profilePictureUri?.toString() ?: ""
                val username = email.substringBefore("@").replace(".", "_").lowercase()

                val profile = UserProfile(
                    id = "google_${email.replace("@", "_").replace(".", "_")}",
                    username = username,
                    displayName = displayName,
                    avatarUrl = avatarUrl.ifBlank {
                        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
                    },
                    tagTitle = "GEN Z EXPLORER",
                    bio = "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳"
                )

                saveSession(profile, email)
                return@withContext AuthResult.Success(profile, email)
            } else {
                return@withContext AuthResult.Error("Loại xác thực không được hỗ trợ. Vui lòng thử lại.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In", e)
            return@withContext AuthResult.Cancelled
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google accounts available on device or Play Services not configured", e)
            return@withContext AuthResult.Error(
                message = "Không tìm thấy tài khoản Google trên thiết bị hoặc Google Play Services chưa sẵn sàng.",
                isNoCredential = true
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.type}", e)
            // Handle developer error or missing web client id gracefully
            val errorMsg = when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Lỗi kết nối mạng khi liên hệ máy chủ Google. Vui lòng thử lại."
                e.message?.contains("16:") == true || e.message?.contains("CANCELED") == true ->
                    return@withContext AuthResult.Cancelled
                else ->
                    "Đăng nhập Google không thành công (${e.message ?: "Mã lỗi: ${e.type}"}). Vui lòng thử lại."
            }
            return@withContext AuthResult.Error(errorMsg)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected sign in exception", e)
            return@withContext AuthResult.Error("Đã xảy ra sự cố trong quá trình đăng nhập (${e.localizedMessage ?: "Vui lòng thử lại"}).")
        }
    }

    /**
     * Fallback login method (used when running on an emulator without Play Services accounts
     * or when testing directly with the user's specific Google email).
     */
    fun signInDirect(email: String, displayName: String, avatarUrl: String = ""): UserProfile {
        val username = email.substringBefore("@").replace(".", "_").lowercase()
        val profile = UserProfile(
            id = "google_${email.replace("@", "_").replace(".", "_")}",
            username = username,
            displayName = displayName,
            avatarUrl = avatarUrl.ifBlank {
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
            },
            tagTitle = "GEN Z EXPLORER",
            bio = "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳"
        )
        saveSession(profile, email)
        return profile
    }

    private fun saveSession(profile: UserProfile, email: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, profile.id)
            .putString(KEY_EMAIL, email)
            .putString(KEY_DISPLAY_NAME, profile.displayName)
            .putString(KEY_USERNAME, profile.username)
            .putString(KEY_AVATAR_URL, profile.avatarUrl)
            .putString(KEY_TAG_TITLE, profile.tagTitle)
            .putString(KEY_BIO, profile.bio)
            .apply()

        _isLoggedIn.value = true
        _currentAuthUser.value = profile
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            credentialManager.clearCredentialState(
                androidx.credentials.ClearCredentialStateRequest()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear credential manager state", e)
        }

        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        _currentAuthUser.value = null
    }

    fun updateSavedUserProfile(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_DISPLAY_NAME, profile.displayName)
            .putString(KEY_USERNAME, profile.username)
            .putString(KEY_AVATAR_URL, profile.avatarUrl)
            .putString(KEY_TAG_TITLE, profile.tagTitle)
            .putString(KEY_BIO, profile.bio)
            .apply()

        _currentAuthUser.value = profile
    }
}
