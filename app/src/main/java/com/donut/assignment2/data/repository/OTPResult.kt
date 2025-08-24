package com.donut.assignment2.data.repository

import java.lang.Exception

sealed class OTPResult {
    // Gửi mã thành công, trả về verificationId
    data class Success(val verificationId: String) : OTPResult()

    // Xảy ra lỗi
    data class Error(val exception: Exception) : OTPResult()

    // Tự động xác thực thành công (không cần nhập mã)
    object AutoVerified : OTPResult()
}