package com.example.androidbaseapp.domain.model

sealed class AppError {
    object NetworkError : AppError()
    object TimeoutError : AppError()
    object OfflineError : AppError()
    data class UnknownError(val message: String) : AppError()
    
    fun getErrorMessage(): String {
        return when (this) {
            is NetworkError -> "ネットワークエラーが発生しました"
            is TimeoutError -> "接続がタイムアウトしました"
            is OfflineError -> "オフラインです"
            is UnknownError -> message.ifEmpty { "不明なエラーが発生しました" }
        }
    }
    
    fun canRetry(): Boolean {
        return when (this) {
            is NetworkError, is TimeoutError -> true
            is OfflineError, is UnknownError -> false
        }
    }
}