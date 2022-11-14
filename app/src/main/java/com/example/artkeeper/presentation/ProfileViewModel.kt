package com.example.artkeeper.presentation

import androidx.lifecycle.*
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.repository.PostRepository
import com.example.artkeeper.data.repository.UserRepository

class ProfileViewModel(private val userRepo: UserRepository, private val postRepo: PostRepository) :
    ViewModel() {

    private val _user: MutableLiveData<User?> = MutableLiveData(null)
    val user: LiveData<User?>
        get() = _user

    suspend fun getUserRepo(uid: String) {
        _user.value = userRepo.getUser(uid)
    }


    fun getUserPost(): LiveData<List<Post>> {
       return postRepo.getAllUserPost(_user.value!!.uid).asLiveData()
    }

    fun getNumPostUser(): LiveData<Int> {
        return postRepo.getNumPost(_user.value!!.uid).asLiveData()
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