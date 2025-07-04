package com.example.androidbaseapp.presentation.userdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidbaseapp.data.util.AppErrorException
import com.example.androidbaseapp.domain.model.AppError
import com.example.androidbaseapp.domain.model.User
import com.example.androidbaseapp.domain.provider.ErrorMessageProvider
import com.example.androidbaseapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val errorMessageProvider: ErrorMessageProvider
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserDetailUiState())
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()
    
    /**
     * 指定されたユーザーIDのユーザー詳細を取得
     * Result APIを使用してtry-catchを使わない統一的なエラーハンドリング
     */
    fun loadUser(userId: Int) {
        viewModelScope.launch {
            // ローディング状態を設定
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Result APIを使用したエラーハンドリング（try-catchなし）
            userRepository.getUserById(userId)
                .onSuccess { user ->
                    if (user != null) {
                        // ユーザーが見つかった場合
                        _uiState.value = _uiState.value.copy(
                            user = user,
                            isLoading = false
                        )
                        Timber.d("Successfully loaded user: $userId")
                    } else {
                        // ユーザーが存在しない場合（ビジネスエラー）
                        val userNotFoundError = AppError.UnknownError("User not found")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = errorMessageProvider.getErrorMessage(userNotFoundError)
                        )
                        Timber.w("User not found: $userId")
                    }
                }
                .onFailure { throwable ->
                    // AppErrorExceptionからAppErrorを取得してメッセージを生成
                    val appError = if (throwable is AppErrorException) {
                        throwable.appError
                    } else {
                        AppError.UnknownError(throwable.message ?: "Unknown error occurred")
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessageProvider.getErrorMessage(appError)
                    )
                    Timber.e(throwable, "Failed to load user $userId")
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class UserDetailUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)