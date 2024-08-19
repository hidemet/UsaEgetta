package com.example.hammami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.database.FirebaseDb
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GiftCardsViewModel @Inject constructor(
    private val firebaseDb: FirebaseDb,
): ViewModel() {

    private val _giftCardValues = MutableStateFlow<List<Int>>(emptyList())
    val giftCardValues: StateFlow<List<Int>> = _giftCardValues

    private val _navigationEvent = MutableSharedFlow<Int>()
    val navigationEvent: SharedFlow<Int> = _navigationEvent

    init {
        loadGiftCardValues()
    }

    private fun loadGiftCardValues() {
        // Popola l'array con i valori delle gift card
        _giftCardValues.value = listOf(50, 100,150,200,250,300,350,400,450,500,550,600,650,700,750,800,850,900,950,1000)
    }

    fun onGiftCardSelected(value: Int) {
        viewModelScope.launch {
            _navigationEvent.emit(value)
        }
    }
}