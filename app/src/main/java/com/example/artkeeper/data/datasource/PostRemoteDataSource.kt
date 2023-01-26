package com.example.artkeeper.data.datasource

import android.net.Uri
import android.util.Log
import com.example.artkeeper.data.model.PostRemote
import com.example.artkeeper.utils.Constants.databaseRef
import com.example.artkeeper.utils.Constants.firebaseAuth
import com.example.artkeeper.utils.Resource
import com.google.firebase.database.DatabaseException
import com.google.firebase.ktx.Firebase
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
    private val firebaseUid = firebaseAuth.uid
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

    suspend fun insertPost(uid: String, post: PostRemote): Result<Unit> {
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

    suspend fun getAllPostRemote(uid: String): Resource<List<PostRemote>> {

        val postList = mutableListOf<PostRemote>()
        val posts = dbPost.child(uid).get().await()
        for (post in posts.children) {
            post.getValue(PostRemote::class.java).let {
                it?.imagePath =
                    storageRef.child("images/$uid/${it?.imagePath}").downloadUrl.await()
                        .toString()
                postList.add(
                    PostRemote(
                        it!!.imagePath,
                        it.sketchedBy,
                        it.description,
                        it.postTimestamp
                    )
                )
            }
        }
        //return Result.success(postList)
        return Resource.Success(postList)
        /* } catch (e: Exception) {
             when (e) {
                 is DatabaseException -> Resource.Failure() //Result.failure(Throwable(e.cause!!))
                 is StorageException -> Result.failure(Throwable(e.cause!!))
                 else -> Result.failure(Throwable("Impossibile scaricare i post..."))
             }
         }*/
    }

    // TODO: invece di fare la query posts, utilizzerò la lista che ho scaricato per visualizzare i post
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

    /*
        TODO:
         - per questa funzione ha senso passare direttamente l'oggetto che stiamo visualizzando.
           Quindi è necessario creare prima una funzione che scarichi tutti i post
     */

    suspend fun deletePostRemote(uid: String, timestamp: String): Result<Unit> {
        return try {
            val post = dbPost.child(uid).get().await()
            Result.success(Unit)
        } catch (e: Exception) {
            when (e) {
                is DatabaseException -> Result.failure(Throwable(e.cause!!))
                is StorageException -> Result.failure(Throwable(e.cause!!))
                else -> Result.failure(Throwable("Impossibile cancellare il post"))
            }
        }
    }
}