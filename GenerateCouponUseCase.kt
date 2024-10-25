package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.CouponRepository
import com.example.hammami.domain.model.Coupon
import com.example.hammami.domain.usecase.Result
import com.example.hammami.domain.usecase.DataError
import com.example.hammami.util.CouponConstants

import javax.inject.Inject

class GenerateCouponUseCase @Inject constructor(
    private val couponRepository: CouponRepository
) {
    suspend operator fun invoke(value: Int): Result<Coupon, DataError> {
        return when {
            value <= 0 -> Result.Error(DataError.User.INVALID_INPUT)
            !CouponConstants.VALID_COUPON_VALUES.contains(value) -> Result.Error(DataError.User.INVALID_INPUT)
            else -> couponRepository.generateCoupon(value)
        }
    }
}
