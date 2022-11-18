package com.example.artkeeper.data.datasource

import com.example.artkeeper.data.UserDao
import com.example.artkeeper.data.model.User
import com.example.artkeeper.utils.Resource
import kotlinx.coroutines.flow.Flow

class UserLocalDataSource(private val userDao: UserDao) {

    suspend fun insert(user: User) = userDao.insert(user)
    suspend fun update(user: User) = userDao.update(user)
    suspend fun delete(user: User) = userDao.delete(user)

    suspend fun getUser(uid: String): User? = userDao.getUser(uid)

}