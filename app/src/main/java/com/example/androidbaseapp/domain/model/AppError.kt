package com.example.androidbaseapp.domain.model

/**
 * アプリケーション全体で使用するエラー定義
 * 各エラーは特定の原因と対処法を持つ
 */
sealed class AppError {
    /**
     * ネットワークエラー
     * 発生原因: インターネット接続なし、DNSエラー、サーバー接続不可等
     * 対処法: 接続確認後リトライ可能
     */
    object NetworkError : AppError()
    
    /**
     * タイムアウトエラー
     * 発生原因: API応答時間が設定値を超過、サーバー負荷高等
     * 対処法: 時間をおいてリトライ可能
     */
    object TimeoutError : AppError()
    
    /**
     * オフラインエラー
     * 発生原因: 端末がオフライン状態、機内モード等
     * 対処法: ネットワーク設定確認が必要、自動リトライ不適切
     */
    object OfflineError : AppError()
    
    /**
     * 不明なエラー
     * 発生原因: 予期しない例外、未定義のエラー状況
     * 対処法: ログ確認とエラー報告が必要、リトライは状況により判断
     */
    data class UnknownError(val message: String) : AppError()
    
    /**
     * エラーメッセージの取得はErrorMessageProviderを使用してください
     * このメソッドは後方互換性のために残されています
     * 
     * @deprecated ErrorMessageProviderを使用してください
     */
    @Deprecated(
        message = "Use ErrorMessageProvider.getErrorMessage(error) instead",
        replaceWith = ReplaceWith("errorMessageProvider.getErrorMessage(this)")
    )
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