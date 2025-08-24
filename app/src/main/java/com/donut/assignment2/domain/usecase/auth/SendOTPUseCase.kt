package com.donut.assignment2.domain.usecase.auth

import android.app.Activity
import android.util.Log
import com.donut.assignment2.data.repository.OTPResult
import com.donut.assignment2.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendOTPUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "SendOTPUseCase"
    }

    suspend operator fun invoke(phoneNumber: String, activity: Activity): Flow<OTPResult> {
        Log.d(TAG, "Processing phone number: $phoneNumber")

        // Validate and format phone number
        val formattedPhone = try {
            formatPhoneNumber(phoneNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid phone number format: $phoneNumber", e)
            throw IllegalArgumentException("Số điện thoại không hợp lệ: ${e.message}")
        }

        Log.d(TAG, "Formatted phone number: $formattedPhone")
        return authRepository.sendOTP(formattedPhone, activity)
    }

    private fun formatPhoneNumber(phone: String): String {
        // Remove all spaces, dashes, parentheses, dots
        val cleaned = phone.replace(Regex("[\\s\\-\\(\\)\\.]+"), "")

        Log.d(TAG, "Cleaned phone: $cleaned")

        // Validate that it contains only digits and + at the beginning
        if (!cleaned.matches(Regex("^\\+?[0-9]+$"))) {
            throw IllegalArgumentException("Số điện thoại chỉ được chứa số và dấu +")
        }

        // Format for Vietnam
        return when {
            // Already has +84
            cleaned.startsWith("+84") -> {
                if (cleaned.length < 12) {
                    throw IllegalArgumentException("Số điện thoại quá ngắn")
                }
                cleaned
            }
            // Has 84 without +
            cleaned.startsWith("84") -> {
                if (cleaned.length < 11) {
                    throw IllegalArgumentException("Số điện thoại quá ngắn")
                }
                "+$cleaned"
            }
            // Starts with 0 (local format)
            cleaned.startsWith("0") -> {
                if (cleaned.length < 10) {
                    throw IllegalArgumentException("Số điện thoại quá ngắn")
                }
                "+84${cleaned.drop(1)}"
            }
            // No prefix, assume local
            cleaned.length >= 9 -> {
                "+84$cleaned"
            }
            else -> {
                throw IllegalArgumentException("Số điện thoại không đúng định dạng")
            }
        }.also { formatted ->
            Log.d(TAG, "Final formatted phone: $formatted")

            // Final validation
            if (formatted.length < 12 || formatted.length > 15) {
                throw IllegalArgumentException("Độ dài số điện thoại không hợp lệ")
            }
        }
    }
}