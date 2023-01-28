package com.example.artkeeper.data

import androidx.room.Dao
import androidx.room.Query
import com.example.artkeeper.data.model.Post
import kotlinx.coroutines.flow.Flow


@Dao
abstract class PostDao : BaseDao<Post> {

    @Query("DELETE FROM post_user")
    abstract suspend fun deleteAll()


    @Query("SELECT COUNT(id_post) FROM post_user")
    abstract fun checkTableExist(): Int


    @Query("SELECT * FROM post_user ORDER by post_timestamp DESC")
    abstract fun getUserPosts(): Flow<List<Post>>

    @Query("SELECT COUNT(id_post) FROM post_user")
    abstract fun getNumPost(): Flow<Int>
}