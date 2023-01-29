package com.example.artkeeper.data.datasource

import android.net.Uri
import android.util.Log
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.model.PostFromRemote
import com.example.artkeeper.data.model.PostToRemote
import com.example.artkeeper.utils.Constants.databaseRef
import com.google.firebase.database.DatabaseException
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileNotFoundException

class PostRemoteDataSource(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val TAG = javaClass.simpleName

    private val dbPost = databaseRef.getReference("post")
    private val storageRef = Firebase.storage.reference

    suspend fun saveImageRemote(uid: String, imagePath: String): Result<Unit> {

        val imageName = imagePath.split("/").let {
            it[it.lastIndex]
        }
        val imageRef = "images/$uid/$imageName"
        Log.d("$TAG, saveImageRemote", imageRef)
        return try {
            storageRef.child(imageRef).putFile(Uri.fromFile(File(imagePath)))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d(TAG, "in saveImageRemote exception ${e.message.toString()}")
            when (e) {
                is FileNotFoundException -> Result.failure(e.cause!!)
                is StorageException -> Result.failure(e.cause!!)
                else -> Result.failure(e.cause!!)
            }
        }
    }

    suspend fun insertPost(uid: String, post: PostToRemote): Result<Unit> {
        return try {
            dbPost.child(uid).push().setValue(post).await()
            Result.success(Unit)
        } catch (e: Exception) {
            when (e) {
                is DatabaseException -> Result.failure(e.cause!!)
                else -> Result.failure(Throwable("Impossibile salvare il post..."))
            }

        }

    }


    suspend fun getAllPostRemote(uid: String): Result<List<PostFromRemote>> {

        val postList = mutableListOf<PostFromRemote>()
        val posts = dbPost.child(uid).get().await()
        for (post in posts.children) {
            post.getValue(PostFromRemote::class.java).let {
                it?.imagePath =
                    storageRef.child("images/$uid/${it?.imagePath}").downloadUrl.await()
                        .toString()

                postList.add(
                    PostFromRemote(
                        post.key.toString(),
                        it!!.imagePath,
                        it.sketchedBy,
                        it.description,
                        it.postTimestamp
                    )
                )
            }
        }
        return Result.success(postList.asReversed())
    }


    suspend fun deleteAllRemote(uid: String): Result<Unit> {

        return try {
            val posts = dbPost.child(uid).get().await()
            for (post in posts.children) {
                Log.d(TAG, "imagePath: ${post.child("imagePath").value.toString()}")
                val imagePath = post.child("imagePath").value.toString()
                storageRef.child("images/$uid/$imagePath").delete().await()
            }
            dbPost.child(uid).removeValue().await()

            Result.success(Unit)
        } catch (e: Exception) {
            when (e) {
                is DatabaseException -> {
                    Log.d(TAG, "DatabaseException: ${e.message}")
                    Result.failure(e.cause!!)
                }
                is StorageException -> {
                    Log.d(TAG, "StorageException: ${e.message}")
                    Result.failure(e.cause!!)
                }
                else -> Result.failure(e.cause!!)
            }
        }
    }


    suspend fun deletePostRemote(
        uid: String,
        idPostRemote: String,
        imagePath: String
    ): Result<Unit> {
        return try {
            Log.d(TAG, imagePath)
            dbPost.child(uid).child(idPostRemote).removeValue().await()
            FirebaseStorage.getInstance().getReferenceFromUrl(imagePath).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            when (e) {
                is DatabaseException -> Result.failure(Throwable(e.cause!!))
                is StorageException -> Result.failure(Throwable(e.cause!!))
                else -> Result.failure(Throwable("Impossibile cancellare il post"))
            }
        }
    }

    suspend fun getLatestPost(uid: String): Post {

        var postRetrieved: Post = Post(0, "", "", "", "", "")
        val postFromRemote =
            dbPost.child(uid).limitToLast(1).get().await()
        Log.d(TAG, postFromRemote.childrenCount.toString())
        for (post in postFromRemote.children) {
            post.getValue(PostFromRemote::class.java).let {
                Log.d(TAG, post.toString())
                it?.imagePath =
                    storageRef.child("images/$uid/${it?.imagePath}").downloadUrl.await()
                        .toString()
                postRetrieved = Post(
                    0,
                    post.key.toString(),
                    it!!.imagePath,
                    it.sketchedBy,
                    it.description,
                    it.postTimestamp
                )
            }
            break
        }
        return postRetrieved
    }
}