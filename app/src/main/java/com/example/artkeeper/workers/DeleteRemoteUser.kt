package com.example.artkeeper.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.utils.ArtKeeper

class DeleteRemoteUser(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private val TAG: String = javaClass.simpleName
    private val userRepository = (ctx.applicationContext as ArtKeeper).userRepository

    override suspend fun doWork(): Result {

        userRepository.deleteUserRemote().onSuccess {
            Log.d(TAG, "isDeleted: $it")
            return Result.success()
        }.onFailure { error ->
            Log.e(TAG, error.message.toString())
            return Result.failure()
        }

        return Result.success()
    }
}