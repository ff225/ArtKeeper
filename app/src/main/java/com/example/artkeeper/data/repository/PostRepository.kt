package com.example.artkeeper.data.repository

import com.example.artkeeper.data.datasource.PostLocalDataSource
import com.example.artkeeper.data.datasource.PostRemoteDataSource
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.model.PostToRemote
import kotlinx.coroutines.flow.Flow

class PostRepository(
    private val postLocalDataSource: PostLocalDataSource,
    private val postRemoteDataSource: PostRemoteDataSource
) {

    suspend fun insert(post: Post) = postLocalDataSource.insert(post)

    suspend fun delete(post: Post) = postLocalDataSource.delete(post)

    suspend fun deleteAll() = postLocalDataSource.deleteAll()

    fun getAllUserPost(): Flow<List<Post>> = postLocalDataSource.getAllUserPost()

    suspend fun saveImageRemote(uid: String, imagePath: String) =
        postRemoteDataSource.saveImageRemote(uid, imagePath)

    suspend fun insertRemote(uid: String, post: PostToRemote): Result<Unit> =
        postRemoteDataSource.insertPost(uid, post)

    suspend fun updateRoomWithLatestPost(uid: String) {
        insert(postRemoteDataSource.getLatestPost(uid))
    }

    suspend fun getAllPostRemote(uid: String): Result<List<Post>> {
        val postList = mutableListOf<Post>()
        for (post in postRemoteDataSource.getAllPostRemote(uid).getOrThrow()) {
            postList.add(
                post
            )
        }
        return Result.success(postList)
    }

    suspend fun getAllPostUserRemote(uid: String) {
        for (post in getAllPostRemote(uid).getOrThrow())
            insert(post)
    }

    suspend fun deleteRemote(uid: String, idPostRemote: String, imagePath: String): Result<Unit> =
        postRemoteDataSource.deletePostRemote(uid, idPostRemote, imagePath)

    suspend fun deleteAllPostRemote(uid: String): Result<Unit> =
        postRemoteDataSource.deleteAllRemote(uid)

}