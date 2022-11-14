package com.example.artkeeper.data

import androidx.room.*
import com.example.artkeeper.data.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    /*
    @Delete
    suspend fun delete(uid: String)
*/
    @Query("SELECT * FROM users WHERE uid=:uid")
    suspend fun getUser(uid: String): User
}