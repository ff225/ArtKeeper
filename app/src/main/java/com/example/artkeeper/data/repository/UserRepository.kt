package com.example.artkeeper.data.repository

import com.example.artkeeper.data.datasource.UserLocalDataSource
import com.example.artkeeper.data.datasource.UserRemoteDataSource
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.model.UserOnline
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class UserRepository(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource
) {

    suspend fun insertUserLocal(user: User) = userLocalDataSource.insert(user)

    suspend fun updateUserLocal(user: User) = userLocalDataSource.update(user)

    suspend fun deleteUserLocal(user: User) = userLocalDataSource.delete(user)

    suspend fun addChildLocal(uid: String, nChild: Int, nameChild: List<String>) =
        userLocalDataSource.addChild(uid, nChild, nameChild)

    fun getUserLocal(uid: String): Flow<User> =
        userLocalDataSource.getUser(uid).distinctUntilChanged()

    suspend fun insertUserRemote(user: User) = userRemoteDataSource.insertUser(user)

    suspend fun checkNicknameRemote(nickName: String): Result<Boolean> =
        userRemoteDataSource.checkNickname(nickName)

    suspend fun checkUserRemote(): Result<Boolean> = userRemoteDataSource.checkUser()

    suspend fun getUserOnline(): Result<UserOnline> = userRemoteDataSource.getUser()

    suspend fun deleteUserRemote(): Result<Boolean> = userRemoteDataSource.deleteUser()

    suspend fun addChildRemote(nChild: Int, nameChild: List<String>) =
        userRemoteDataSource.addSon(nChild, nameChild)
}