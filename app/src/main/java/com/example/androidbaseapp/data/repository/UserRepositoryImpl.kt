package com.example.androidbaseapp.data.repository

import com.example.androidbaseapp.data.local.dao.UserDao
import com.example.androidbaseapp.data.local.entity.toDomain
import com.example.androidbaseapp.data.local.entity.toEntity
import com.example.androidbaseapp.data.remote.api.UserApi
import com.example.androidbaseapp.data.remote.dto.toDomain
import com.example.androidbaseapp.domain.model.User
import com.example.androidbaseapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao
) : UserRepository {
    
    companion object {
        private const val CACHE_EXPIRY_HOURS = 24
        private const val CACHE_EXPIRY_MS = CACHE_EXPIRY_HOURS * 60 * 60 * 1000L
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
            Result.failure(e)
        }
    }
    
    override suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)?.toDomain()
    }
    
    suspend fun isCacheExpired(): Boolean {
        val lastUpdated = userDao.getLastUpdatedTime() ?: return true
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastUpdated) > CACHE_EXPIRY_MS
    }
}