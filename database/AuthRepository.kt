package com.example.hammami.database

import com.example.hammami.models.User
import com.example.hammami.util.PreferencesManager
import com.example.hammami.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseDb: FirebaseDb,
    private val preferencesManager: PreferencesManager
) {
    private val _authState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val authState: StateFlow<Resource<User>> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        if (preferencesManager.isUserLoggedIn()) {
            firebaseDb.getCurrentUser()?.let { user ->
                CoroutineScope(Dispatchers.IO).launch {
                    fetchUserData(user.uid)
                }
            } ?: run {
                _authState.value = Resource.Error("User not authenticated")
                preferencesManager.setLoggedIn(false)
            }
        } else {
            _authState.value = Resource.Error("User not registered")
        }
    }

    suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            val user = firebaseDb.loginUser(email, password)
            preferencesManager.setLoggedIn(true)
            fetchUserData(user.uid)
            _authState.value
        } catch (e: Exception) {
            preferencesManager.setLoggedIn(false)
            Resource.Error(e.message ?: "Unknown error during login")
        }
    }

    fun signOut() {
        firebaseDb.logoutUser()
        preferencesManager.setLoggedIn(false)
        _authState.value = Resource.Error("User has been logged out")
    }

    suspend fun signUp(email: String, password: String, userData: User): Resource<User> {
        return try {
            val user = firebaseDb.createUser(email, password)
            firebaseDb.saveUserInformation(user.uid, userData)
            preferencesManager.setLoggedIn(true)
            Resource.Success(userData)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error during registration")
        }
    }

    suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            firebaseDb.resetPassword(email)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send password reset email")
        }
    }

    suspend fun refreshAuthToken() {
        try {
            firebaseDb.getCurrentUser()?.let { user ->
                user.getIdToken(true).await()
                fetchUserData(user.uid)
            } ?: run {
                _authState.value = Resource.Error("User not authenticated")
                preferencesManager.setLoggedIn(false)
            }
        } catch (e: Exception) {
            _authState.value = Resource.Error("Failed to refresh auth token: ${e.message}")
            preferencesManager.setLoggedIn(false)
        }
    }

    private suspend fun fetchUserData(uid: String) {
        try {
            val user = firebaseDb.getUserProfile(uid)
            _authState.value = Resource.Success(user)
        } catch (e: Exception) {
            _authState.value = Resource.Error("Failed to fetch user data: ${e.message}")
        }
    }
}