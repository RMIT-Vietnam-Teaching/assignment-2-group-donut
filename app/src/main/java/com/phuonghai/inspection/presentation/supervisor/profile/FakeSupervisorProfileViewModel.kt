package com.phuonghai.inspection.presentation.supervisor.profile

import com.phuonghai.inspection.domain.model.User
import com.phuonghai.inspection.domain.model.UserRole
import com.phuonghai.inspection.domain.repository.SupervisorProfileViewModelContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSupervisorProfileViewModel : SupervisorProfileViewModelContract {
    override val user: StateFlow<User?> = MutableStateFlow(
        User(
            fullName = "Test User",
            email = "test@example.com",
            phoneNumber = "123456",
            role = UserRole.SUPERVISOR,
            profileImageUrl = ""
        )
    )
    override val isLoading: StateFlow<Boolean> = MutableStateFlow(false)
    override val signOutSuccess: StateFlow<Boolean> = MutableStateFlow(false)

    override fun loadUser() {}
    override fun signOut() {}
    override fun clearSignOutSuccess() {}
}