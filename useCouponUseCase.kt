package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.CouponRepository
import com.example.hammami.domain.usecase.DataError
import com.example.hammami.domain.usecase.Result
import javax.inject.Inject

class UseCouponUseCase @Inject constructor(
    private val couponRepository: CouponRepository
) {
    suspend operator fun invoke(couponCode: String, bookingId: String): Result<Unit, DataError> {
        if (couponCode.isBlank() ) {
            return Result.Error(DataError.User.INVALID_INPUT)
        }
        if (bookingId.isBlank()) {
            return Result.Error(DataError.User.INVALID_INPUT)
        }

        // Validazione del coupon prima dell'utilizzo
        return when (val validationResult = couponRepository.validateCoupon(couponCode)) {
            is Result.Success -> couponRepository.useCoupon(couponCode, bookingId)
            is Result.Error -> Result.Error(validationResult.error)
        }
    }
}