package com.example.hammami.presentation.ui.fragments.userProfile.coupon

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.R
import com.example.hammami.core.ui.UiText
import com.example.hammami.core.util.asUiText
import com.example.hammami.domain.model.AvailableCoupon
import com.example.hammami.domain.model.Coupon
import com.example.hammami.domain.model.User
import com.example.hammami.domain.usecase.Result
import com.example.hammami.domain.usecase.coupon.GenerateCouponUseCase
import com.example.hammami.domain.usecase.coupon.GetActiveCouponsUseCase
import com.example.hammami.domain.usecase.coupon.GetAvailableCouponsUseCase
import com.example.hammami.domain.usecase.user.ObserveUserStateUseCase
import com.example.hammami.domain.usecase.user.RefreshUserStateUseCase
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.example.hammami.util.ClipboardManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponViewModel @Inject constructor(
    private val getAvailableCouponsUseCase: GetAvailableCouponsUseCase,
    private val getActiveCouponsUseCase: GetActiveCouponsUseCase,
    private val generateCouponUseCase: GenerateCouponUseCase,
    private val clipboardManager: ClipboardManager,
    private val observeUserStateUseCase: ObserveUserStateUseCase,
    private val refreshUserStateUseCase: RefreshUserStateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CouponState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    init {
        // Inizia osservando i dati utente
        observeUserState()
        // Carica i dati iniziali
        loadData()
    }


    private fun observeUserState() {
        viewModelScope.launch {
            observeUserStateUseCase()
                .collect { result ->
                    when (result) {
                        is Result.Success -> updateUserData(result.data)
                        is Result.Error -> emitUiEvent(UiEvent.ShowUserMessage(result.error.asUiText()))
                    }
                }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            loadCoupons()
            updateState { copy(isLoading = false) }
        }
    }

    private suspend fun loadCoupons() {
        when (val activeResult = getActiveCouponsUseCase()) {
            is Result.Success -> updateState { copy(activeCoupons = activeResult.data) }
            is Result.Error -> emitUiEvent(UiEvent.ShowUserMessage(activeResult.error.asUiText()))
        }

        when (val availableResult = getAvailableCouponsUseCase()) {
            is Result.Success -> updateState { copy(availableCoupons = availableResult.data) }
            is Result.Error -> emitUiEvent(UiEvent.ShowUserMessage(availableResult.error.asUiText()))
        }
    }


    fun onCouponSelected(value: Int) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }

            when (val result = generateCouponUseCase(value)) {
                is Result.Success -> {
                    refreshUserStateUseCase()
                    loadCoupons()
                }
                is Result.Error -> emitUiEvent(UiEvent.ShowUserMessage(result.error.asUiText()))
            }

            updateState { copy(isLoading = false) }
        }
    }

    fun copyCouponToClipboard(code: String) {
        clipboardManager.copyToClipboard(code)
        viewModelScope.launch {
            emitUiEvent(UiEvent.ShowUserMessage(UiText.StringResource(R.string.coupon_copied)))
        }
    }

    private suspend fun emitUiEvent(event: UiEvent) {
        Log.d("CouponViewModel", "Emitting UI event: $event")
        _uiEvent.emit(event)
    }

    private fun updateState(update: CouponState.() -> CouponState) {
        _state.value = update(_state.value)
    }

    private fun updateUserData(user: User?) {
        updateState { copy(userPoints = user?.points ?: 0) }
    }


    data class CouponState(
        val userPoints: Int = 0,
        val availableCoupons: List<AvailableCoupon> = emptyList(),
        val activeCoupons: List<Coupon> = emptyList(),
        val isLoading: Boolean = false
    ) {
        val canRedeemCoupons: Boolean get() = userPoints >= 50
        val hasActiveCoupons: Boolean get() = activeCoupons.isNotEmpty()

    }

    sealed class UiEvent {
        data class ShowUserMessage(val message: UiText) : UiEvent()
    }
}