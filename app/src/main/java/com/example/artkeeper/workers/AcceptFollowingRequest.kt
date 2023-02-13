package com.example.artkeeper.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.artkeeper.utils.ArtKeeper

class AcceptFollowingRequest(ctx: Context, params: WorkerParameters) :
    CoroutineWorker(ctx, params) {

    private val userRepository = (ctx.applicationContext as ArtKeeper).userRepository
    override suspend fun doWork(): Result {
        val uidUser = inputData.getString("uidUser")!!
        val uidRequest = inputData.getString("uidRequest")!!
        val isAcceptRequest = inputData.getBoolean("isAcceptRequest", false)
        return try {

            userRepository.getUserRemote(uidUser).onSuccess { user ->
                var listFollower = mutableListOf<String>()
                var listRequest = mutableListOf<String>()

                if (!user.pendingRequestFrom.isNullOrEmpty())
                    listRequest = user.pendingRequestFrom as MutableList<String>

                if (!user.follower.isNullOrEmpty())
                    listFollower = user.follower as MutableList<String>

                if (isAcceptRequest) {
                    listFollower.add(uidRequest)
                } else
                    listFollower.remove(uidRequest)

                listRequest.remove(uidRequest)

                userRepository.insertFollowingRequestRemote(
                    uidUser,
                    "follower",
                    listFollower
                )
                userRepository.insertFollowingRequestRemote(
                    uidUser,
                    "pendingRequestFrom",
                    listRequest
                )
            }

            userRepository.getUserRemote(uidRequest).onSuccess { user ->
                var listFollower = mutableListOf<String>()
                var listRequest = mutableListOf<String>()

                if (!user.pendingRequestTo.isNullOrEmpty())
                    listRequest = user.pendingRequestTo as MutableList<String>

                if (!user.follower.isNullOrEmpty())
                    listFollower = user.follower as MutableList<String>

                if (isAcceptRequest) {
                    listFollower.add(uidUser)
                } else
                    listFollower.remove(uidUser)

                listRequest.remove(uidUser)

                userRepository.insertFollowingRequestRemote(
                    uidRequest,
                    "pendingRequestTo",
                    listRequest
                )

                userRepository.insertFollowingRequestRemote(
                    uidRequest,
                    "follower",
                    listFollower
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}