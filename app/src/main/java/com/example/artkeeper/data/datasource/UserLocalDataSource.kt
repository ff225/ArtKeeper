package com.example.artkeeper.data.datasource

import com.example.artkeeper.data.dao.UserDao
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

    suspend fun addChild(uid: String, nChild: Int, nameChild: List<String>) =
        withContext(dispatcher) { userDao.addChild(uid, nChild, nameChild) }

    fun getUser(uid: String): Flow<User>? = userDao.getUser(uid)


}