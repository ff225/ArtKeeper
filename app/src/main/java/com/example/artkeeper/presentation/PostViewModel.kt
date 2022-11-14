package com.example.artkeeper.presentation

import android.net.Uri
import androidx.lifecycle.*
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PostViewModel(private val repo: PostRepository) : ViewModel() {

    private val uid: String? = FirebaseAuth.getInstance().currentUser?.uid
    private var _imageUri: MutableLiveData<Uri?> = MutableLiveData(null)
    val imageUri: LiveData<Uri?>
        get() = _imageUri
    private var _description: MutableLiveData<String?> = MutableLiveData(null)
    val description: LiveData<String?>
        get() = _description

    val allPost: LiveData<List<Post>> = repo.getAllPost().asLiveData()


    fun setImageUri(imageUri: Uri) {
        _imageUri.value = imageUri
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun reset() {
        _imageUri.value = null
        _description.value = null
    }

    fun checkPost(): Boolean {
        if (_imageUri.value == null)
            return false

        return true
    }


    private fun createPost(): Post {

        return Post(
            0,
            uid!!,
            "Francesco",
            _imageUri.value!!,
            0,
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

class PostViewModelFactory(private val repo: PostRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PostViewModel(repo) as T
    }
}