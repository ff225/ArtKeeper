package com.example.artkeeper.presentation

import android.net.Uri
import androidx.lifecycle.*
import androidx.work.*
import com.example.artkeeper.data.model.PostToRemote
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.repository.UserRepository
import com.example.artkeeper.utils.Constants.firebaseAuth
import com.example.artkeeper.workers.GetLatestPost
import com.example.artkeeper.workers.SavePostRemote
import java.util.*
import java.util.concurrent.TimeUnit

class PostViewModel(
    userRepo: UserRepository,
    private val workManager: WorkManager
) :
    ViewModel() {

    private val uid: String = firebaseAuth.uid.toString()
    val user: LiveData<User> = userRepo.getUserLocal(uid).asLiveData()
    private var _imageUri: MutableLiveData<Uri?> = MutableLiveData(null)
    val imageUri: LiveData<Uri?>
        get() = _imageUri
    private var _childName: MutableLiveData<String?> = MutableLiveData(null)
    private var _description: MutableLiveData<String?> = MutableLiveData(null)
    val description: LiveData<String?>
        get() = _description
    private var _timestamp: Long = 0L

    fun setImageUri(imageUri: Uri) {
        _imageUri.value = imageUri
    }

    fun setDescription(description: String) {
        _description.value = description.trim()
    }

    fun setChildName(name: String?) {
        _childName.value = name
    }

    fun reset() {
        _imageUri.value = null
        _description.value = null
        _childName.value = null
        _timestamp = 0L
    }

    fun checkPost(): Boolean {
        if (_imageUri.value == null)
            return false

        return true
    }

    private fun createPostRemote(): PostToRemote {
        _timestamp = getTimestamp()
        return PostToRemote(
            _imageUri.value.toString(),
            _childName.value,
            _description.value,
            _timestamp.toString()
        )
    }

    private fun getLatestPost(): OneTimeWorkRequest {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        return OneTimeWorkRequestBuilder<GetLatestPost>().setInputData(workDataOf("uid" to uid))
            .setConstraints(constraint)
            .setInitialDelay(30, TimeUnit.SECONDS)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            ).build()
    }

    private fun savePostWork() {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val postRemote = createPostRemote()
        val savePostRequest =
            OneTimeWorkRequestBuilder<SavePostRemote>().setInputData(
                workDataOf(
                    "uid" to uid,
                    "imagePath" to postRemote.imagePath,
                    "childName" to postRemote.sketchedBy,
                    "description" to postRemote.description,
                    "timestamp" to postRemote.postTimestamp
                )
            )
                .setConstraints(constraint).setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        workManager.beginUniqueWork(
            "savePostRequest",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            savePostRequest
        ).then(getLatestPost())
            .enqueue()
    }

    fun insert() {
        savePostWork()
        /*viewModelScope.launch {
            repo.insert(createPost())
        }*/
    }

    private fun getTimestamp(): Long {
        val calendar = Calendar.getInstance()
        return Date(calendar.time.time).time

    }

}

class PostViewModelFactory(
    private val userRepo: UserRepository,
    private val workManager: WorkManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PostViewModel(userRepo, workManager) as T
    }
}