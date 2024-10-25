package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.CouponRepository
import com.example.hammami.domain.model.Coupon
import com.example.hammami.domain.usecase.DataError
import com.example.hammami.domain.usecase.Result
import com.example.hammami.util.CouponConstants
import javax.inject.Inject

class ValidateCouponUseCase @Inject constructor(
    private val couponRepository: CouponRepository
) {
    suspend operator fun invoke(code: String): Result<Coupon, DataError> {
        if (!isValidCouponInput(code)) {
            return Result.Error(DataError.User.INVALID_INPUT)
        }
        return couponRepository.validateCoupon(code)
    }

    private fun isValidCouponInput(code: String): Boolean =
        code.isNotBlank() && CouponConstants.COUPON_CODE_PATTERN.matches(code)
}