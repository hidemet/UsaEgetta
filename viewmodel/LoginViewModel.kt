package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.models.User
import com.example.hammami.database.AuthRepository
import com.example.hammami.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableSharedFlow<Resource<User>>()
    val loginState: SharedFlow<Resource<User>> = _loginState

    private val _resetPasswordState = MutableSharedFlow<Resource<Unit>>()
    val resetPasswordState: SharedFlow<Resource<Unit>> = _resetPasswordState

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginState.emit(Resource.Loading())
            val result = authRepository.signIn(email, password)
            _loginState.emit(result)
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.emit(Resource.Loading())
            val result = authRepository.resetPassword(email)
            _resetPasswordState.emit(result)
        }
    }
}