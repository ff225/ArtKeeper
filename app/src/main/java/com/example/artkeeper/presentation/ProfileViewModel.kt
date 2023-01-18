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
import com.example.artkeeper.workers.DeleteLocalUser
import com.example.artkeeper.workers.DeleteRemoteUser
import com.example.artkeeper.workers.SaveChildRemote
import com.example.artkeeper.workers.UserWorker
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

    private lateinit var _name: String
    private lateinit var _lastName: String
    private lateinit var _nickName: String
    private lateinit var _nameChild: MutableList<String>
    private var _nChild: Int = -1

    //private val firebaseAuth = FirebaseAuth.getInstance().currentUser
    private val _user: MutableLiveData<User> =
        userRepo.getUserLocal(firebaseAuth.uid.toString()).asLiveData() as MutableLiveData<User>
    val user: LiveData<User> = _user
    val numPost: LiveData<Int> = postRepo.getNumPost(firebaseAuth.uid.toString()).asLiveData()
    val postUser: LiveData<List<Post>> =
        postRepo.getAllUserPost(firebaseAuth.uid.toString()).asLiveData()

    val deleteRemoteUserWorksInfos: LiveData<List<WorkInfo>>
    val logoutUserWorkInfo: LiveData<List<WorkInfo>>

    init {
        reset()
        deleteRemoteUserWorksInfos =
            workManager.getWorkInfosForUniqueWorkLiveData("DeleteRemoteUserWorker")

        logoutUserWorkInfo =
            workManager.getWorkInfosForUniqueWorkLiveData("DeleteLocalAccountUserWorker")
        Log.d("ProfileViewModel", "${_nChild}, ${_nameChild.size} ")
    }

    fun checkUser() = liveData {
        emit(Resource.Loading())
        if (userRepo.checkUserRemote()) {
            insertUser(createUserFromRemote(getUserOnline()))
            emit(Resource.Success("Utente registrato"))
        } else
            emit(Resource.Failure(Exception("Utente deve registrarsi...")))

    }

    /*
    Le chiamate suspend vanno utilizzate quando va emesso un solo valore.
    Non vanno usate, per es., quando vogliamo caricare i post.
     */
    private suspend fun getUserOnline() = userRepo.getUserOnline()

    private fun createUserFromRemote(uo: UserOnline) = User(
        uo.uid!!,
        uo.firstName!!,
        uo.lastName!!,
        uo.nickName!!,
        uo.nChild!!,
        uo.nameChild ?: listOf()
    )

    fun addChild(name: String) {
        //reset()
        _nameChild = _user.value?.nameChild as MutableList<String>
        _nameChild.add(name)
        _nChild = _nameChild.size
        Log.d("ProfileViewModel", "${_nameChild.size}")
        storeChild()

    }

    fun removeChild(id: Int) {
        _nameChild = _user.value?.nameChild as MutableList<String>
        _nameChild.removeAt(id)
        _nChild = _nameChild.size
        storeChild()

    }

    private fun storeChild() {
        saveChildRemote()
        viewModelScope.launch {
            userRepo.addChildLocal(firebaseAuth.uid.toString(), _nChild, _nameChild)
        }
    }

    private fun saveChildRemote() {
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

    fun setName(name: String) {
        _name = name.trim()
    }

    fun setLastName(lastName: String) {
        _lastName = lastName.trim()
    }

    fun setNickName(nick: String) {
        _nickName = nick.trim()
    }

    fun checkUserInfo() =
        !(_name.isBlank() || _lastName.isBlank() || _nickName.isBlank())

    private fun getNameChild() = _user.value?.nameChild ?: listOf()
    private fun getNChild() = _user.value?.nChild ?: 0


    private fun createUser(uid: String): User {
        return User(
            uid,
            _name,
            _lastName,
            _nickName,
            _nChild,
            _nameChild
        )
    }

    fun updateInfoUser(prevNickname: String) = liveData {
        emit(Resource.Loading())
        if (userRepo.checkNicknameLocal(_nickName) && _nickName != prevNickname)
            emit(Resource.Failure(Exception("Nickname Utilizzato")))
        else {
            updateRemote()
            emit(Resource.Success(userRepo.updateUserLocal(createUser(firebaseAuth.uid.toString()))))

        }
    }

    private fun updateRemote() {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val updateRequest = OneTimeWorkRequestBuilder<UserWorker>()

            .setInputData(
                createInputDataForUri(
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

    private fun createInputDataForUri(user: User) =
        workDataOf(
            "uid" to user.uid,
            "nickname" to user.nickName,
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "nChild" to user.nChild,
            "nameChild" to user.nameChild?.toTypedArray()
        )


    /*
    // TODO: workmanager
    fun updateInfoUserOnline(prevNickname: String) = liveData {
        emit(Resource.Loading())
        if (userRepo.checkNicknameLocal(_nickName) && _nickName != prevNickname)
            emit(Resource.Failure(Exception("Nickname Utilizzato")))
        else
            emit(Resource.Success(userRepo.insertUserRemote(createUser(firebaseAuth!!.uid))))
    }
*/
    fun insertUserOnline() = liveData {
        emit(Resource.Loading())
        if (userRepo.checkNicknameRemote(_nickName))
            emit(Resource.Failure(Exception("Nickname Utilizzato")))
        else {
            insertUser(createUser(firebaseAuth.uid.toString()))
            emit(Resource.Success(userRepo.insertUserRemote(createUser(firebaseAuth.uid.toString()))))
        }
    }


    private suspend fun insertUser(user: User) {
        userRepo.insertUserLocal(user)
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            postRepo.delete(post)
        }
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
                createInputDataForUri(
                    createUser(firebaseAuth.uid.toString())
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()


    private fun deleteUserRemote() = OneTimeWorkRequestBuilder<DeleteRemoteUser>().build()


    fun deleteLocalAccount() {
        reset()
        workManager
            .beginUniqueWork(
                "DeleteLocalAccountUserWorker",
                ExistingWorkPolicy.REPLACE,
                deleteUserLocal()
            )
            .enqueue()
    }


    // TODO: deleteRemoteUserPost
    fun deleteRemoteAccount() {
        workManager.beginUniqueWork(
            "DeleteRemoteUserWorker",
            ExistingWorkPolicy.REPLACE,
            deleteUserRemote()
        )
            .then(deleteUserLocal()).enqueue()
    }

    /*
    fun deleteAccount() = liveData {
        emit(Resource.Loading())
        try {
            deleteRemoteAccount()
            //userRepo.deleteUserRemote()
            //userRepo.deleteUserLocal(user.value!!)
            //deleteLocalAccount()
            emit(Resource.Success("Operazione completata con successo."))

        } catch (e: Exception) {
            Log.d("ProfileViewModel", e.message.toString())
            emit(Resource.Failure(e))
        }
    }
    */
    private fun reset() {
        _name = ""
        _lastName = ""
        _nickName = ""
        _nameChild = getNameChild().toMutableList()
        _nChild = getNChild()
        Log.d("ProfileViewModel", "in reset: ${user.value?.nChild}")
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