package com.example.artkeeper.presentation

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

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    private var _name: String = ""
    private var _lastName: String = ""
    private var _nickName: String = ""

    //private var _user: LiveData<User> = MutableLiveData(null)
    val user: LiveData<User> = userRepo.getUser(uid!!).asLiveData()
    val numPost: LiveData<Int> = postRepo.getNumPost(uid!!).asLiveData()
    val postUser: LiveData<List<Post>> = postRepo.getAllUserPost(uid!!).asLiveData()

    fun checkUser(uid: String): Boolean {
        return userRepo.checkUser(uid)
    }

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
        _name = name
    }

    fun setLastName(LastName: String) {
        _lastName = LastName
    }

    fun setNickName(nick: String) {
        _nickName = nick
    }

    fun checkUserInfo() =
        !(_name.equals("") || _lastName.equals("") || _nickName.equals(""))

    private fun createUser(uid: String): User {
        return User(
            uid,
            _name,
            _lastName,
            _nickName,
            1,
            "Francesco"
        )
    }

    fun insertUser(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepo.insertUser(createUser(uid))
        }
    }
    /*
    private fun insertUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepo.insertUser(user)
        }
    }

/*
    fun confirmUserCreation(uid: String): Boolean {
        if (checkUserInfo()) {
            insertUser(createUser(uid))
            return true
        }
        return false
    }
 */*/
}

class ProfileViewModelFactory(
    private val userRepo: UserRepository,
    private val postRepo: PostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(userRepo, postRepo) as T
    }
}