package com.example.artkeeper.data

import androidx.room.Dao
import androidx.room.Query
import com.example.artkeeper.data.model.Post
import kotlinx.coroutines.flow.Flow


@Dao
abstract class PostDao : BaseDao<Post> {


    @Query("DELETE FROM post where uid_user =:uid")
    abstract suspend fun deleteAll(uid: String)

    @Query("SELECT * FROM post WHERE id = :id")
    abstract fun getPost(id: Int): Flow<Post>

    @Query("SELECT * FROM post ORDER BY post_timestamp DESC")
    abstract fun getPosts(): Flow<List<Post>>


    @Query("SELECT * FROM post WHERE uid_user = :uid ORDER by post_timestamp DESC")
    abstract fun getUserPosts(uid: String): Flow<List<Post>>

    // imposto a flow perché deve aggiornarsi ad ogni post caricato
    @Query("SELECT COUNT(id) FROM post WHERE uid_user =:uid")
    abstract fun getNumPost(uid: String): Flow<Int>
}