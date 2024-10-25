package com.example.hammami.domain.model

import android.util.Log
import com.google.firebase.Timestamp
import java.util.Date
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.DocumentId

data class Coupon(
    @DocumentId val id: String = "",
    @PropertyName("code") val code: String = "",
    @PropertyName("value") val value: Int = 0,
    @PropertyName("type") val type: CouponType = CouponType.POINTS_REWARD,
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("expirationDate") val expirationDate: Timestamp = Timestamp.now(),
    @PropertyName("isUsed") val isUsed: Boolean = false,
    @PropertyName("usedDate") val usedDate: Timestamp? = null,
    @PropertyName("userId") val userId: String = "",
    @PropertyName("usedInBooking") val usedInBooking: String? = null
) {
    fun isValid(): Boolean = !isUsed && !isExpired()

    fun isExpired(): Boolean = expirationDate.toDate().before(Date())
}
