package com.example.artkeeper.presentation

import android.util.Log
import androidx.lifecycle.*
import com.example.artkeeper.data.model.Nickname
import com.example.artkeeper.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PendingRequestViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _pendingReqList = MutableLiveData<List<Nickname>>()
    val pendingReqList: LiveData<List<Nickname>>
        get() = _pendingReqList

    fun observePendingReq(pendingReqList: List<String>) {
        Log.d(javaClass.simpleName, pendingReqList.toString())
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userRepository.getAllNicknamePendingReq(pendingReqList).collect {
                    _pendingReqList.postValue(it)
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class PendingRequestViewModelFactory(
    private val userRepo: UserRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PendingRequestViewModel(userRepo) as T
    }
}