package com.phuonghai.inspection.presentation.supervisor.profile

import androidx.lifecycle.ViewModel
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.usecase.GetUserInformationUseCase
import com.phuonghai.inspection.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.phuonghai.inspection.domain.repository.SupervisorProfileViewModelContract
import kotlinx.coroutines.launch


@HiltViewModel
class SupervisorProfileViewModel @Inject constructor(
    private val getUserInformationUseCase: GetUserInformationUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel(), SupervisorProfileViewModelContract {

    companion object { private const val TAG = "SupervisorProfileVM" }

    val currentUserId = FirebaseAuth.getInstance().uid ?: ""

    private val _user = MutableStateFlow<User?>(null)
    override val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading = _isLoading.asStateFlow()

    // ✅ NEW
    private val _signOutSuccess = MutableStateFlow(false)
    override val signOutSuccess = _signOutSuccess.asStateFlow()

    override fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getUserInformationUseCase(currentUserId)
                _user.value = result.getOrNull()
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun signOut() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting sign out process...")
                signOutUseCase()
                _user.value = null
                _signOutSuccess.value = true   // ✅ báo UI
                Log.d(TAG, "Sign out completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Exception during sign out", e)
            }
        }
    }

    // ✅ NEW
    override fun clearSignOutSuccess() { _signOutSuccess.value = false }
}