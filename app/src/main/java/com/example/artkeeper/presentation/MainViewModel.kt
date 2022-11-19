package com.example.artkeeper.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.data.repository.PostRepository

class MainViewModel(private val postRepo: PostRepository) : ViewModel() {

    val allPost: LiveData<List<Post>> = postRepo.getAllPost().asLiveData()
}

class MainViewModelFactory(private val postRepo: PostRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(postRepo) as T
    }
}