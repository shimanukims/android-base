package com.example.androidbaseapp.data.util

import com.example.androidbaseapp.domain.model.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * AppErrorをラップする例外クラス
 * Result.failureにAppError情報を保持するために使用
 */
class AppErrorException(val appError: AppError) : Exception()

/**
 * ネットワーク呼び出しを安全に実行し、Resultクラスでラップする拡張関数
 * 
 * 使用目的:
 * - try-catchを使わずに、Kotlin標準のResult APIを活用
 * - 全てのAPI呼び出しを統一的にエラーハンドリング
 * - コルーチンの適切なディスパッチャー（IO）で実行
 * 
 * 使用例:
 * safeApiCall { userApi.getUsers() }
 */
suspend inline fun <T> safeApiCall(
    crossinline apiCall: suspend () -> T
): Result<T> = withContext(Dispatchers.IO) {
    runCatching {
        // API呼び出しを実行し、例外をキャッチ
        apiCall()
    }.onFailure { throwable ->
        // エラーログを記録（デバッグ時に有用）
        Timber.e(throwable, "API call failed")
    }
}

/**
 * Result<T>をResult<R>に変換し、エラーをAppErrorに変換する拡張関数
 * 
 * 使用目的:
 * - API呼び出し成功時のデータ変換処理
 * - 変換処理中のエラーもResult型で管理
 * - Exception → AppError への統一的な変換
 * 
 * 使用例:
 * safeApiCall { userApi.getUsers() }
 *   .mapWithError { userDtos -> userDtos.map { it.toDomain() } }
 */
inline fun <T, R> Result<T>.mapWithError(
    transform: (T) -> R
): Result<R> = this.fold(
    onSuccess = { value ->
        // 成功時は変換処理を実行（変換中のエラーもキャッチ）
        runCatching { transform(value) }
    },
    onFailure = { throwable ->
        // 元のエラーをAppErrorに変換
        val appError = when (throwable) {
            // ネットワーク関連のエラー分類
            is SocketTimeoutException -> AppError.TimeoutError
            is IOException -> AppError.NetworkError
            else -> AppError.UnknownError(throwable.message ?: "Unknown error occurred")
        }
        // AppErrorそのものを例外として渡す（メッセージはUI層で解決）
        Result.failure(AppErrorException(appError))
    }
)

/**
 * ResultのエラーをAppErrorに変換する拡張関数
 * 
 * 使用目的:
 * - 既存のResult型をAppError形式に統一
 * - エラーログの記録
 * 
 * 注意: 現在はmapWithErrorの方が汎用的なため、こちらの使用頻度は低い
 */
fun <T> Result<T>.toAppError(): Result<T> = this.fold(
    onSuccess = { Result.success(it) },
    onFailure = { throwable ->
        val appError = when (throwable) {
            is SocketTimeoutException -> AppError.TimeoutError
            is IOException -> AppError.NetworkError
            else -> AppError.UnknownError(throwable.message ?: "Unknown error occurred")
        }
        Timber.e("Mapped error: $appError")
        Result.failure(AppErrorException(appError))
    }
)