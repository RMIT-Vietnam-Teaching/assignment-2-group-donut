package com.phuonghai.inspection.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phuonghai.inspection.domain.model.UserRole
import com.phuonghai.inspection.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    fun loadUserRole(phoneNumber: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                _userRole.value = user?.role
            } catch (e: Exception) {
                _userRole.value = null
            }
        }
    }
}