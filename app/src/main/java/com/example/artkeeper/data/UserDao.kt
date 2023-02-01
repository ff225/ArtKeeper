package com.example.artkeeper.data

import androidx.room.*
import com.example.artkeeper.data.model.Nickname
import com.example.artkeeper.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao : BaseDao<User> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertNickname(nickname: Nickname)

    @Delete
    abstract suspend fun deleteNickname(nickname: Nickname)

    @Query("SELECT * FROM nickname_users")
    abstract suspend fun getAllNickname(): List<Nickname>

    @Query("SELECT EXISTS (SELECT * FROM users WHERE nickname=:nickname)")
    abstract suspend fun checkNickname(nickname: String): Boolean

    @Query("UPDATE users SET num_child=:nChild, name_child=:nameChild WHERE uid=:uid")
    abstract suspend fun addChild(uid: String, nChild: Int, nameChild: List<String>)

    @Query("SELECT count(*) FROM users WHERE uid= :uid")
    abstract suspend fun checkUser(uid: String): Boolean

    @Query("SELECT * FROM users WHERE uid=:uid")
    abstract fun getUser(uid: String): Flow<User>

    @Query("SELECT * FROM nickname_users WHERE nickname LIKE '%' ||:queryString")
    abstract fun getNicknames(queryString: String): Flow<List<Nickname>>


}