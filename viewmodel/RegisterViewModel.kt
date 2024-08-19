package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.models.RegistrationData
import com.example.hammami.models.User
import com.example.hammami.database.AuthRepository
import com.example.hammami.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registrationData = MutableStateFlow(RegistrationData())
    val registrationData: StateFlow<RegistrationData> = _registrationData.asStateFlow()

    private val _registrationState = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val registrationState: StateFlow<Resource<User>> = _registrationState

    fun updateRegistrationData(update: (RegistrationData) -> RegistrationData) {
        _registrationData.value = update(_registrationData.value)
    }

    fun createUser(email: String, password: String, userData: User) {
        viewModelScope.launch {
            _registrationState.value = Resource.Loading()
            _registrationState.value = authRepository.signUp(email, password, userData)
        }
    }

    fun clearRegistrationData() {
        _registrationData.value = RegistrationData()
    }
}