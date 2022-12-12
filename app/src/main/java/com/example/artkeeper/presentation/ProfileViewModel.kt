package com.example.artkeeper.presentation

import android.util.Log
import androidx.lifecycle.*
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.model.User
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
    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    private val _user: MutableLiveData<User> =
        userRepo.getUser(uid!!).asLiveData() as MutableLiveData<User>

    //val user: LiveData<User> = userRepo.getUser(uid!!).asLiveData()
    val user: LiveData<User> = _user
    val numPost: LiveData<Int> = postRepo.getNumPost(uid!!).asLiveData()
    val postUser: LiveData<List<Post>> = postRepo.getAllUserPost(uid!!).asLiveData()

    init {
        reset()
        Log.d("ProfileViewModel", "${_nChild}, ${_nameChild.size} ")
    }

    fun checkUser(uid: String): Boolean {
        return userRepo.checkUser(uid)
    }

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
        viewModelScope.launch(Dispatchers.IO) {
            userRepo.addChild(uid!!, _nChild, _nameChild)
        }
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

    fun updateInfoUser() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepo.updateUser(createUser(uid!!))
        }
    }

    //TODO: Rimuovere uid
    fun insertUser(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepo.insertUser(createUser(uid))
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun deleteAccount() = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {
            FirebaseAuth.getInstance().currentUser?.delete()!!.await()
            postRepo.deleteAll(uid!!)
            userRepo.deleteUser(user.value!!)
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