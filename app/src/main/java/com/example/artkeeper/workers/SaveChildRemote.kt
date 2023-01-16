package com.example.artkeeper.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.utils.ArtKeeper

class SaveChildRemote(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val userRepository = (ctx.applicationContext as ArtKeeper).userRepository

    override suspend fun doWork(): Result {
        return try {
            val nChild = inputData.getInt("nChild", 0)
            val nameChild = inputData.getStringArray("nameChild")?.asList()
            userRepository.addChildRemote(nChild, nameChild!!)
            Result.success()
        }catch (e: Exception){
            Result.failure()
        }
    }
}