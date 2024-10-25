package com.example.hammami.domain.usecase.coupon

import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.model.AvailableCoupon
import com.example.hammami.domain.usecase.DataError
import com.example.hammami.domain.usecase.Result
import javax.inject.Inject

class GetAvailableCouponsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<List<AvailableCoupon>, DataError> {
        return when (val userResult = userRepository.getUserData()) {
            is Result.Success -> {
                val userPoints = userResult.data.points
                Result.Success(generateAvailableCoupons(userPoints))
            }

            is Result.Error -> Result.Error(userResult.error)
        }
    }


    private fun generateAvailableCoupons(userPoints: Int): List<AvailableCoupon> {
        return listOf(
            AvailableCoupon(
                value = 10,
                requiredPoints = 50,
                description = "Sconto di 10€",
                isEnabled = userPoints >= 50
            ),
            AvailableCoupon(
                value = 20,
                requiredPoints = 100,
                description = "Sconto di 20€",
                isEnabled = userPoints >= 100
            ),
            AvailableCoupon(
                value = 30,
                requiredPoints = 150,
                description = "Sconto di 30€",
                isEnabled = userPoints >= 150
            )
        )
    }
}