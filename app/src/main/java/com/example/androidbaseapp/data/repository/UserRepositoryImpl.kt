package com.example.androidbaseapp.data.repository

import com.example.androidbaseapp.data.local.dao.UserDao
import com.example.androidbaseapp.data.local.entity.toDomain
import com.example.androidbaseapp.data.local.entity.toEntity
import com.example.androidbaseapp.data.remote.api.UserApi
import com.example.androidbaseapp.data.remote.dto.toDomain
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
        return try {
            Timber.d("Fetching users from API")
            val userDtos = userApi.getUsers()
            val users = userDtos.map { it.toDomain() }
            val entities = users.map { it.toEntity() }
            
            userDao.deleteAllUsers()
            userDao.insertUsers(entities)
            
            Timber.d("Successfully cached ${users.size} users")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh users")
            val appError = when (e) {
                is SocketTimeoutException -> AppError.TimeoutError
                is IOException -> AppError.NetworkError
                else -> AppError.UnknownError(e.message ?: "Unknown error occurred")
            }
            Result.failure(Exception(appError.getErrorMessage()))
        }
    }
    
    override suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)?.toDomain()
    }
    
    private suspend fun isCacheExpired(): Boolean {
        val lastUpdated = userDao.getLastUpdatedTime() ?: return true
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastUpdated) > CACHE_EXPIRY_MS
    }
}