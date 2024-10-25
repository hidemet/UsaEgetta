package com.example.hammami.domain.model


data class AvailableCoupon(
    val value: Int,
    val requiredPoints: Int,
    val description: String = "Sconto di $value€",
    val isEnabled: Boolean = true
) {
    fun canBeRedeemed(userPoints: Int): Boolean =
        isEnabled && userPoints >= requiredPoints
}