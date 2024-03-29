package com.example.artkeeper.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.artkeeper.data.model.User
import com.example.artkeeper.utils.ArtKeeper

class DeleteLocalUser(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {


    private val userRepository = (ctx.applicationContext as ArtKeeper).userRepository
    private val postRepository = (ctx.applicationContext as ArtKeeper).postRepository


    override suspend fun doWork(): Result {

        return try {
            val uid = inputData.getString("uid")
            val nickname = inputData.getString("nickname")
            val firstName = inputData.getString("firstName")
            val lastname = inputData.getString("lastName")
            val photoUser = inputData.getString("photoUser")
            val nChild = inputData.getInt("nChild", 0)
            val nameChild = inputData.getStringArray("nameChild")?.toList()

            userRepository.deleteUserLocal(
                User(
                    uid!!,
                    firstName!!,
                    lastname!!,
                    photoUser!!,
                    nickname!!,
                    nChild,
                    nameChild,
                )
            )

            postRepository.deleteAll()

            Result.success(workDataOf("value" to true))
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}