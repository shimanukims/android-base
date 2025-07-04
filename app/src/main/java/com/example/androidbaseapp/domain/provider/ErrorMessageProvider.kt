package com.example.androidbaseapp.domain.provider

import com.example.androidbaseapp.domain.model.AppError

/**
 * エラーメッセージの取得を抽象化するProvider Interface
 * 
 * 設計方針:
 * - Domain層ではPlatform固有の依存を避けるため、インターフェースのみ定義
 * - 実装はPresentation層（AndroidErrorMessageProvider）で行う
 * - Clean Architectureの依存関係ルールを遵守
 * - DIパターンで依存性を注入
 * 
 * 利点:
 * - Domain層がAndroid固有のリソースに依存しない
 * - テスト時にMock実装を簡単に差し替え可能
 * - エラーメッセージの一元管理
 */
interface ErrorMessageProvider {
    /**
     * AppErrorに対応するエラーメッセージを取得
     * 
     * @param error AppErrorインスタンス
     * @return エラーメッセージ
     */
    fun getErrorMessage(error: AppError): String
}