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

    suspend fun updateNicknamePost(nickName: String, uid: String) = withContext(dispatcher) {
        postDao.updateNicknamePost(nickName, uid)
    }

    suspend fun update(post: Post) {
        postDao.update(post)
    }

    suspend fun delete(post: Post) = withContext(dispatcher) {
        postDao.delete(post)
    }

    suspend fun deleteAll(uid: String) =
        withContext(dispatcher) {
            async {
                postDao.deleteAll(
                    uid
                )
            }
        }.await()

    fun getNumPost(uid: String): Flow<Int> = postDao.getNumPost(uid)

    fun getAllUserPost(uid: String): Flow<List<Post>> = postDao.getUserPosts(uid)
    fun getAllPost(): Flow<List<Post>> = postDao.getPosts()

}