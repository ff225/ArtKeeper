package com.example.artkeeper.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.example.artkeeper.data.repository.PostRepository
import com.example.artkeeper.data.repository.UserRepository
import com.example.artkeeper.utils.Resource


class VisitedUserProfileViewModel(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {


    fun getInfoUser(uid_user: String) = liveData {
        emit(Resource.Loading())
        userRepository.getUserRemote(uid_user).onSuccess {
            emit(Resource.Success(it))
        }.onFailure {
            Log.d(javaClass.simpleName, it.message.toString())
            emit(Resource.Failure(Exception()))
        }
    }

    fun getPostUser(uid_user: String) = liveData {
        emit(Resource.Loading())
        postRepository.getAllPostRemote(uid_user).onSuccess {
            emit(Resource.Success(it))
        }.onFailure {
            emit(Resource.Failure(Exception()))
        }
    }
}

@Suppress("UNCHECKED_CAST")
class VisitedUserProfileViewModelFactory(
    private val userRepo: UserRepository,
    private val postRepository: PostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VisitedUserProfileViewModel(userRepo, postRepository) as T
    }
}