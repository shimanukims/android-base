package com.example.androidbaseapp.data.repository

import com.example.androidbaseapp.data.local.dao.UserDao
import com.example.androidbaseapp.data.local.entity.toDomain
import com.example.androidbaseapp.data.local.entity.toEntity
import com.example.androidbaseapp.data.remote.api.UserApi
import com.example.androidbaseapp.data.remote.dto.toDomain
import com.example.androidbaseapp.data.util.safeApiCall
import com.example.androidbaseapp.data.util.mapWithError
import com.example.androidbaseapp.data.util.AppErrorException
import com.example.androidbaseapp.domain.model.AppError
import com.example.androidbaseapp.domain.model.User
import com.example.androidbaseapp.domain.repository.UserRepository
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao
) : UserRepository {
    
    companion object {
        private val CACHE_EXPIRY_DURATION = Duration.ofHours(24)
        private val CACHE_EXPIRY_MS = CACHE_EXPIRY_DURATION.toMillis()
    }
    
    override fun getUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun refreshUsers(): Result<Unit> {
        Timber.d("Fetching users from API")
        
        // Result APIを使用してエラーハンドリング
        return safeApiCall { userApi.getUsers() }
            .mapWithError { userDtos ->
                // APIレスポンスをドメインモデルに変換
                val users = userDtos.map { it.toDomain() }
                val entities = users.map { it.toEntity() }
                
                // データベースの更新
                userDao.deleteAllUsers()
                userDao.insertUsers(entities)
                
                Timber.d("Successfully cached ${users.size} users")
                Unit
            }
    }
    
    override suspend fun getUserById(id: Int): Result<User?> {
        return runCatching {
            // ローカルDBからユーザーを取得
            val userEntity = userDao.getUserById(id)
            userEntity?.toDomain()
        }.fold(
            onSuccess = { user -> Result.success(user) },
            onFailure = { throwable ->
                Timber.e(throwable, "Failed to get user by id: $id")
                val appError = AppError.UnknownError("Failed to get user")
                Result.failure(AppErrorException(appError))
            }
        )
    }
    
    private suspend fun isCacheExpired(): Boolean {
        val lastUpdated = userDao.getLastUpdatedTime() ?: return true
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastUpdated) > CACHE_EXPIRY_MS
    }
}