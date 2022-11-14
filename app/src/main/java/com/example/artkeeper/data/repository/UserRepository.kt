package com.example.artkeeper.data.repository

import com.example.artkeeper.data.datasource.UserLocalDataSource
import com.example.artkeeper.data.model.User

class UserRepository(private val userLocalDataSource: UserLocalDataSource) {

    suspend fun insertUser(user: User) = userLocalDataSource.insert(user)
    suspend fun updateUser(user: User) = userLocalDataSource.update(user)
    suspend fun deleteUser(user: User) = userLocalDataSource.delete(user)

    suspend fun getUser(uid: String): User? = userLocalDataSource.getUser(uid)
}