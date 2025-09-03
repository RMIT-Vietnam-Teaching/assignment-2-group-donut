package com.phuonghai.inspection.data.repository

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.model.UserRole
import com.phuonghai.inspection.domain.repository.AuthState
import com.phuonghai.inspection.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : IAuthRepository {

    private val _authState = MutableSharedFlow<AuthState>(replay = 1)
    override val authState: SharedFlow<AuthState> = _authState.asSharedFlow()

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        Log.d(TAG, "sendVerificationCode() phone=$phoneNumber")
        _authState.tryEmit(AuthState.Loading)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted(): auto-retrieval -> sign in")
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "onVerificationFailed()", e)
                _authState.tryEmit(AuthState.Error(e.localizedMessage))
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent(): vid=$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                _authState.tryEmit(AuthState.CodeSent(verificationId))
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                Log.w(TAG, "onCodeAutoRetrievalTimeOut(): vid=$verificationId")
                _authState.tryEmit(AuthState.CodeTimeout)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun verifyCode(verificationId: String, code: String) {
        val finalVid = verificationId.ifEmpty { storedVerificationId ?: "" }
        Log.d(TAG, "verifyCode(): vid=$finalVid code=$code")
        if (finalVid.isBlank()) {
            _authState.tryEmit(AuthState.Error("verificationId is blank"))
            return
        }
        _authState.tryEmit(AuthState.Loading)
        val credential = PhoneAuthProvider.getCredential(finalVid, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential(): success uid=${auth.currentUser?.uid}")
                    _authState.tryEmit(AuthState.Success)
                } else {
                    val ex = task.exception
                    Log.e(TAG, "signInWithCredential(): failure", ex)
                    _authState.tryEmit(AuthState.Error(ex?.localizedMessage))
                }
            }
    }

    override suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid
        val projectId = com.google.firebase.FirebaseApp.getInstance().options.projectId
        Log.d("AuthRepository", "getCurrentUser(): uid=$uid projectId=$projectId")
        if (uid == null) return null

        return try {
            val docRef = firestore.collection("users").document(uid)
            val snap = docRef.get().await()
            Log.d("AuthRepository", "getCurrentUser(): path=${docRef.path} exists=${snap.exists()}")

            if (!snap.exists()) {
                Log.w("AuthRepository", "User doc missing → return null")
                return null
            }

            // --- ĐỌC THỦ CÔNG, TRÁNH toObject() LỖI ---
            val fullName = snap.getString("fullName") ?: ""
            val email = snap.getString("email")
            val phone = snap.getString("phoneNumber") ?: ""
            val roleStr = snap.getString("role") ?: "INSPECTOR"
            val role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.INSPECTOR }
            val supervisorId = snap.getString("supervisorId")
            val profileImageUrl = snap.getString("profileImageUrl")

            val user = User(
                uId = uid,
                fullName = fullName,
                email = email,
                phoneNumber = phone,
                role = role,
                supervisorId = supervisorId,
                profileImageUrl = profileImageUrl
            )
            Log.d("AuthRepository", "getCurrentUser(): loaded user=$user")
            user
        } catch (e: Exception) {
            Log.e("AuthRepository", "getCurrentUser(): error", e)
            null
        }
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
