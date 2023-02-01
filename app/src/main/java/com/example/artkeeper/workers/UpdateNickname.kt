package com.example.artkeeper.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.utils.ArtKeeper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class UpdateNickname(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    val userRepository = (ctx.applicationContext as ArtKeeper).userRepository

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            async { userRepository.deleteNicknames() }.await()
            async { userRepository.insertNicknames() }.await()
        }
        return Result.success()
    }

}