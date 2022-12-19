package com.example.artkeeper.data.datasource

import com.example.artkeeper.data.UserDao
import com.example.artkeeper.data.model.User
import kotlinx.coroutines.flow.Flow

class UserLocalDataSource(private val userDao: UserDao) {

    suspend fun insert(user: User) = userDao.insert(user)
    suspend fun update(user: User) = userDao.update(user)
    suspend fun delete(user: User) = userDao.delete(user)
    suspend fun addChild(uid: String, nChild: Int, nameChild: List<String>) =
        userDao.addChild(uid, nChild, nameChild)

    fun getUser(uid: String): Flow<User> = userDao.getUser(uid)
    fun checkUser(uid: String): Boolean = userDao.checkUser(uid)

}