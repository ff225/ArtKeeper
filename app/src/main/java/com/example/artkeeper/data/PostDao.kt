package com.example.artkeeper.data

import androidx.room.*
import com.example.artkeeper.data.model.Post
import kotlinx.coroutines.flow.Flow


@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(post: Post)

    @Update
    suspend fun update(post: Post)

    @Delete
    suspend fun delete(post: Post)

    @Query("SELECT * FROM post WHERE id = :id")
    fun getPost(id: Int): Flow<Post>

    @Query("SELECT * FROM post ORDER BY post_timestamp DESC")
    fun getPosts(): Flow<List<Post>>

    @Query("SELECT * FROM post WHERE nickname = :nickname ORDER by post_timestamp DESC")
    fun getUserPosts(nickname: String): Flow<List<Post>>

    //TODO imposto a flow perché deve aggiornarsi ad ogni post caricato
    @Query("SELECT COUNT(id) FROM post WHERE nickname =:nickname")
    fun getNumPost(nickname: String): Flow<Int>
}