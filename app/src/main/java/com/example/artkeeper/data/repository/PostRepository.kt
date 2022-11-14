package com.example.artkeeper.data.repository

import com.example.artkeeper.data.datasource.PostLocalDataSource
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.utils.Resource
import kotlinx.coroutines.flow.Flow

class PostRepository(private val postLocalDataSource: PostLocalDataSource) {

    suspend fun insert(post: Post) = postLocalDataSource.insert(post)

    fun getAllPost(): Flow<List<Post>> = postLocalDataSource.getAllPost()

    fun getAllUserPost(uid: String): Flow<List<Post>> = postLocalDataSource.getAllUserPost(uid)

    fun getNumPost(uid: String): Flow<Int> = postLocalDataSource.getNumPost(uid)
}