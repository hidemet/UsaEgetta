package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.hammami.database.UserProfileRepository


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    // Logica specifica per la Home...
}