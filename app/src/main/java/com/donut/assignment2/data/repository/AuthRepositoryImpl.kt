package com.donut.assignment2.data.repository

import android.app.Activity
import android.util.Log
import com.donut.assignment2.domain.model.User
import com.donut.assignment2.domain.model.UserRole
import com.donut.assignment2.domain.repository.AuthRepository
import com.donut.assignment2.domain.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.firestore.Source
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override suspend fun sendOTP(
        phoneNumber: String,
        activity: Activity
    ): Flow<OTPResult> = callbackFlow {
        Log.d(TAG, "Sending OTP to: $phoneNumber")

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Verification completed automatically")
                trySend(OTPResult.AutoVerified)
                close()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Verification failed", e)
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        if (e.message?.contains("blocked", ignoreCase = true) == true) {
                            "Số điện thoại này chưa được đăng ký trong hệ thống"
                        } else {
                            "Số điện thoại không hợp lệ"
                        }
                    }
                    is FirebaseAuthException -> {
                        when (e.errorCode) {
                            "ERROR_TOO_MANY_REQUESTS" -> "Quá nhiều yêu cầu. Vui lòng thử lại sau"
                            "ERROR_QUOTA_EXCEEDED" -> "Đã vượt quá giới hạn gửi SMS"
                            else -> "Gửi OTP thất bại: ${e.message}"
                        }
                    }
                    else -> "Lỗi kết nối: ${e.message}"
                }
                trySend(OTPResult.Error(Exception(errorMessage)))
                close(e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "Code sent successfully, verificationId: $verificationId")
                trySend(OTPResult.Success(verificationId))
            }
        }

        try {
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during OTP send", e)
            trySend(OTPResult.Error(Exception("Không thể gửi OTP: ${e.message}")))
            close(e)
        }

        awaitClose {
            Log.d(TAG, "Closing OTP flow")
        }
    }

    override suspend fun verifyOTP(
        verificationId: String,
        otp: String
    ): Result<User> {
        return try {
            Log.d(TAG, "Verifying OTP with verificationId: $verificationId")

            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            val currentUser = authResult.user
            if (currentUser != null) {
                val phoneNumber = currentUser.phoneNumber ?: ""
                Log.d(TAG, "Firebase auth successful for phone: $phoneNumber")

                try {
                    // 🔥 Use phone number as document ID in Firestore
                    val userDoc = firestore.collection("users")
                        .document(phoneNumber)
                        .get(Source.SERVER)
                        .await()

                    if (!userDoc.exists()) {
                        Log.w(TAG, "User document not found for phone: $phoneNumber")
                        return Result.failure(Exception(
                            "Số điện thoại $phoneNumber chưa được đăng ký trong hệ thống. Liên hệ admin để được cấp tài khoản."
                        ))
                    }

                    // Convert Firestore document to User model
                    val user = mapFirestoreDocumentToUser(userDoc.data!!, phoneNumber)

                    Log.d(TAG, "User loaded successfully: ${user.fullName} (${user.role})")

                    // 🔄 Cache user data locally for offline access
                    try {
                        userRepository.saveUserToLocal(user)
                        Log.d(TAG, "User cached locally")
                    } catch (cacheException: Exception) {
                        Log.w(TAG, "Failed to cache user locally", cacheException)
                        // Continue anyway, local cache is not critical
                    }

                    Result.success(user)

                } catch (e: Exception) {
                    Log.e(TAG, "Exception during OTP verification", e)

                    val errorMessage = when {
                        // Network/offline errors
                        e.message?.contains("offline", ignoreCase = true) == true ||
                                e.message?.contains("network", ignoreCase = true) == true -> {
                            "Không có kết nối mạng. Vui lòng kiểm tra internet và thử lại."
                        }

                        // Firestore unavailable
                        e.message?.contains("unavailable", ignoreCase = true) == true -> {
                            "Dịch vụ tạm thời không khả dụng. Vui lòng thử lại sau."
                        }

                        // Document not found
                        e.message?.contains("document", ignoreCase = true) == true -> {
                            "Số điện thoại chưa được đăng ký trong hệ thống"
                        }

                        // Permission denied
                        e.message?.contains("permission", ignoreCase = true) == true -> {
                            "Không có quyền truy cập. Liên hệ admin."
                        }

                        else -> "Xác thực thất bại: ${e.message}"
                    }

                    Result.failure(Exception(errorMessage))
                }
            } else {
                Log.e(TAG, "Firebase auth result is null")
                Result.failure(IllegalStateException("Xác thực thất bại: Không nhận được thông tin người dùng"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during OTP verification", e)

            val errorMessage = when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    when {
                        e.message?.contains("invalid-verification-code", ignoreCase = true) == true ->
                            "Mã OTP không đúng"
                        e.message?.contains("session-expired", ignoreCase = true) == true ->
                            "Mã OTP đã hết hạn. Vui lòng gửi lại mã mới"
                        else -> "Mã OTP không hợp lệ"
                    }
                }
                is FirebaseAuthInvalidUserException -> "Người dùng không hợp lệ"
                is FirebaseAuthException -> {
                    when (e.errorCode) {
                        "ERROR_TOO_MANY_REQUESTS" -> "Quá nhiều yêu cầu xác thực. Vui lòng thử lại sau"
                        "ERROR_NETWORK_REQUEST_FAILED" -> "Lỗi kết nối mạng. Kiểm tra internet và thử lại"
                        else -> "Lỗi xác thực Firebase: ${e.message}"
                    }
                }
                else -> "Lỗi xác thực: ${e.message}"
            }

            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        val phoneNumber = firebaseUser.phoneNumber ?: return null

        return try {
            // First try local cache
            val cachedUser = userRepository.getUserByPhone(phoneNumber)
            if (cachedUser != null) {
                Log.d(TAG, "Returning cached user: ${cachedUser.fullName}")
                return cachedUser
            }

            // Fallback to Firestore
            val userDoc = firestore.collection("users")
                .document(phoneNumber)
                .get()
                .await()

            if (userDoc.exists() && userDoc.data != null) {
                val user = mapFirestoreDocumentToUser(userDoc.data!!, phoneNumber)

                // Cache for next time
                try {
                    userRepository.saveUserToLocal(user)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to cache current user", e)
                }

                user
            } else {
                Log.w(TAG, "Current user document not found in Firestore")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            null
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            Log.d(TAG, "Signing out user")
            firebaseAuth.signOut()

            // Optional: Clear local cache
            try {
                userRepository.clearLocalCache()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear local cache during signout", e)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during signout", e)
            Result.failure(Exception("Đăng xuất thất bại: ${e.message}"))
        }
    }

    override fun isUserLoggedIn(): Boolean {
        val isLoggedIn = firebaseAuth.currentUser != null
        Log.d(TAG, "User logged in status: $isLoggedIn")
        return isLoggedIn
    }

    override suspend fun updateUserProfile(
        displayName: String?,
        email: String?
    ): Result<Unit> {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            return Result.failure(IllegalStateException("Người dùng chưa đăng nhập"))
        }

        val phoneNumber = currentUser.phoneNumber
        if (phoneNumber.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Không tìm thấy số điện thoại"))
        }

        return try {
            // Update Firebase Auth profile
            if (!displayName.isNullOrBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                currentUser.updateProfile(profileUpdates).await()
            }

            // Update email if provided
            if (!email.isNullOrBlank()) {
                currentUser.updateEmail(email).await()
            }

            // Update Firestore document
            val updates = mutableMapOf<String, Any>()
            if (!displayName.isNullOrBlank()) {
                updates["fullName"] = displayName
            }
            if (!email.isNullOrBlank()) {
                updates["email"] = email
            }

            if (updates.isNotEmpty()) {
                firestore.collection("users")
                    .document(phoneNumber)
                    .update(updates)
                    .await()

                Log.d(TAG, "User profile updated successfully")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            val errorMessage = when (e) {
                is FirebaseAuthRecentLoginRequiredException ->
                    "Cần đăng nhập lại để cập nhật thông tin"
                is FirebaseAuthInvalidUserException ->
                    "Người dùng không hợp lệ"
                else -> "Cập nhật thông tin thất bại: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            return Result.failure(IllegalStateException("Người dùng chưa đăng nhập"))
        }

        val phoneNumber = currentUser.phoneNumber ?: ""

        return try {
            // Delete Firestore document first
            if (phoneNumber.isNotBlank()) {
                firestore.collection("users")
                    .document(phoneNumber)
                    .delete()
                    .await()
                Log.d(TAG, "User document deleted from Firestore")
            }

            // Delete Firebase Auth account
            currentUser.delete().await()
            Log.d(TAG, "Firebase Auth account deleted")

            // Clear local cache
            try {
                userRepository.clearLocalCache()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear cache during account deletion", e)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account", e)
            val errorMessage = when (e) {
                is FirebaseAuthRecentLoginRequiredException ->
                    "Cần đăng nhập lại để xóa tài khoản"
                is FirebaseAuthInvalidUserException ->
                    "Tài khoản không hợp lệ"
                else -> "Xóa tài khoản thất bại: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun refreshUser(): Result<User?> {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.d(TAG, "No current user to refresh")
            return Result.success(null)
        }

        return try {
            // Reload Firebase Auth user
            currentUser.reload().await()

            // Get updated user data from Firestore
            val user = getCurrentUser()

            Log.d(TAG, "User refreshed successfully")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing user", e)
            Result.failure(Exception("Làm mới thông tin người dùng thất bại: ${e.message}"))
        }
    }

    // 🔧 Helper function to map Firestore document to User model
    private fun mapFirestoreDocumentToUser(data: Map<String, Any>, phoneNumber: String): User {
        return User(
            phoneNumber = data["phoneNumber"] as? String ?: phoneNumber,
            fullName = data["fullName"] as? String ?: "Unknown User",
            email = data["email"] as? String,
            role = try {
                UserRole.valueOf(data["role"] as? String ?: "INSPECTOR")
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Invalid role in Firestore, defaulting to INSPECTOR", e)
                UserRole.INSPECTOR
            },
            supervisorPhone = data["supervisorPhone"] as? String,
            profileImageUrl = data["profileImageUrl"] as? String
        )
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}