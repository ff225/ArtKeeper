package com.example.artkeeper.presentation

import androidx.lifecycle.*
import com.example.artkeeper.data.model.Nickname
import com.example.artkeeper.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val userRepository: UserRepository,
) :
    ViewModel() {

    private val TAG = javaClass.simpleName

    private val _nickNameList = MutableLiveData<List<Nickname>>()
    val nickNameList: LiveData<List<Nickname>>
        get() = _nickNameList

    init {
        observeNicknames()
    }

    val searchQuery = MutableStateFlow(String())
    private fun observeNicknames() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                searchQuery.flatMapLatest { query ->
                    userRepository.getNicknames(query)
                }.collect {
                    _nickNameList.postValue(it)
                }

            }
        }
    }
}

class MainViewModelFactory(
    private val userRepository: UserRepository,
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(userRepository) as T
    }
}