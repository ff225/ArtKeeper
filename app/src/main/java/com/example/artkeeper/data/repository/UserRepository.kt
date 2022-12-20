package com.example.artkeeper.data.repository

import com.example.artkeeper.data.datasource.UserLocalDataSource
import com.example.artkeeper.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userLocalDataSource: UserLocalDataSource
) {

    suspend fun insertUser(user: User) = userLocalDataSource.insert(user)

    suspend fun checkNickname(nickName: String): Boolean =
        userLocalDataSource.checkNickname(nickName)

    suspend fun updateUser(user: User) = userLocalDataSource.update(user)
    suspend fun deleteUser(user: User) = userLocalDataSource.delete(user)
    suspend fun addChild(uid: String, nChild: Int, nameChild: List<String>) =
        userLocalDataSource.addChild(uid, nChild, nameChild)

    fun getUser(uid: String): Flow<User> = userLocalDataSource.getUser(uid)
    fun checkUser(uid: String): Boolean = userLocalDataSource.checkUser(uid)
}