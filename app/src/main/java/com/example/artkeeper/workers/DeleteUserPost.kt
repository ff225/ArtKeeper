package com.example.artkeeper.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.utils.ArtKeeper


class DeleteUserPost(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val postRepo = (ctx.applicationContext as ArtKeeper).postRepository
    override suspend fun doWork(): Result {
        return try {
            val uid = inputData.getString("uid")!!
            val timestamp = inputData.getString("timestamp")!!
            postRepo.deleteRemote(uid, timestamp)
            Result.success()
        } catch (e: Exception) {
            Log.d("DeleteUserPost", e.message.toString())
            Result.failure()
        }
    }
}