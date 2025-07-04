package com.example.androidbaseapp.di

import com.example.androidbaseapp.domain.provider.ErrorMessageProvider
import com.example.androidbaseapp.presentation.provider.AndroidErrorMessageProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Provider関連のDependency Injection設定
 * 
 * 設計方針:
 * - インターフェースと実装の結合をDIで管理
 * - Clean Architectureの依存関係ルールを維持
 * - SingletonComponentでアプリ全体にProviderを提供
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {
    
    /**
     * ErrorMessageProviderの実装をバインド
     * Domain層のインターフェースにPresentation層の実装を注入
     */
    @Binds
    abstract fun bindErrorMessageProvider(
        androidErrorMessageProvider: AndroidErrorMessageProvider
    ): ErrorMessageProvider
}