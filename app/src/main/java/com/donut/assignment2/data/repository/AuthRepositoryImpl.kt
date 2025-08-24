package com.donut.assignment2.data.repository

import android.app.Activity
import android.util.Log
import com.donut.assignment2.domain.model.User
import com.donut.assignment2.domain.model.UserRole
import com.donut.assignment2.domain.repository.AuthRepository
import com.donut.assignment2.domain.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
        private const val USERS_COLLECTION = "users"
    }

    override suspend fun sendOTP(phoneNumber: String, activity: Activity): Flow<OTPResult> = callbackFlow {
        Log.d(TAG, "Sending OTP to: $phoneNumber")

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Verification completed automatically")
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            trySend(OTPResult.AutoVerified)
                        } else {
                            trySend(OTPResult.Error(task.exception ?: Exception("Auto verification failed")))
                        }
                        close()
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Verification failed: ${e.message}", e)

                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Số điện thoại không hợp lệ"
                    is FirebaseTooManyRequestsException -> "Quá nhiều yêu cầu. Vui lòng thử lại sau"
                    is FirebaseAuthException -> "Lỗi xác thực: ${e.message}"
                    else -> "Gửi OTP thất bại: ${e.message}"
                }

                trySend(OTPResult.Error(Exception(errorMessage)))
                close()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "Code sent successfully. VerificationId: $verificationId")
                trySend(OTPResult.Success(verificationId))
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        try {
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting phone verification", e)
            trySend(OTPResult.Error(e))
            close()
        }

        awaitClose {
            Log.d(TAG, "OTP flow closed")
        }
    }

    override suspend fun verifyOTP(verificationId: String, otp: String): Result<User> {
        Log.d(TAG, "🔥 Starting OTP verification")
        Log.d(TAG, "VerificationId: ${verificationId.take(20)}...")
        Log.d(TAG, "OTP Code: $otp")

        return try {
            // Step 1: Create credential
            Log.d(TAG, "🔥 Step 1: Creating credential")
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)

            // Step 2: Sign in with Firebase
            Log.d(TAG, "🔥 Step 2: Signing in with Firebase")
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            val firebaseUser = authResult.user
            if (firebaseUser == null) {
                Log.e(TAG, "🔥 ERROR: Firebase user is null after sign in")
                return Result.failure(Exception("Đăng nhập thất bại: Firebase user null"))
            }

            Log.d(TAG, "🔥 Step 3: Firebase sign in successful")
            Log.d(TAG, "Firebase User ID: ${firebaseUser.uid}")
            Log.d(TAG, "Firebase Phone: ${firebaseUser.phoneNumber}")

            // Step 3: Get phone number
            val phoneNumber = firebaseUser.phoneNumber
            if (phoneNumber.isNullOrBlank()) {
                Log.e(TAG, "🔥 ERROR: Phone number is null/blank")
                return Result.failure(Exception("Không lấy được số điện thoại"))
            }

            Log.d(TAG, "🔥 Step 4: Getting user from Firestore")
            // Step 4: Get or create user
            val user = getOrCreateUser(firebaseUser)

            Log.d(TAG, "🔥 Step 5: Saving user to local cache")
            // Step 5: Save to local
            userRepository.saveUserToLocal(user).fold(
                onSuccess = {
                    Log.d(TAG, "🔥 User saved to local successfully")
                },
                onFailure = { error ->
                    Log.w(TAG, "🔥 WARNING: Failed to save user to local: ${error.message}")
                    // Continue anyway, local cache failure shouldn't block login
                }
            )

            Log.d(TAG, "🔥 SUCCESS: User verification complete for ${user.phoneNumber}")
            Result.success(user)

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e(TAG, "🔥 FIREBASE AUTH ERROR: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("invalid verification code", ignoreCase = true) == true ->
                    "Mã OTP không đúng"
                e.message?.contains("expired", ignoreCase = true) == true ->
                    "Mã OTP đã hết hạn"
                else -> "Mã xác thực không hợp lệ: ${e.message}"
            }
            Result.failure(Exception(errorMessage))

        } catch (e: FirebaseNetworkException) {
            Log.e(TAG, "🔥 NETWORK ERROR: ${e.message}", e)
            Result.failure(Exception("Không có kết nối mạng. Vui lòng kiểm tra internet"))

        } catch (e: FirebaseTooManyRequestsException) {
            Log.e(TAG, "🔥 TOO MANY REQUESTS: ${e.message}", e)
            Result.failure(Exception("Quá nhiều yêu cầu. Vui lòng thử lại sau"))

        } catch (e: FirebaseException) {
            Log.e(TAG, "🔥 FIREBASE ERROR: ${e.message}", e)
            Result.failure(Exception("Lỗi Firebase: ${e.message}"))

        } catch (e: Exception) {
            Log.e(TAG, "🔥 GENERAL ERROR: ${e.message}", e)
            Log.e(TAG, "🔥 ERROR CLASS: ${e.javaClass.name}")
            Log.e(TAG, "🔥 STACK TRACE:", e)
            Result.failure(Exception("Lỗi không xác định: ${e.javaClass.simpleName} - ${e.message}"))
        }
    }

    private suspend fun getOrCreateUser(firebaseUser: FirebaseUser): User {
        val phoneNumber = firebaseUser.phoneNumber
            ?: throw Exception("Phone number is null")

        Log.d(TAG, "🔥 Getting user from Firestore for: $phoneNumber")

        // Always create a default user first as fallback
        val defaultUser = User(
            phoneNumber = phoneNumber,
            fullName = firebaseUser.displayName ?: "User ${phoneNumber.takeLast(4)}",
            email = firebaseUser.email,
            role = UserRole.INSPECTOR,
            supervisorPhone = null,
            profileImageUrl = firebaseUser.photoUrl?.toString()
        )

        Log.d(TAG, "🔥 Creating default user as fallback: $defaultUser")

        try {
            Log.d(TAG, "🔥 Attempting to connect to Firestore...")

            // Try to get user from Firestore with a timeout
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(phoneNumber)
                .get()
                .await()

            if (userDoc.exists()) {
                Log.d(TAG, "🔥 User found in Firestore")
                val userData = userDoc.data

                val user = User(
                    phoneNumber = userData?.get("phoneNumber") as? String ?: phoneNumber,
                    fullName = userData?.get("fullName") as? String ?: defaultUser.fullName,
                    email = userData?.get("email") as? String,
                    role = try {
                        UserRole.valueOf(userData?.get("role") as? String ?: "INSPECTOR")
                    } catch (e: Exception) {
                        UserRole.INSPECTOR
                    },
                    supervisorPhone = userData?.get("supervisorPhone") as? String,
                    profileImageUrl = userData?.get("profileImageUrl") as? String
                )

                Log.d(TAG, "🔥 Successfully loaded user from Firestore: $user")
                return user

            } else {
                Log.d(TAG, "🔥 User not found in Firestore, saving default user")

                // Try to save new user (don't fail if it doesn't work)
                try {
                    firestore.collection(USERS_COLLECTION)
                        .document(phoneNumber)
                        .set(defaultUser)
                        .await()
                    Log.d(TAG, "🔥 Default user saved to Firestore")
                } catch (saveError: Exception) {
                    Log.w(TAG, "🔥 Could not save to Firestore, continuing anyway: ${saveError.message}")
                }

                return defaultUser
            }

        } catch (e: Exception) {
            Log.w(TAG, "🔥 Firestore error, using default user: ${e.message}")

            // For ANY Firestore error, just return the default user
            Log.i(TAG, "🔥 Proceeding with offline default user")

            // Try to queue the save for later (fire and forget)
            try {
                firestore.collection(USERS_COLLECTION)
                    .document(phoneNumber)
                    .set(defaultUser)
                Log.d(TAG, "🔥 Queued user save for later")
            } catch (queueError: Exception) {
                Log.d(TAG, "🔥 Could not queue save, that's ok")
            }

            return defaultUser
        }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        val phoneNumber = firebaseUser.phoneNumber ?: return null

        return try {
            userRepository.getUserByPhone(phoneNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            null
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            userRepository.clearLocalCache()
            Log.d(TAG, "User signed out successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun updateUserProfile(displayName: String?, email: String?): Result<Unit> {
        val user = firebaseAuth.currentUser ?: return Result.failure(Exception("User not logged in"))

        return try {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .apply {
                    displayName?.let { setDisplayName(it) }
                }
                .build()

            user.updateProfile(profileUpdates).await()
            email?.let { user.updateEmail(it).await() }

            Log.d(TAG, "User profile updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Update profile failed", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val user = firebaseAuth.currentUser ?: return Result.failure(Exception("User not logged in"))

        return try {
            user.delete().await()
            userRepository.clearLocalCache()
            Log.d(TAG, "Account deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Delete account failed", e)
            Result.failure(e)
        }
    }

    override suspend fun refreshUser(): Result<User?> {
        return try {
            val currentUser = getCurrentUser()
            Result.success(currentUser)
        } catch (e: Exception) {
            Log.e(TAG, "Refresh user failed", e)
            Result.failure(e)
        }
    }
}