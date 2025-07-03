package com.example.androidbaseapp.presentation.userlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidbaseapp.data.repository.UserRepositoryImpl
import com.example.androidbaseapp.domain.model.AppError
import com.example.androidbaseapp.domain.model.User
import com.example.androidbaseapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _canRetry = MutableStateFlow(false)
    val canRetry: StateFlow<Boolean> = _canRetry.asStateFlow()
    
    val users: StateFlow<List<User>> = userRepository.getUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val uiState: StateFlow<UserListUiState> = combine(
        users,
        isLoading,
        isRefreshing,
        errorMessage,
        canRetry
    ) { users, loading, refreshing, error, retry ->
        UserListUiState(
            users = users,
            isLoading = loading,
            isRefreshing = refreshing,
            errorMessage = error,
            canRetry = retry,
            isEmpty = users.isEmpty() && !loading && !refreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserListUiState()
    )
    
    init {
        loadUsers()
    }
    
    fun loadUsers() {
        loadUsersInternal(isRefresh = false)
    }
    
    fun refresh() {
        if (_isRefreshing.value) return // Ignore if already refreshing
        loadUsersInternal(isRefresh = true)
    }
    
    private fun loadUsersInternal(isRefresh: Boolean) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _isLoading.value = true
            }
            _errorMessage.value = null
            
            try {
                // Always refresh on app start as per requirements
                val result = userRepository.refreshUsers()
                result.onFailure { throwable ->
                    _errorMessage.value = throwable.message
                    // エラーメッセージの内容からリトライ可能性を判断
                    _canRetry.value = throwable.message?.let { msg ->
                        msg.contains("ネットワークエラー") || msg.contains("タイムアウト")
                    } ?: false
                    Timber.e(throwable, if (isRefresh) "Failed to refresh users" else "Failed to load users")
                }
            } finally {
                if (isRefresh) {
                    _isRefreshing.value = false
                } else {
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
        _canRetry.value = false
    }
}

data class UserListUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val canRetry: Boolean = false,
    val isEmpty: Boolean = false
)