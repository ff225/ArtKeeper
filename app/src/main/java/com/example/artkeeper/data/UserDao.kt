package com.example.artkeeper.data

import androidx.room.*
import com.example.artkeeper.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT EXISTS (SELECT * FROM users WHERE nickname=:nickname)")
    suspend fun checkNickname(nickname: String): Boolean

    @Query("UPDATE users SET num_child=:nChild, name_child=:nameChild WHERE uid=:uid")
    suspend fun addChild(uid: String, nChild: Int, nameChild: List<String>)

    @Query("SELECT count(*) FROM users WHERE uid= :uid")
    suspend fun checkUser(uid: String): Boolean

    @Query("SELECT * FROM users WHERE uid=:uid")
    fun getUser(uid: String): Flow<User>
}