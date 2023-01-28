package com.example.artkeeper.data.datasource

import com.example.artkeeper.data.PostDao
import com.example.artkeeper.data.model.Post
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


class PostLocalDataSource(
    private val postDao: PostDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun insert(post: Post) = withContext(dispatcher) {
        postDao.insert(post)
    }

    suspend fun update(post: Post) {
        postDao.update(post)
    }

    suspend fun checkTableExist(): Int = withContext(dispatcher) {
        async { postDao.checkTableExist() }
    }.await()

    suspend fun delete(post: Post) = withContext(dispatcher) {
        postDao.delete(post)
    }

    suspend fun deleteAll() =
        withContext(dispatcher) {
            async {
                postDao.deleteAll()
            }
        }.await()

    fun getNumPost(): Flow<Int> = postDao.getNumPost()

    fun getAllUserPost(): Flow<List<Post>> = postDao.getUserPosts()

}