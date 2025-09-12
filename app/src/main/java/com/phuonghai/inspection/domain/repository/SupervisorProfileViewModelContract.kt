package com.phuonghai.inspection.domain.repository

import com.phuonghai.inspection.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface SupervisorProfileViewModelContract {
    val user: StateFlow<User?>
    val isLoading: StateFlow<Boolean>
    val signOutSuccess: StateFlow<Boolean>

    fun loadUser()
    fun signOut()
    fun clearSignOutSuccess()
}