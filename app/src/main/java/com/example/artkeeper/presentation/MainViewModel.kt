package com.example.artkeeper.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.artkeeper.data.repository.UserRepository
import com.example.artkeeper.workers.UpdateNickname
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(userRepository: UserRepository, private val workManager: WorkManager) :
    ViewModel() {

    private val TAG = javaClass.simpleName

    init {
        viewModelScope.launch {
            userRepository.insertNicknames()
        }
        //updateNickname()
    }

    val searchQuery = MutableStateFlow("")
    private val nicknameFlow = searchQuery.flatMapLatest { query ->
        userRepository.getNicknames(query)

    }

    val nickname = nicknameFlow.asLiveData()

    fun updateNickname() {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateNicknameRequest = PeriodicWorkRequestBuilder<UpdateNickname>(1, TimeUnit.HOURS)
            .setConstraints(constraint)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "UpdateNicknameRequest",
            ExistingPeriodicWorkPolicy.KEEP,
            updateNicknameRequest
        )

        Log.d(TAG, "worker started")
    }
}

class MainViewModelFactory(
    private val userRepository: UserRepository,
    private val workManager: WorkManager
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(userRepository, workManager) as T
    }
}