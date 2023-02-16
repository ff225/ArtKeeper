package com.example.artkeeper.presentation

import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.repository.PostRepository
import com.example.artkeeper.data.repository.UserRepository
import com.example.artkeeper.utils.Constants
import com.example.artkeeper.utils.Resource
import com.example.artkeeper.workers.AcceptFollowingRequest
import com.example.artkeeper.workers.SendFollowingRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class VisitedUserProfileViewModel(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _post = MutableLiveData<List<Post>>()
    val post: LiveData<List<Post>>
        get() = _post

    private val _followers = MutableLiveData<List<String>>()
    val followers: LiveData<List<String>>
        get() = _followers

    private val _pendingRequestFrom = MutableLiveData<List<String>>()
    val pendingRequestFrom: LiveData<List<String>>
        get() = _pendingRequestFrom

    private val _pendingRequestTo = MutableLiveData<List<String>>()
    val pendingRequestTo: LiveData<List<String>>
        get() = _pendingRequestTo

    init {
        observePendingRequestTo()
        observePendingRequestFrom()
        observeFollower()
    }


    fun getInfoUser(uidRequest: String) = liveData {
        emit(Resource.Loading())
        userRepository.getUserRemote(uidRequest).onSuccess { userInfo ->
            emit(Resource.Success(userInfo))
        }.onFailure {
            Log.d(javaClass.simpleName, it.message.toString())
            emit(Resource.Failure(Exception()))
        }
    }


    fun sendRequest(uidRequest: String, isFollowingRequest: Boolean) {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val sendRequest = OneTimeWorkRequestBuilder<SendFollowingRequest>()
            .setInputData(
                workDataOf(
                    "uidUser" to Constants.firebaseAuth.uid.toString(),
                    "uidRequest" to uidRequest,
                    "isFollowingRequest" to isFollowingRequest
                )
            )
            .setConstraints(constraint)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager
            .beginUniqueWork("SendRequestFollowing", ExistingWorkPolicy.REPLACE, sendRequest)
            .enqueue()
    }


    fun acceptRequest(uidRequest: String, isAcceptRequest: Boolean) {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val acceptFollowingReq = OneTimeWorkRequestBuilder<AcceptFollowingRequest>()
            .setInputData(
                workDataOf(
                    "uidUser" to Constants.firebaseAuth.uid.toString(),
                    "uidRequest" to uidRequest,
                    "isAcceptRequest" to isAcceptRequest
                )
            )
            .setConstraints(constraint)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager
            .beginUniqueWork(
                "AcceptFollowingRequest",
                ExistingWorkPolicy.REPLACE,
                acceptFollowingReq
            )
            .enqueue()
    }


    fun getPostUser(uidRequest: String) = liveData {
        emit(Resource.Loading())

        postRepository.getAllPostRemote(uidRequest).onSuccess {
            emit(Resource.Success(it))
        }.onFailure {
            emit(Resource.Failure(Exception()))
        }
    }


    //region:: observer
    private fun observeFollower() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userRepository.getFollowers().collect {
                    _followers.postValue(it)
                }
            }
        }
    }

    private fun observePendingRequestFrom() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userRepository.getPendingReqFrom().collect {
                    _pendingRequestFrom.postValue(it)
                }
            }
        }
    }

    private fun observePendingRequestTo() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userRepository.getPendingReqTo().collect {
                    _pendingRequestTo.postValue(it)
                }
            }
        }
    }
    //endregion
}

@Suppress("UNCHECKED_CAST")
class VisitedUserProfileViewModelFactory(
    private val userRepo: UserRepository,
    private val postRepository: PostRepository,
    private val workManager: WorkManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VisitedUserProfileViewModel(userRepo, postRepository, workManager) as T
    }
}