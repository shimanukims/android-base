package com.example.androidbaseapp.presentation.provider

import android.content.Context
import com.example.androidbaseapp.R
import com.example.androidbaseapp.domain.model.AppError
import com.example.androidbaseapp.domain.provider.ErrorMessageProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ErrorMessageProviderのAndroid実装
 * 
 * 責務:
 * - AppErrorをAndroidのString Resourcesを使ってエラーメッセージに変換
 * - Clean ArchitectureでPresentation層に配置（Android固有の実装）
 * - ApplicationContextを使用してメモリリークを防止
 * 
 * 設計パターン:
 * - Dependency Inversion: Domain層のインターフェースを実装
 * - Singleton: アプリ全体で単一インスタンスを使用
 * - Context Injection: Hiltによる依存性注入
 */
@Singleton
class AndroidErrorMessageProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : ErrorMessageProvider {
    
    /**
     * AppErrorに対応するエラーメッセージを取得
     * 
     * 実装方針:
     * - 各AppErrorタイプを適切なString Resourceにマッピング
     * - UnknownErrorは動的メッセージと固定メッセージの併用
     * - String Resourcesによる一元管理
     */
    override fun getErrorMessage(error: AppError): String {
        return when (error) {
            is AppError.NetworkError -> context.getString(R.string.error_network)
            is AppError.TimeoutError -> context.getString(R.string.error_timeout)
            is AppError.OfflineError -> context.getString(R.string.error_offline)
            is AppError.UnknownError -> {
                // UnknownErrorは詳細メッセージがある場合はそれを使用、なければデフォルト
                if (error.message.isNotEmpty()) {
                    error.message
                } else {
                    context.getString(R.string.error_unknown)
                }
            }
        }
    }
}