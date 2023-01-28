package com.example.artkeeper.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.utils.ArtKeeper

class GetLatestPost(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val postRepo = (ctx.applicationContext as ArtKeeper).postRepository
    override suspend fun doWork(): Result {
        try {
            val uid = inputData.getString("uid")!!
            postRepo.updateRoomWithLatestPost(uid)
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }

    }
}