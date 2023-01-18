package com.example.artkeeper.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.utils.ArtKeeper

class DeleteRemoteUser(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val userRepository = (ctx.applicationContext as ArtKeeper).userRepository
    override suspend fun doWork(): Result {

        userRepository.deleteUserRemote().onSuccess {
            Log.d("DeleteRemoteUser - onSuccess", "isDeleted: ${it.toString()}")
            return Result.success()
        }.onFailure { error ->
            Log.d("DeleteUserRemote - onFailure", error.cause.toString())
            return Result.failure()
        }

        return Result.success()
        //return Result.success()
        /*return try {

            }
            Log.d("DeleteRemoteUserWorker", userRepository.deleteUserRemote().toString())
            //Log.d("DeleteRemoteUserWorker", runAttemptCount.toString())
            Result.success()
        } catch (t: Exception)
        {
            Log.d("DeleteRemoteUserWorker", runAttemptCount.toString())
            Log.e("DeleteRemoteUserUserWorker", "Error")
            Result.failure()
        }*/
    }
}