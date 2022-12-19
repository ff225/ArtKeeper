package com.example.artkeeper.data.datasource

import com.example.artkeeper.data.PostDao
import com.example.artkeeper.data.model.Post
import kotlinx.coroutines.flow.Flow


class PostLocalDataSource(private val postDao: PostDao) {
    suspend fun insert(post: Post) {
        postDao.insert(post)
    }

    suspend fun update(post: Post) {
        postDao.update(post)
    }

    suspend fun delete(post: Post) {
        postDao.delete(post)
    }

    suspend fun deleteAll(uid: String) = postDao.deleteAll(uid)

    fun getNumPost(uid: String): Flow<Int> = postDao.getNumPost(uid)

    fun getAllUserPost(uid: String): Flow<List<Post>> = postDao.getUserPosts(uid)
    fun getAllPost(): Flow<List<Post>> = postDao.getPosts()

}