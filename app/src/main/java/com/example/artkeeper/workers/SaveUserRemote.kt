package com.example.artkeeper.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.data.model.User
import com.example.artkeeper.utils.ArtKeeper

class SaveUserRemote(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val TAG: String = javaClass.simpleName
    private val userRepository = (ctx.applicationContext as ArtKeeper).userRepository
    override suspend fun doWork(): Result {
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
        Log.d(TAG, "Success")
        return Result.success()
    }
}