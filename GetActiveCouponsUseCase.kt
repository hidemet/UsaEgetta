package com.example.hammami.domain.usecase.coupon

import android.util.Log
import com.example.hammami.core.util.asUiText
import com.example.hammami.data.repositories.CouponRepository
import com.example.hammami.domain.model.Coupon
import com.example.hammami.domain.usecase.DataError
import com.example.hammami.domain.usecase.Result
import javax.inject.Inject

class GetActiveCouponsUseCase @Inject constructor(
    private val couponRepository: CouponRepository,
) {
    suspend operator fun invoke(): Result<List<Coupon>, DataError> {

        return when (val result = couponRepository.getUserCoupons()) {
            is Result.Success -> handleSuccessResult(result.data)
            is Result.Error -> handleErrorResult(result.error)
        }
    }

    private fun handleSuccessResult(coupons: List<Coupon>): Result<List<Coupon>, DataError> {
        val validCoupons = coupons.filter { it.isValid() }
        return Result.Success(validCoupons)
    }

    private fun handleErrorResult(error: DataError): Result<List<Coupon>, DataError> {
        return Result.Error(error)
    }
}