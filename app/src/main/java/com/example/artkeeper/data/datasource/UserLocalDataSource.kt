package com.example.artkeeper.data.datasource

import com.example.artkeeper.data.UserDao
import com.example.artkeeper.data.model.Nickname
import com.example.artkeeper.data.model.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserLocalDataSource(
    private val userDao: UserDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun insert(user: User) {
        withContext(dispatcher) { async { userDao.insert(user) } }.await()
    }

    suspend fun update(user: User) =
        withContext(dispatcher) { async { userDao.update(user) } }.await()

    suspend fun delete(user: User) =
        withContext(Dispatchers.Main) {
            userDao.delete(user)
        }

    suspend fun insertFollowingRequest(uid: String, followers: List<String>) =
        withContext(dispatcher) {
            userDao.insertFollowingRequest(uid, followers)
        }

    suspend fun insertFollower(uid: String, followers: List<String>) =
        withContext(dispatcher) {
            userDao.insertFollower(uid, followers)
        }

    suspend fun checkPendingReq(uid: String, followers: List<String>) =
        withContext(dispatcher) {
            userDao.checkFollowingRequest(uid, followers)
        }

    suspend fun checkFollower(uid: String, followers: List<String>) =
        withContext(dispatcher) {
            userDao.checkFollowingRequest(uid, followers)
        }

    suspend fun addChild(uid: String, nChild: Int, nameChild: List<String>) =
        withContext(dispatcher) { userDao.addChild(uid, nChild, nameChild) }

    suspend fun insertNickname(nickname: Nickname) =
        withContext(dispatcher) { userDao.insertNickname(nickname) }

    suspend fun deleteNickname(nickname: Nickname) =
        withContext((dispatcher)) { userDao.deleteNickname(nickname) }

    suspend fun getAllNickname(): List<Nickname> = withContext(dispatcher) {
        userDao.getAllNickname()
    }

    fun getUser(uid: String): Flow<User>? = userDao.getUser(uid)

    fun getNicknames(queryString: String): Flow<List<Nickname>> = userDao.getNicknames(queryString)

}