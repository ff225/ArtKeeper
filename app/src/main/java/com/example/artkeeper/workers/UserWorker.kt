package com.example.artkeeper.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.data.model.User
import com.example.artkeeper.utils.ArtKeeper

class UserWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private val userRepository = (ctx.applicationContext as ArtKeeper).userRepository

    override suspend fun doWork(): Result {
        return try {
            val uid = inputData.getString("uid")
            val nickname = inputData.getString("nickname")
            val firstName = inputData.getString("firstName")
            val lastname = inputData.getString("lastName")
            val nChild = inputData.getInt("nChild", 0)
            val nameChild = inputData.getStringArray("nameChild")?.toList()

            userRepository.insertUserRemote(
                User(
                    uid!!,
                    firstName!!,
                    lastname!!,
                    nickname!!,
                    nChild,
                    nameChild
                )
            )
            Log.d("UserWorker", runAttemptCount.toString())
            return Result.success()
        } catch (t: Exception) {
            Log.d("UserWorker", runAttemptCount.toString())
            Log.e("UserWorker", "Error")
            Result.failure()
        }
    }
}