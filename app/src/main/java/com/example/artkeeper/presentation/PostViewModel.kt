package com.example.artkeeper.presentation

import android.net.Uri
import androidx.lifecycle.*
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.repository.PostRepository
import com.example.artkeeper.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PostViewModel(private val repo: PostRepository, userRepo: UserRepository) :
    ViewModel() {

    private val uid: String = FirebaseAuth.getInstance().currentUser!!.uid
    val user: LiveData<User> = userRepo.getUser(uid).asLiveData()
    private var _imageUri: MutableLiveData<Uri?> = MutableLiveData(null)
    val imageUri: LiveData<Uri?>
        get() = _imageUri
    private var _childName: MutableLiveData<String?> = MutableLiveData(null)
    private var _description: MutableLiveData<String?> = MutableLiveData(null)
    val description: LiveData<String?>
        get() = _description

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
    }

    fun checkPost(): Boolean {
        if (_imageUri.value == null)
            return false

        return true
    }

    private fun createPost(): Post {

        return Post(
            0,
            uid,
            user.value!!.nickName,
            _imageUri.value!!,
            0,
            _childName.value,
            _description.value,
            true,
            getTimestamp()
        )
    }

    fun insert() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.insert(createPost())
        }
    }

    private fun getTimestamp(): Long {
        val calendar = Calendar.getInstance()
        return Date(calendar.time.time).time

    }

}

class PostViewModelFactory(
    private val postRepo: PostRepository,
    private val userRepo: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PostViewModel(postRepo, userRepo) as T
    }
}