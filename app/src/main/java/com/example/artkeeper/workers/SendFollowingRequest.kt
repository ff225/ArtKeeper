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
        val isFollowingRequest = inputData.getBoolean("isFollowingRequest", false)

        return try {

            userRepository.getUserRemote(uidUser).onSuccess { user ->
                var listRequest = mutableListOf<String>()
                Log.d(javaClass.simpleName, user.pendingRequestTo.toString())

                if (isFollowingRequest) {
                    if (!user.pendingRequestTo.isNullOrEmpty())
                        listRequest = user.pendingRequestTo as MutableList<String>
                    listRequest.add(uidRequest)
                    userRepository.insertFollowingRequestRemote(
                        uidUser,
                        "pendingRequestTo",
                        listRequest
                    )
                } else {
                    listRequest = user.pendingRequestTo as MutableList<String>
                    listRequest.remove(uidRequest)
                    userRepository.insertFollowingRequestRemote(
                        uidUser,
                        "pendingRequestTo",
                        listRequest
                    )
                }
            }

            userRepository.getUserRemote(uidRequest).onSuccess { user ->
                var listRequest = mutableListOf<String>()
                Log.d(javaClass.simpleName, user.pendingRequestFrom.toString())

                if (isFollowingRequest) {
                    if (!user.pendingRequestFrom.isNullOrEmpty())
                        listRequest = user.pendingRequestFrom as MutableList<String>
                    listRequest.add(uidUser)
                    userRepository.insertFollowingRequestRemote(
                        uidRequest,
                        "pendingRequestFrom",
                        listRequest
                    )
                } else {
                    listRequest = user.pendingRequestFrom as MutableList<String>
                    listRequest.remove(uidUser)
                    userRepository.insertFollowingRequestRemote(
                        uidRequest,
                        "pendingRequestFrom",
                        listRequest
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }

    }
}