package com.example.artkeeper.data.repository

import android.util.Log
import com.example.artkeeper.data.datasource.UserLocalDataSource
import com.example.artkeeper.data.datasource.UserRemoteDataSource
import com.example.artkeeper.data.model.Nickname
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

    suspend fun insertFollowingRequestLocal(uid: String, followers: List<String>) =
        userLocalDataSource.insertFollowingRequest(uid, followers)

    suspend fun insertFollowingRequestRemote(uid: String, followers: List<String>) =
        userRemoteDataSource.insertFollowingRequest(uid, followers)

    suspend fun insertFollower(uid: String, followers: List<String>) =
        userLocalDataSource.insertFollower(uid, followers)

    suspend fun checkPendingReqLocal(uid: String, followers: List<String>) =
        userLocalDataSource.checkPendingReq(uid, followers)

    suspend fun checkFollower(uid: String, followers: List<String>) =
        userLocalDataSource.checkFollower(uid, followers)

    suspend fun addChildLocal(uid: String, nChild: Int, nameChild: List<String>) =
        userLocalDataSource.addChild(uid, nChild, nameChild)

    fun getUserLocal(uid: String): Flow<User>? =
        userLocalDataSource.getUser(uid)?.distinctUntilChanged()

    suspend fun insertUserRemote(user: User) = userRemoteDataSource.insertUser(user)

    suspend fun checkNicknameRemote(nickName: String): Result<Boolean> =
        userRemoteDataSource.checkNickname(nickName)

    suspend fun checkUserRemote(): Result<Boolean> = userRemoteDataSource.checkUser()

    suspend fun getUserRemote(uid: String): Result<UserOnline> = userRemoteDataSource.getUser(uid)

    suspend fun deleteUserRemote(): Result<Boolean> = userRemoteDataSource.deleteUser()

    suspend fun addChildRemote(nChild: Int, nameChild: List<String>) =
        userRemoteDataSource.addChild(nChild, nameChild)

    suspend fun insertNicknames() {
        Log.d("UserRepository", "insert nicknames...")
        for (nick in userRemoteDataSource.getAllNicknames().getOrThrow()) {
            userLocalDataSource.insertNickname(nick)
        }
    }

    private suspend fun getAllNickname(): List<Nickname> {
        return userLocalDataSource.getAllNickname()
    }

    suspend fun deleteNicknames() {
        for (nick in getAllNickname()) {
            userLocalDataSource.deleteNickname(nick)
        }
    }

    fun getNicknames(queryString: String): Flow<List<Nickname>> =
        userLocalDataSource.getNicknames(queryString)
}