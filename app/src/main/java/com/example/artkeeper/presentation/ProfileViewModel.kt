package com.example.artkeeper.presentation

import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.model.UserOnline
import com.example.artkeeper.data.repository.PostRepository
import com.example.artkeeper.data.repository.UserRepository
import com.example.artkeeper.utils.Constants.firebaseAuth
import com.example.artkeeper.utils.Resource
import com.example.artkeeper.workers.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val postRepo: PostRepository,
    private val workManager: WorkManager
) :
    ViewModel() {
    private val TAG: String = javaClass.simpleName

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    val postUser: LiveData<List<Post>> =
        postRepo.getAllUserPost().asLiveData()

    private val _pendingRequestFrom = MutableLiveData<List<String>>()
    val pendingRequestFrom: LiveData<List<String>>
        get() = _pendingRequestFrom
    private val _followers = MutableLiveData<List<String>>()
    val followers: LiveData<List<String>>
        get() = _followers


    val deleteRemoteUserWorksInfo: LiveData<List<WorkInfo>>
    val logoutUserWorkInfo: LiveData<List<WorkInfo>>

    init {
        deleteRemoteUserWorksInfo =
            workManager.getWorkInfosForUniqueWorkLiveData("DeleteRemoteUserWorker")

        logoutUserWorkInfo =
            workManager.getWorkInfosForUniqueWorkLiveData("DeleteLocalAccountUserWorker")

        observeUser()
        observePendingRequestFrom()
        observeFollowers()

        viewModelScope.launch {
            postRepo.getAllPostUserRemote(firebaseAuth.uid.toString())

        }
    }

    private fun createUser(name: String, lastName: String, nickName: String): User {
        return User(
            firebaseAuth.uid!!,
            name,
            lastName,
            firebaseAuth.currentUser?.photoUrl.toString(),
            nickName,
            _user.value?.nChild ?: 0,
            _user.value?.nameChild ?: mutableListOf(),
        )
    }

    private fun inputDataWorker(user: User) =
        workDataOf(
            "uid" to user.uid,
            "nickname" to user.nickName,
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "photoUser" to user.photo,
            "nChild" to user.nChild,
            "name_child" to user.nameChild?.toTypedArray(),
        )


    // region:: login e registrazione utente

    private fun createUserFromRemote(uo: UserOnline) = User(
        uo.uid!!,
        uo.firstName!!,
        uo.lastName!!,
        uo.photoUser!!,
        uo.nickName!!,
        uo.nChild!!,
        uo.nameChild ?: listOf(),
    )

    private suspend fun insertUser(user: User) {
        userRepo.insertUserLocal(user)
    }

    fun checkUser() = liveData {
        emit(Resource.Loading())
        userRepo.checkUserRemote().onSuccess {
            insertUser(createUserFromRemote(getUserRemote(firebaseAuth.uid.toString())))
            emit(Resource.Success("Utente registrato"))
        }.onFailure {
            emit(Resource.Failure(Exception(it.message)))
        }
    }


    private suspend fun getUserRemote(uid: String) = userRepo.getUserRemote(uid).getOrThrow()

    fun userRegistration(name: String, lastName: String, nickName: String) = liveData {
        emit(Resource.Loading())

        userRepo.checkNicknameRemote(nickName).onSuccess { isNotUsed ->
            if (isNotUsed) {
                insertUser(createUser(name, lastName, nickName))
                emit(
                    Resource.Success(
                        userRepo.insertUserRemote(
                            createUser(
                                name,
                                lastName,
                                nickName
                            )
                        )
                    )
                )
            }
        }.onFailure {
            emit(Resource.Failure(Exception("Nickname Utilizzato")))
        }
    }

    fun deleteRegistration() = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {
            FirebaseAuth.getInstance().currentUser?.delete()!!.await()
            emit(Resource.Success("Operazione effettuata con successo."))
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    // endregion


    // region:: logout

    private fun deleteUserLocal(): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<DeleteLocalUser>()
            .setInputData(
                inputDataWorker(
                    _user.value!!
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

    fun deleteUserLocalWork() {
        workManager.cancelAllWorkByTag("savePostRequest")
        workManager.cancelUniqueWork("getLatestPostWorker")
        workManager
            .beginUniqueWork(
                "DeleteLocalAccountUserWorker",
                ExistingWorkPolicy.REPLACE,
                deleteUserLocal()
            )
            .enqueue()
    }

    private fun deleteUserRemote(): OneTimeWorkRequest {
        val constraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        return OneTimeWorkRequestBuilder<DeleteRemoteUser>().setConstraints(constraint)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            ).build()
    }

    // endregion


    // region:: aggiungi/rimuovi figlio
    fun addChild(name: String) {
        val nameChild = _user.value?.nameChild as MutableList<String>
        nameChild.add(name)
        val nChild = nameChild.size
        Log.i(TAG, "in addChild, Names: $nameChild")
        Log.i(TAG, "in addChild, Size: $nChild")
        saveChild(nameChild, nChild)

    }

    fun removeChild(id: Int) {
        val nameChild = _user.value?.nameChild as MutableList<String>
        nameChild.removeAt(id)
        val nChild = nameChild.size
        Log.i(TAG, "in removeChild, Names: $nameChild")
        Log.i(TAG, "in removeChild, Size: $nChild")
        saveChild(nameChild, nChild)

    }

    private fun saveChild(nameChild: List<String>, nChild: Int) {
        saveChildWork(nameChild, nChild)
        viewModelScope.launch {
            userRepo.addChildLocal(firebaseAuth.uid.toString(), nChild, nameChild)
        }
    }

    private fun saveChildWork(nameChild: List<String>, nChild: Int) {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val saveChildRequest = OneTimeWorkRequestBuilder<SaveChildRemote>()
            .setInputData(
                workDataOf("nameChild" to nameChild.toTypedArray(), "nChild" to nChild)
            ).setConstraints(constraint)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager
            .beginUniqueWork("SaveChildWorker", ExistingWorkPolicy.REPLACE, saveChildRequest)
            .enqueue()


    }
    // endregion


    // region:: update info utente
    fun updateUserInfo(name: String, lastName: String, nickName: String, prevNickname: String) =
        liveData {
            emit(Resource.Loading())
            userRepo.checkNicknameRemote(nickName).onSuccess {
                updateUserInfoWork(name, lastName, nickName)
                emit(
                    Resource.Success(
                        userRepo.updateUserLocal(
                            createUser(
                                name,
                                lastName,
                                nickName
                            )
                        )
                    )
                )
            }.onFailure {
                if (nickName != prevNickname)
                    emit(Resource.Failure(Exception(it.message.toString())))
                else {
                    updateUserInfoWork(name, lastName, nickName)
                    emit(
                        Resource.Success(
                            userRepo.updateUserLocal(
                                createUser(
                                    name,
                                    lastName,
                                    nickName
                                )
                            )
                        )
                    )
                }
            }
        }


    private fun updateUserInfoWork(name: String, lastName: String, nickName: String) {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val updateRequest = OneTimeWorkRequestBuilder<SaveUserRemote>()

            .setInputData(
                inputDataWorker(
                    createUser(name, lastName, nickName)
                )

            ).setConstraints(constraint)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager
            .beginUniqueWork("UpdateUserWorker", ExistingWorkPolicy.REPLACE, updateRequest)
            .enqueue()

    }

    // endregion


    // region:: cancella post utente

    fun deletePost(post: Post) {
        deletePostRemote(post.idPost, post.imagePath)
        viewModelScope.launch {
            postRepo.delete(post)
        }
    }

    private fun deletePostRemote(idPost: String, imagePath: String) {
        val constraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val deletePostRequest =
            OneTimeWorkRequestBuilder<DeleteUserPost>().setConstraints(constraint).setInputData(
                workDataOf(
                    "uid" to firebaseAuth.uid,
                    "idPost" to idPost,
                    "imagePath" to imagePath
                )
            ).setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            ).build()

        workManager.beginUniqueWork(
            "deletePostRemote",
            ExistingWorkPolicy.APPEND,
            deletePostRequest
        ).enqueue()
    }

    // endregion


    // region:: rimuovi utente dal social
    private fun deleteAllPostRemote(): OneTimeWorkRequest {
        val constraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        return OneTimeWorkRequestBuilder<DeleteAllPostRemote>().setConstraints(constraint)
            .setInputData(workDataOf("uid" to firebaseAuth.uid.toString()))
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            ).build()
    }

    fun deleteUserRemoteWork() {
        workManager.cancelAllWorkByTag("getLatestPostWorker")
        workManager.beginUniqueWork(
            "DeleteRemoteUserWorker",
            ExistingWorkPolicy.REPLACE,
            deleteUserRemote()
        )
            .then(deleteAllPostRemote())
            .then(deleteUserLocal()).enqueue()
    }

    // endregion

    
    // region:: observer
    private fun observeUser() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userRepo.getUserLocal(firebaseAuth.uid!!)?.collect {
                    _user.postValue(it)
                }
            }
        }
    }

    private fun observePendingRequestFrom() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userRepo.getPendingReqFrom().collect {
                    _pendingRequestFrom.postValue(it)
                }
            }
        }
    }

    private fun observeFollowers() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userRepo.getFollowers().collect {
                    _followers.postValue(it)
                }
            }
        }
    }

    //endregion
}

@Suppress("UNCHECKED_CAST")
class ProfileViewModelFactory(
    private val userRepo: UserRepository,
    private val postRepo: PostRepository,
    private val workManager: WorkManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(userRepo, postRepo, workManager) as T
    }
}