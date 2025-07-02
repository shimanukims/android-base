package com.example.androidbaseapp.domain.repository

import com.example.androidbaseapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsers(): Flow<List<User>>
    suspend fun refreshUsers(): Result<Unit>
    suspend fun getUserById(id: Int): User?
}