package com.example.artkeeper.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import com.example.artkeeper.data.PostDao
import com.example.artkeeper.data.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class PostViewModel(private val postDao: PostDao) : ViewModel() {


    private val _postList: MutableList<Post> = mutableListOf()
    val postList: List<Post> = _postList

    private val _numOfPost = MutableLiveData(0)
    val numOfPost = _numOfPost

    private val _image = MutableLiveData<Uri?>()
    val image: LiveData<Uri?> = _image

    private val _description = MutableLiveData("")
    val description: LiveData<String> = _description

    init {
        getAllPost()
        reset()
    }

    fun setImage(image: Uri) {
        _image.postValue(image)
    }

    private fun saveImageToInternalStorage(image: Uri): Uri {

        return image
    }

    fun setDescription(description: String) {
        _description.postValue(description)
    }

    fun sharePost(
        /* image: Bitmap,
         nickname: String,
         nLike: Int,
         description: String = "",
         isUploaded: Boolean = false*/
    ) {
        var post = getNewPostItem(
            _image.value!!,
            "Francesco",
            description = _description.value!!,
            timestamp = getTimestamp()
        )
        insertPost(post)
        reset()
    }

    private fun getTimestamp(): Long {
        val calendar = Calendar.getInstance()
        return Date(calendar.time.time).time

    }

    private fun getNewPostItem(
        image: Uri,
        nickname: String,
        nLike: Int = 0,
        description: String = "",
        isUploaded: Boolean = false,
        timestamp: Long

    ): Post {
        return Post(0, image, nickname, nLike, description, isUploaded, timestamp)
    }

    private fun insertPost(post: Post) {
        viewModelScope.launch(Dispatchers.IO) {
            postDao.insert(post)
        }
    }

    fun getAllPost() =
        postDao.getPosts()

    /*
    private fun printAllPost() {
        Log.i("POST", _postList.size.toString())
        for (item in _postList)
            Log.i("POST", "id: ${item.id}, description:${item.description}")
    }
*/
    //TODO modificare nickname con quello del login
    fun getNumOfPost(nickname: String) = postDao.getNumPost(nickname)

    fun getUserPosts(nickname: String) = postDao.getUserPosts(nickname)

    fun reset() {
        _postList.clear()
        _image.postValue(null)
        _description.postValue("")
        _numOfPost.postValue(0)
    }
}

class PostViewModelFactory(private val postDao: PostDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(postDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}