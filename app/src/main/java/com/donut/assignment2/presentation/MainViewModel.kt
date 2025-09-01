package com.donut.assignment2.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donut.assignment2.domain.model.UserRole
import com.donut.assignment2.domain.usecase.inspector.GetUserByPhoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserByPhone: GetUserByPhoneUseCase
) : ViewModel() {

    private val _role = MutableStateFlow<UserRole?>(null)
    val role = _role.asStateFlow()

    fun loadUser(phoneNumber: String) {
        viewModelScope.launch {
            val user = getUserByPhone(phoneNumber)
            _role.value = user?.role
        }
    }
}