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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
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
    data class Success(val userProfile: UserProfile, val email: String) : AuthResult()
    data object Cancelled : AuthResult()
    data class Error(
        val message: String,
        val isNetworkError: Boolean = false,
        val isNoCredential: Boolean = false,
        val canFallbackToBrowser: Boolean = false
    ) : AuthResult()
}

class AuthManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lumina_auth_prefs", Context.MODE_PRIVATE)
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentAuthUser = MutableStateFlow(buildProfileFromFirebaseUser(firebaseAuth.currentUser) ?: getSavedUser())
    val currentAuthUser: StateFlow<UserProfile?> = _currentAuthUser.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            val isUserLoggedIn = user != null
            _isLoggedIn.value = isUserLoggedIn
            if (user != null) {
                val profile = buildProfileFromFirebaseUser(user)
                _currentAuthUser.value = profile
            } else {
                _currentAuthUser.value = null
            }
        }
    }

    companion object {
        private const val TAG = "AuthManager"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_EMAIL = "auth_user_email"
        private const val KEY_DISPLAY_NAME = "auth_user_display_name"
        private const val KEY_USERNAME = "auth_user_username"
        private const val KEY_AVATAR_URL = "auth_user_avatar_url"
        private const val KEY_TAG_TITLE = "auth_user_tag_title"
        private const val KEY_BIO = "auth_user_bio"
    }

    /**
     * Reads default_web_client_id generated automatically by Google Services gradle plugin
     * from google-services.json.
     */
    fun getWebClientId(): String? {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val clientId = context.getString(resId)
                if (clientId.isNotBlank()) clientId else null
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting default_web_client_id resource", e)
            null
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun buildProfileFromFirebaseUser(user: FirebaseUser?): UserProfile? {
        if (user == null) return null
        val email = user.email ?: ""
        val displayName = user.displayName?.ifBlank { null }
            ?: email.substringBefore("@").ifBlank { "Lữ khách Lumina" }
        val username = email.substringBefore("@").replace(".", "_").lowercase().ifBlank { "traveler" }
        val avatarUrl = user.photoUrl?.toString() ?: ""

        val savedBio = prefs.getString(KEY_BIO, null)
        val savedTag = prefs.getString(KEY_TAG_TITLE, null)

        return UserProfile(
            id = "google_${user.uid}",
            username = username,
            displayName = displayName,
            avatarUrl = avatarUrl.ifBlank {
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
            },
            tagTitle = savedTag ?: "GEN Z EXPLORER",
            bio = savedBio ?: "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳"
        )
    }

    private fun getSavedUser(): UserProfile? {
        if (firebaseAuth.currentUser == null) return null

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

    /**
     * Primary Sign-In Method: Uses CredentialManager (One-Tap / Google Play Services).
     * If Google Play Services is missing or no accounts exist on the device, it automatically
     * falls back to Browser-based OAuth so users are never blocked.
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

                // 4. Authenticate with Firebase Auth using the Google ID Token
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)
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

                val profile = UserProfile(
                    id = "google_${firebaseUser.uid}",
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
                // User dismissed or canceled
                return@withContext AuthResult.Cancelled
            }
            // If Play Services issue or no credential found, automatically fallback
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
     * Works on any Android device with or without Google Play Services.
     * Resolves to the exact same unified Google User ID and profile data.
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

            // Check if there is an in-flight pending auth result from previous activity recreation
            val pendingResultTask = firebaseAuth.pendingAuthResult
            val authResult = if (pendingResultTask != null) {
                pendingResultTask.await()
            } else {
                firebaseAuth.startActivityForSignInWithProvider(activity, provider.build()).await()
            }

            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Không nhận được dữ liệu xác thực từ Google Web OAuth.")

            val email = firebaseUser.email ?: ""
            val displayName = firebaseUser.displayName?.ifBlank { null }
                ?: email.substringBefore("@").ifBlank { "Lữ khách Lumina" }
            val username = email.substringBefore("@").replace(".", "_").lowercase().ifBlank { "traveler" }
            val avatarUrl = firebaseUser.photoUrl?.toString() ?: ""

            val profile = UserProfile(
                id = "google_${firebaseUser.uid}",
                username = username,
                displayName = displayName,
                avatarUrl = avatarUrl.ifBlank {
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop"
                },
                tagTitle = "GEN Z EXPLORER",
                bio = "Tài khoản Google: $email • Yêu du lịch & Khám phá Việt Nam 🇻🇳"
            )

            saveSession(profile, email)
            Log.d(TAG, "Browser sign-in successful for user: ${profile.displayName} (UID: ${firebaseUser.uid})")
            return AuthResult.Success(profile, email)
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
                // User cleanly closed browser or pressed back
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

    private fun saveSession(profile: UserProfile, email: String) {
        prefs.edit()
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

        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sign out of Firebase Auth", e)
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
