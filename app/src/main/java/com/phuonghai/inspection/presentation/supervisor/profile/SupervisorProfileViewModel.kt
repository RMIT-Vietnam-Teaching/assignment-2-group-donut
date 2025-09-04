package com.phuonghai.inspection.presentation.supervisor.profile

import androidx.lifecycle.ViewModel
import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.usecase.GetUserInformationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@HiltViewModel
class SupervisorProfileViewModel @Inject constructor(
    private val getUserInformationUseCase: GetUserInformationUseCase
) : ViewModel() {
    val currentUserId = FirebaseAuth.getInstance().uid ?: ""
    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

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
}