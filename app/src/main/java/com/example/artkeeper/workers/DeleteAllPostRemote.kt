package com.example.artkeeper.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.utils.ArtKeeper

class DeleteAllPostRemote(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val postRepo = (ctx.applicationContext as ArtKeeper).postRepository
    private val TAG = javaClass.simpleName

    override suspend fun doWork(): Result {
        val uid = inputData.getString("uid")

        val deleteAll = postRepo.deleteAllPostRemote(uid!!)
        return if (deleteAll.isSuccess)
            Result.success()
        else {
            deleteAll.onFailure {
                Log.d(TAG, it.cause.toString())
            }
            Result.failure()
        }
    }
}