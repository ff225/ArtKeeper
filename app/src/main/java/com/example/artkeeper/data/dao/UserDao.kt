package com.example.artkeeper.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.artkeeper.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao : BaseDao<User> {

    @Query("UPDATE user SET num_child=:nChild, name_child=:nameChild WHERE uid=:uid")
    abstract suspend fun addChild(uid: String, nChild: Int, nameChild: List<String>)

    @Query("SELECT count(*) FROM user WHERE uid= :uid")
    abstract suspend fun checkUser(uid: String): Boolean

    @Query("SELECT * FROM user WHERE uid=:uid")
    abstract fun getUser(uid: String): Flow<User>?


}