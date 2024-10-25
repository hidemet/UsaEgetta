package com.example.hammami.data.repositories

import com.example.hammami.domain.model.User
import com.example.hammami.domain.usecase.Result

import com.example.hammami.domain.usecase.GetUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Rrepository condiviso per i dati utente
@Singleton
class UserStateRepository @Inject constructor(
    private val getUserUseCase: GetUserUseCase
) {
    private val _userData = MutableStateFlow<User?>(null)
    val userData = _userData.asStateFlow()

    suspend fun refreshUserData() {
        when (val result = getUserUseCase()) {
            is Result.Success -> _userData.value = result.data
            is Result.Error -> {} // Gestisci errore se necessario
        }
    }
}
