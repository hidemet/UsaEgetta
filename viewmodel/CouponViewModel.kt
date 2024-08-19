package com.example.hammami.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hammami.database.AuthRepository
import com.example.hammami.database.UserProfileRepository
import com.example.hammami.models.Coupon
import com.example.hammami.models.User
import com.example.hammami.util.ClipboardManager
import com.example.hammami.util.CouponListResource

import com.example.hammami.util.Resource
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CouponViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val clipboardManager: ClipboardManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _availableCouponValues = MutableStateFlow<List<Int>>(emptyList())
    val availableCouponValues: StateFlow<List<Int>> = _availableCouponValues.asStateFlow()

    private val _activeCoupons = MutableStateFlow<CouponListResource>(Resource.Unspecified())
    val activeCoupons: StateFlow<CouponListResource> = _activeCoupons.asStateFlow()

    private val _couponGenerationResult = MutableStateFlow<Resource<Coupon>>(Resource.Unspecified())
    val couponGenerationResult: StateFlow<Resource<Coupon>> = _couponGenerationResult.asStateFlow()

    val user: StateFlow<Resource<User>> = authRepository.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading()
    )



    init {
        loadCoupons()
    }

    fun loadCoupons() {
        loadAvailableCoupons()
        loadActiveCoupons()
    }

    private fun loadAvailableCoupons() {
        viewModelScope.launch {
            _availableCouponValues.value = listOf(10, 20, 30) // Example values
        }
    }

    private fun loadActiveCoupons() {
        viewModelScope.launch {
            user.collect { userResource ->
                when (userResource) {
                    is Resource.Success -> _activeCoupons.value = Resource.Success(userResource.data!!.coupons)
                    is Resource.Error -> _activeCoupons.value = Resource.Error(userResource.message ?: "Errore sconosciuto")
                    is Resource.Loading -> _activeCoupons.value = Resource.Loading()
                    is Resource.Unspecified -> _activeCoupons.value = Resource.Unspecified()
                }
            }
        }
    }

    private fun createCoupon(value: Int): Coupon {
        val code = generateCouponCode(value)
        val exiprationDate = calculateExpirationDate()
        return Coupon(code, value, exiprationDate)

    }

    private fun addCouponToUser(coupon: Coupon) {
        viewModelScope.launch {
            val currentUserResource = user.value
            if (currentUserResource is Resource.Success) {
                val currentUser = currentUserResource.data!!
                if (currentUser.points >= coupon.value * 5) {
                    val updatedUser = currentUser.copy(
                        points = currentUser.points - coupon.value * 5,
                        coupons = currentUser.coupons + coupon
                    )
                    try {
                        updateUserProfile(updatedUser)
                        _couponGenerationResult.value = Resource.Success(coupon)
                    } catch (e: Exception) {
                        _couponGenerationResult.value =
                            Resource.Error("Errore durante l'acquisto del coupon")
                    }
                } else {
                    _couponGenerationResult.value = Resource.Error("Punti insufficienti")
                }
            }
        }
    }


    private fun generateCouponCode(value: Int): String {
        val charPool: List<Char> = ('A'..'Z') + ('0'..'9')
        val random = SecureRandom()
        val timestamp = System.currentTimeMillis()
        val randomPart = (1..4)
            .map { charPool[random.nextInt(charPool.size)] }
            .joinToString("")
        return "${timestamp}${randomPart}${value.toString().padStart(4, '0')}"

    }

    private fun calculateExpirationDate(): Timestamp = Timestamp(
        Date.from(
            LocalDateTime.now().plusYears(1).atZone(
                ZoneId.systemDefault()
            ).toInstant()
        )
    )


// ------------------ Update the loadCoupons method in CouponViewModel ------------------

    fun onCouponSelected(value: Int) {
        val coupon = createCoupon(value)
        addCouponToUser(coupon)
    }


    private fun updateUserProfile(updatedUser: User) {
        viewModelScope.launch {
            userProfileRepository.updateUserProfile(updatedUser)
        }
    }


    fun copyCouponToClipboard(couponCode: String) {
        clipboardManager.copyToClipboard(couponCode)
    }
}