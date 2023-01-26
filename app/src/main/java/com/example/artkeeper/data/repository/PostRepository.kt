package com.example.artkeeper.data.repository

import com.example.artkeeper.data.datasource.PostLocalDataSource
import com.example.artkeeper.data.datasource.PostRemoteDataSource
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.model.PostRemote
import com.example.artkeeper.utils.Resource
import kotlinx.coroutines.flow.Flow

class PostRepository(
    private val postLocalDataSource: PostLocalDataSource,
    private val postRemoteDataSource: PostRemoteDataSource
) {

    suspend fun insert(post: Post) = postLocalDataSource.insert(post)
    suspend fun delete(post: Post) = postLocalDataSource.delete(post)
    suspend fun deleteAll(uid: String) = postLocalDataSource.deleteAll(uid)
    suspend fun updateNicknamePost(nickName: String, uid: String) =
        postLocalDataSource.updateNicknamePost(nickName, uid)

    fun getAllPost(): Flow<List<Post>> = postLocalDataSource.getAllPost()

    fun getAllUserPost(uid: String): Flow<List<Post>> = postLocalDataSource.getAllUserPost(uid)

    fun getNumPost(uid: String): Flow<Int> = postLocalDataSource.getNumPost(uid)

    suspend fun saveImageRemote(uid: String, imagePath: String) =
        postRemoteDataSource.saveImageRemote(uid, imagePath)

    suspend fun insertRemote(uid: String, post: PostRemote): Result<Unit> =
        postRemoteDataSource.insertPost(uid, post)

    suspend fun getAllPostRemote(uid: String): Resource<List<PostRemote>> =
        postRemoteDataSource.getAllPostRemote(uid)

    suspend fun deleteRemote(uid: String, timestamp: String): Result<Unit> =
        postRemoteDataSource.deletePostRemote(uid, timestamp)

    suspend fun deleteAllPostRemote(uid: String): Result<Unit> =
        postRemoteDataSource.deleteAllRemote(uid)

}