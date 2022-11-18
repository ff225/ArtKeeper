package com.example.artkeeper.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.artkeeper.data.model.User
import com.example.artkeeper.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegistrationViewModel(private val userRepository: UserRepository) : ViewModel() {

    private var _name = ""
    private var _lastName = ""
    private var _nickName = ""

    init {
        reset()
    }

    fun setName(name: String) {
        _name = name
    }

    fun setLastName(lastName: String) {
        _lastName = lastName
    }

    fun setNickname(nickName: String) {
        _nickName = nickName
    }


    fun confirmUserCreation(): Boolean {
        if (checkUserInfo()) {
            insertUser(createUser())
            return true
        }
        return false
    }

    private fun checkUserInfo(): Boolean {
        if (FirebaseAuth.getInstance().uid == null
            || _name == ""
            || _lastName == ""
            || _nickName == ""
        )
            return false
        return true
    }

    private fun createUser(): User {
        return User(
            FirebaseAuth.getInstance().uid.toString(),
            _name,
            _lastName,
            _nickName,
            1,
            "Francesco"
        )
    }

    private fun insertUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.insertUser(user)
        }
    }


    fun reset() {
        _name = ""
        _lastName = ""
        _nickName = ""

    }
}

class RegistrationViewModelFactory(private val userRepository: UserRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegistrationViewModel(userRepository) as T
    }
}