package com.phuonghai.inspection.presentation.home.inspector.profile

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
import kotlinx.coroutines.launch

@HiltViewModel
class InspectorProfileViewModel @Inject constructor(
    private val getUserInformationUseCase: GetUserInformationUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "InspectorProfileVM"
    }

    val currentUserId = FirebaseAuth.getInstance().uid ?: ""

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // ✅ ADD SIGN OUT SUCCESS STATE
    private val _signOutSuccess = MutableStateFlow(false)
    val signOutSuccess = _signOutSuccess.asStateFlow()

    fun loadUser() {
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

    fun signOut() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting sign out process...")

                // Call the use case
                signOutUseCase()

                // Clear local state
                _user.value = null

                // ✅ SET SUCCESS FLAG FOR UI TO HANDLE NAVIGATION
                _signOutSuccess.value = true

                Log.d(TAG, "Sign out completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Exception during sign out", e)
            }
        }
    }

    // ✅ RESET SIGN OUT SUCCESS FLAG
    fun clearSignOutSuccess() {
        _signOutSuccess.value = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ProfileViewModel cleared")
    }
}