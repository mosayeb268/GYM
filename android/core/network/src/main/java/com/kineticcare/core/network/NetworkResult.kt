package com.kineticcare.core.network

/**
 * ساختار بازگشتی تماس‌های شبکه به همراه پیام‌های خطای بومی فارسی
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val messageFa: String, val statusCode: Int? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    fun getOrNull(): T? = (this as? Success)?.data
}
