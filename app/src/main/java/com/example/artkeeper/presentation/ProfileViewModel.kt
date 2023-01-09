package com.example.artkeeper.presentation

import android.util.Log
import androidx.lifecycle.*
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.model.UserOnline
import com.example.artkeeper.data.repository.PostRepository
import com.example.artkeeper.data.repository.UserRepository
import com.example.artkeeper.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val postRepo: PostRepository
) :
    ViewModel() {

    private lateinit var _name: String
    private lateinit var _lastName: String
    private lateinit var _nickName: String
    private lateinit var _nameChild: MutableList<String>
    private var _nChild: Int = -1
    private val firebaseAuth = FirebaseAuth.getInstance().currentUser
    private val _user: MutableLiveData<User> =
        userRepo.getUserLocal(firebaseAuth!!.uid).asLiveData() as MutableLiveData<User>
    val user: LiveData<User> = _user
    val numPost: LiveData<Int> = postRepo.getNumPost(firebaseAuth!!.uid).asLiveData()
    val postUser: LiveData<List<Post>> = postRepo.getAllUserPost(firebaseAuth!!.uid).asLiveData()
    //private lateinit var userTmp: UserOnline

    init {
        reset()
        Log.d("ProfileViewModel", "${_nChild}, ${_nameChild.size} ")
    }

    fun checkUser() = liveData {
        emit(Resource.Loading())
        if (userRepo.checkUserRemote()) {
            //Log.d("LoginFragment-ProfileViewModel", getUserOnline().nickName.toString())
            //getUserOnline()
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
        uo.nameChild!!
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

    private fun storeChild() = viewModelScope.launch {
        userRepo.addChildRemote(_nChild, _nameChild)
    }

/*
    private fun storeChild() {
        viewModelScope.launch {
            userRepo.addChild(uid!!, _nChild, _nameChild)
        }
    }
 */

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
        else
            emit(Resource.Success(userRepo.updateUserLocal(createUser(firebaseAuth!!.uid))))
    }

    fun updateInfoUserOnline(prevNickname: String) = liveData {
        emit(Resource.Loading())
        if (userRepo.checkNicknameLocal(_nickName) && _nickName != prevNickname)
            emit(Resource.Failure(Exception("Nickname Utilizzato")))
        else
            emit(Resource.Success(userRepo.insertUserRemote(createUser(firebaseAuth!!.uid))))
    }

    fun insertUserOnline() = liveData {
        emit(Resource.Loading())
        if (userRepo.checkNicknameRemote(_nickName))
            emit(Resource.Failure(Exception("Nickname Utilizzato")))
        else {
            insertUser(createUser(firebaseAuth!!.uid))
            emit(Resource.Success(userRepo.insertUserRemote(createUser(firebaseAuth!!.uid))))
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
            emit(Resource.Success("Fatto"))
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun deleteAccount() = liveData {
        emit(Resource.Loading())
        try {
            //coroutineScope {
            //FirebaseAuth.getInstance().currentUser?.delete()!!.await()
            //postRepo.deleteAll(uid!!)
            //userRepo.deleteUser(user.value!!)
            //}
            userRepo.deleteUserRemote()
            emit(Resource.Success("Operazione completata con successo."))
        } catch (e: Exception) {
            Log.d("ProfileViewModel", e.message.toString())
            emit(Resource.Failure(e))
        }
    }

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
    private val postRepo: PostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(userRepo, postRepo) as T
    }
}