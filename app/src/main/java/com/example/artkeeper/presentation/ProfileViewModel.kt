package com.example.artkeeper.presentation

import android.net.Uri
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
import java.util.concurrent.TimeUnit

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val postRepo: PostRepository,
    private val workManager: WorkManager
) :
    ViewModel() {
    private val TAG: String = javaClass.simpleName

    private lateinit var _name: String
    private lateinit var _lastName: String
    private lateinit var _nickName: String
    private lateinit var _nameChild: MutableList<String>
    private var _nChild: Int = -1

    private val _user: MutableLiveData<User> =
        userRepo.getUserLocal(firebaseAuth.uid.toString()).asLiveData() as MutableLiveData<User>

    val user: LiveData<User> = _user
    val userPhoto: Uri? = firebaseAuth.currentUser?.photoUrl
    val numPost: LiveData<Int> = postRepo.getNumPost().asLiveData()
    val postUser: LiveData<List<Post>> =
        postRepo.getAllUserPost().asLiveData()

    val deleteRemoteUserWorksInfo: LiveData<List<WorkInfo>>
    val logoutUserWorkInfo: LiveData<List<WorkInfo>>

    init {
        reset()
        deleteRemoteUserWorksInfo =
            workManager.getWorkInfosForUniqueWorkLiveData("DeleteRemoteUserWorker")

        logoutUserWorkInfo =
            workManager.getWorkInfosForUniqueWorkLiveData("DeleteLocalAccountUserWorker")
        Log.d(TAG, numPost.value.toString())


        viewModelScope.launch {

            if (postRepo.checkTableExist() == 0)
                postRepo.getAllPostUserRemote(firebaseAuth.uid.toString())
            //userRepo.insertNicknames()

        }
        Log.d(TAG, "${_nChild}, ${_nameChild.size} ")
        Log.d(TAG, "in init, image profile path: ${firebaseAuth.currentUser?.photoUrl.toString()}")
    }

    fun setName(name: String) {
        _name = name.trim()
    }

    fun setLastName(lastName: String) {
        _lastName = lastName.trim()
    }

    fun setNickName(nick: String) {
        _nickName = nick.trim()
    }

    private fun getNameChild() = _user.value?.nameChild ?: listOf()

    private fun getNChild() = _user.value?.nChild ?: 0

    private fun createUser(uid: String): User {
        return User(
            uid,
            _name,
            _lastName,
            userPhoto.toString(),
            _nickName,
            _nChild,
            _nameChild
        )
    }

    private fun createUserFromRemote(uo: UserOnline) = User(
        uo.uid!!,
        uo.firstName!!,
        uo.lastName!!,
        uo.photoUser!!,
        uo.nickName!!,
        uo.nChild!!,
        uo.nameChild ?: listOf()
    )

    fun checkUser() = liveData {
        emit(Resource.Loading())
        userRepo.checkUserRemote().onSuccess {
            insertUser(createUserFromRemote(getUserRemote(firebaseAuth.uid.toString())))
            emit(Resource.Success("Utente registrato"))
        }.onFailure {
            emit(Resource.Failure(Exception(it.message)))
        }
    }

    /*
    Le chiamate suspend vanno utilizzate quando va emesso un solo valore.
    Non vanno usate, per es., quando vogliamo caricare i post.
     */
    private suspend fun getUserRemote(uid: String) = userRepo.getUserRemote(uid).getOrThrow()

    fun addChild(name: String) {
        //reset()
        _nameChild = _user.value?.nameChild as MutableList<String>
        _nameChild.add(name)
        _nChild = _nameChild.size
        Log.i(TAG, "in addChild, Names: $_nameChild")
        Log.i(TAG, "in addChild, Size: $_nChild")
        saveChild()

    }

    fun removeChild(id: Int) {
        _nameChild = _user.value?.nameChild as MutableList<String>
        _nameChild.removeAt(id)
        _nChild = _nameChild.size
        Log.i(TAG, "in removeChild, Names: $_nameChild")
        Log.i(TAG, "in removeChild, Size: $_nChild")
        saveChild()

    }

    private fun saveChild() {
        saveChildWork()
        viewModelScope.launch {
            userRepo.addChildLocal(firebaseAuth.uid.toString(), _nChild, _nameChild)
        }
    }

    private fun saveChildWork() {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val saveChildRequest = OneTimeWorkRequestBuilder<SaveChildRemote>()
            .setInputData(
                workDataOf("nChild" to _nChild, "nameChild" to _nameChild.toTypedArray())
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

    private fun inputDataWorker(user: User) =
        workDataOf(
            "uid" to user.uid,
            "nickname" to user.nickName,
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "photoUser" to user.photo,
            "nChild" to user.nChild,
            "nameChild" to user.nameChild?.toTypedArray()
        )

    fun updateUserInfo(prevNickname: String) = liveData {
        emit(Resource.Loading())
        userRepo.checkNicknameRemote(_nickName).onSuccess {
            updateUserInfoWork()
            //updateNicknamePost(_nickName)
            emit(Resource.Success(userRepo.updateUserLocal(createUser(firebaseAuth.uid.toString()))))
        }.onFailure {
            if (_nickName != prevNickname)
                emit(Resource.Failure(Exception(it.message.toString())))
            else {
                updateUserInfoWork()
                emit(Resource.Success(userRepo.updateUserLocal(createUser(firebaseAuth.uid.toString()))))
            }
        }
    }

    private fun updateUserInfoWork() {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val updateRequest = OneTimeWorkRequestBuilder<SaveUserRemote>()

            .setInputData(
                inputDataWorker(
                    createUser(firebaseAuth.uid.toString())
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

    private suspend fun insertUser(user: User) {
        userRepo.insertUserLocal(user)
    }

    fun userRegistration() = liveData {
        emit(Resource.Loading())

        userRepo.checkNicknameRemote(_nickName).onSuccess { isNotUsed ->
            if (isNotUsed) {
                insertUser(createUser(firebaseAuth.uid.toString()))
                emit(Resource.Success(userRepo.insertUserRemote(createUser(firebaseAuth.uid.toString()))))
            }
        }.onFailure {
            emit(Resource.Failure(Exception("Nickname Utilizzato")))
        }
    }


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
            ExistingWorkPolicy.REPLACE,
            deletePostRequest
        ).enqueue()
    }

    fun deleteRegistration() = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {
            FirebaseAuth.getInstance().currentUser?.delete()!!.await()
            reset()
            emit(Resource.Success("Operazione effettuata con successo."))
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    private fun deleteUserLocal(): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<DeleteLocalUser>()
            .setInputData(
                inputDataWorker(
                    createUser(firebaseAuth.uid.toString())
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

    fun deleteUserLocalWork() {
        reset()
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
        workManager.cancelUniqueWork("getLatestPostWorker")
        workManager.beginUniqueWork(
            "DeleteRemoteUserWorker",
            ExistingWorkPolicy.REPLACE,
            deleteUserRemote()
        )
            .then(deleteAllPostRemote())
            .then(deleteUserLocal()).enqueue()
    }

    private fun reset() {
        _name = ""
        _lastName = ""
        _nickName = ""
        _nameChild = getNameChild().toMutableList()
        _nChild = getNChild()
        Log.i(TAG, "in reset, reset delle variabili")
    }

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