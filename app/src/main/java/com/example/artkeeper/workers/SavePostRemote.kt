package com.example.artkeeper.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.data.model.PostToRemote
import com.example.artkeeper.utils.ArtKeeper

class SavePostRemote(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private val TAG = javaClass.simpleName
    private val postRepo = (ctx.applicationContext as ArtKeeper).postRepository

    override suspend fun doWork(): Result {
        val uid = inputData.getString("uid")!!
        val imagePath = inputData.getString("imagePath")
        val imageName = imagePath!!.split("/").let {
            it[it.lastIndex]
        }
        val post = PostToRemote(
            imageName,
            inputData.getString("childName"),
            inputData.getString("description"),
            inputData.getString("timestamp")!!
        )

        val saveImage = postRepo.saveImageRemote(uid, imagePath)
        val savePost = postRepo.insertRemote(uid, post)

        return if (saveImage.isSuccess)
            if (savePost.isSuccess) {
                Result.success()
            } else {
                savePost.onFailure {
                    Log.d(TAG, it.message.toString())
                }
                Result.failure()
            }
        else {
            saveImage.onFailure {
                Log.d(TAG, it.message.toString())
            }
            Result.failure()
        }
    }

}
