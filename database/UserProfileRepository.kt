package com.example.hammami.database

import com.example.hammami.models.User
import com.example.hammami.util.NetworkUtils
import com.example.hammami.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val firebaseDb: FirebaseDb,
    private val networkUtils: NetworkUtils
) {
    private val _currentUserProfile = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val currentUserProfile: StateFlow<Resource<User>> = _currentUserProfile.asStateFlow()

    suspend fun updateUserProfile(updatedUser: User, currentPassword: String? = null): Resource<User> {
        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("No internet connection")
        }
        return try {
            if (currentPassword != null) {
                // Riautenticazione richiesta per operazioni sensibili
                firebaseDb.reauthenticateUser(currentPassword)
            }
            firebaseDb.updateUserProfile(updatedUser)
            _currentUserProfile.value = Resource.Success(updatedUser)
            Resource.Success(updatedUser)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update user profile")
        }
    }

    suspend fun fetchCurrentUserProfile(): Resource<User> {
        return try {
            val user = firebaseDb.getCurrentUserProfile()
            _currentUserProfile.value = Resource.Success(user)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch current user profile")
        }
    }

    suspend fun uploadProfileImage(imageUri: android.net.Uri): Resource<String> {
        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("No internet connection")
        }
        return try {
            val imageUrl = firebaseDb.uploadProfileImage(imageUri)
            Resource.Success(imageUrl)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload profile image")
        }
    }

    suspend fun deleteUserProfile(): Resource<Unit> {
        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("No internet connection")
        }
        return try {
            firebaseDb.deleteUserProfile()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete user profile")
        }
    }
}