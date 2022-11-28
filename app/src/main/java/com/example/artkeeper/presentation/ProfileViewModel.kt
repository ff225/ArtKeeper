package com.example.artkeeper.presentation

import android.util.Log
import androidx.lifecycle.*
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.repository.PostRepository
import com.example.artkeeper.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val postRepo: PostRepository
) :
    ViewModel() {

    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    val user: LiveData<User> = userRepo.getUser(uid!!).asLiveData()
    val numPost: LiveData<Int> = postRepo.getNumPost(uid!!).asLiveData()
    val postUser: LiveData<List<Post>> = postRepo.getAllUserPost(uid!!).asLiveData()
    private lateinit var _name: String
    private lateinit var _lastName: String
    private lateinit var _nickName: String
    private var _nChild: Int = 0
    private var _nameChild: MutableList<String>? = null

    init {
        reset()
        Log.d("ProfileViewModel", "${_nChild}, ${_nameChild?.size} ")
    }

    fun checkUser(uid: String): Boolean {
        return userRepo.checkUser(uid)
    }

    fun setChild(name: String) {
        reset()
        _nChild += 1
        _nameChild?.add(name)
        Log.d("ProfileViewModel", "${_nameChild?.size}")
    }

    fun storeChild() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepo.addChild(uid!!, _nameChild?.size!!, _nameChild!!)
        }
    }

    /*
    fun updateUser() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepo.updateUser(user)
        }
    }
    */
    /*
    fun getUserRepo(uid: String): LiveData<User> {
        return userRepo.getUser(uid).asLiveData()
    }


    suspend fun getUserRepo(uid: String) {
        _user.value = userRepo.getUser(uid)
    }*/
    /*
            fun getUserPost(): LiveData<List<Post>> {
                return postRepo.getAllUserPost(_user.value!!.uid).asLiveData()
            }


            fun getNumPostUser(): LiveData<Int> {
                return postRepo.getNumPost(_user.value!!.uid).asLiveData()
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

    fun insertUser(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepo.insertUser(createUser(uid))
        }
    }

    private fun reset() {
        _name = ""
        _lastName = ""
        _nickName = ""
        _nameChild = user.value?.nameChild?.toMutableList() ?: mutableListOf()
        _nChild = user.value?.nChild ?: 0
        Log.d("ProfileViewModel", "in reset: ${user.value?.nChild}")
    }
}

class ProfileViewModelFactory(
    private val userRepo: UserRepository,
    private val postRepo: PostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(userRepo, postRepo) as T
    }
}