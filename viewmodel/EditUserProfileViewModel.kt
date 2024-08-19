package com.example.hammami.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.database.UserProfileRepository
import com.example.hammami.models.User
import com.example.hammami.database.AuthRepository
import com.example.hammami.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditUserProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Loading())
    val userProfile: StateFlow<Resource<User>> = _userProfile.asStateFlow()

    private val _profileUpdateEvent = MutableSharedFlow<ProfileUpdateResult>()
    val profileUpdateEvent: SharedFlow<ProfileUpdateResult> = _profileUpdateEvent.asSharedFlow()


    private val _passwordResetEvent = MutableSharedFlow<PasswordResetResult>()
    val passwordResetEvent = _passwordResetEvent.asSharedFlow()

    val user: StateFlow<Resource<User>> = authRepository.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading()
    )
    var imageUri: Uri? = null

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            _userProfile.value = userProfileRepository.fetchCurrentUserProfile()
        }
    }

    fun updateUserProfile(updatedUser: User, currentPassword: String? = null) {
        viewModelScope.launch {
                val result = if (currentPassword != null) {
                    userProfileRepository.updateUserProfile(updatedUser, currentPassword)
                } else {
                    userProfileRepository.updateUserProfile(updatedUser)
                }
                when (result) {
                    is Resource.Success -> {
                        _profileUpdateEvent.emit(ProfileUpdateResult.Success("Profilo aggiornato con successo"))
                        _userProfile.value = Resource.Success(result.data!!)
                    }

                    is Resource.Error -> {
                        _profileUpdateEvent.emit(
                            ProfileUpdateResult.Error(
                                result.message ?: "Errore nell'aggiornamento del profilo"
                            )
                        )
                    }
                    else -> {}
                }
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            val result = userProfileRepository.uploadProfileImage(imageUri)
            when (result) {
                is Resource.Success -> {
                    _profileUpdateEvent.emit(ProfileUpdateResult.Success("Image uploaded successfully"))
                    // Update the user profile with the new image URL
                    _userProfile.value.data?.let { currentUser ->
                        updateUserProfile(currentUser.copy(profileImage = result.data!!))
                    }
                }
                is Resource.Error -> {
                    _profileUpdateEvent.emit(ProfileUpdateResult.Error(result.message ?: "Failed to upload image"))
                }
                else -> {}
            }
        }
    }

    fun deleteUserProfile() {
        viewModelScope.launch {
            when (val result = userProfileRepository.deleteUserProfile()) {
                is Resource.Success -> {
                    _profileUpdateEvent.emit(ProfileUpdateResult.Success("Account deleted successfully"))
                }
                is Resource.Error -> {
                    _profileUpdateEvent.emit(ProfileUpdateResult.Error(result.message ?: "Failed to delete account"))
                }
                else -> {}
            }
        }
    }

    fun createImageUri(context: Context): Uri {
        val imageFile = File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            context.externalCacheDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            val result = authRepository.resetPassword(email)
            when (result) {
                is Resource.Success -> {
                    _passwordResetEvent.emit(PasswordResetResult.Success("Password reset email sent successfully"))
                }
                is Resource.Error -> {
                    _passwordResetEvent.emit(PasswordResetResult.Error(result.message ?: "Failed to send password reset email"))
                }
                else -> {}
            }
        }
    }

    sealed class ProfileUpdateResult {
        data class Success(val message: String) : ProfileUpdateResult()
        data class Error(val message: String) : ProfileUpdateResult()
    }

    sealed class PasswordResetResult {
        data class Success(val message: String) : PasswordResetResult()
        data class Error(val message: String) : PasswordResetResult()
    }
}