package com.example.artkeeper.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.utils.ArtKeeper

class SendFollowingRequest(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    val userRepository = (ctx.applicationContext as ArtKeeper).userRepository

    override suspend fun doWork(): Result {
        val uidUser = inputData.getString("uidUser")!!
        val uidRequest = inputData.getString("uidRequest")!!
        return try {
            userRepository.getUserRemote(uidUser).onSuccess { user ->
                var listRequest = mutableListOf<String>()
                Log.d(javaClass.simpleName, user.pendingRequest.toString())
                if (!user.pendingRequest.isNullOrEmpty())
                    listRequest = user.pendingRequest as MutableList<String>
                listRequest.add(uidRequest)

                userRepository.insertFollowingRequestRemote(uidUser, listRequest)
                userRepository.insertFollowingRequestLocal(uidUser, listRequest)
            }

            userRepository.getUserRemote(uidRequest).onSuccess { user ->
                var listRequest = mutableListOf<String>()
                Log.d(javaClass.simpleName, user.pendingRequest.toString())
                if (!user.pendingRequest.isNullOrEmpty())
                    listRequest = user.pendingRequest as MutableList<String>
                listRequest.add(uidUser)

                userRepository.insertFollowingRequestRemote(uidRequest, listRequest)
                userRepository.insertFollowingRequestLocal(uidRequest, listRequest)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }

    }
}