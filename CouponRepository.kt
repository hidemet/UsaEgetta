package com.example.hammami.data.repositories

import com.example.hammami.data.datasource.coupon.FirebaseFirestoreCouponDataSource
import com.example.hammami.domain.model.*
import com.example.hammami.domain.usecase.DataError
import com.example.hammami.domain.usecase.Result
import com.example.hammami.util.CouponConstants
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepository @Inject constructor(
    private val dataSource: FirebaseFirestoreCouponDataSource,
    private val authRepository: AuthRepository
) {
    suspend fun generateCoupon(value: Int): Result<Coupon, DataError> {
        return try {
            val userId = getUserId() ?: return Result.Error(DataError.Auth.NOT_AUTHENTICATED)
            val userDoc =
                getUserDocument(userId) ?: return Result.Error(DataError.User.USER_NOT_FOUND)

            val currentPoints = userDoc.getLong("points")?.toInt() ?: 0
            val requiredPoints = calculateRequiredPoints(value)

            if (currentPoints < requiredPoints) {
                return Result.Error(DataError.User.INSUFFICIENT_POINTS)
            }

            generateCouponWithPoints(userId, value, currentPoints, requiredPoints)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }

    suspend fun getUserCoupons(): Result<List<Coupon>, DataError> {
        return try {
            val userId = getUserId() ?: return Result.Error(DataError.Auth.NOT_AUTHENTICATED)

            Result.Success(
                dataSource.getCouponsByUserId(userId).documents
                    .mapNotNull { it.toObject(Coupon::class.java) }
            )

        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }

    suspend fun validateCoupon(code: String): Result<Coupon, DataError> {
        return try {
            val coupon =
                getCouponByCode(code) ?: return Result.Error(DataError.User.COUPON_NOT_FOUND)
            validateCouponStatus(coupon)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }

    suspend fun useCoupon(couponCode: String, bookingId: String): Result<Unit, DataError> {
        return try {
            val userId = getUserId() ?: return Result.Error(DataError.Auth.NOT_AUTHENTICATED)
            val couponDoc = getCouponDocument(couponCode)
                ?: return Result.Error(DataError.User.COUPON_NOT_FOUND)

            val coupon = couponDoc.toObject(Coupon::class.java)
                ?: return Result.Error(DataError.User.COUPON_NOT_FOUND)

            validateCouponUsage(coupon, userId)?.let { return it }

            dataSource.updateCouponUsage(dataSource.getCouponRef(), bookingId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }

    private suspend fun generateCouponWithPoints(
        userId: String,
        value: Int,
        currentPoints: Int,
        requiredPoints: Int
    ): Result<Coupon, DataError> {
        val coupon = createCoupon(value, userId)
        val pointsHistory = createPointsHistory(requiredPoints, coupon.id)

        return try {
            dataSource.createCouponWithPoints(coupon, pointsHistory, userId)
            Result.Success(coupon)
        } catch (e: Exception) {
            Result.Error(mapException(e))
        }
    }

    private fun validateCouponStatus(coupon: Coupon): Result<Coupon, DataError> {
        return when {
            coupon.isUsed -> Result.Error(DataError.User.COUPON_ALREADY_USED)
            coupon.isExpired() -> Result.Error(DataError.User.COUPON_EXPIRED)
            else -> Result.Success(coupon)
        }
    }

    private fun validateCouponUsage(
        coupon: Coupon,
        userId: String
    ): Result.Error<Unit, DataError>? {
        return when {
            coupon.userId != userId -> Result.Error(DataError.User.PERMISSION_DENIED)
            coupon.isUsed -> Result.Error(DataError.User.COUPON_ALREADY_USED)
            coupon.isExpired() -> Result.Error(DataError.User.COUPON_EXPIRED)
            else -> null
        }
    }

    private fun createCoupon(value: Int, userId: String) = Coupon(
        code = generateUniqueCode(value),
        value = value,
        type = CouponType.POINTS_REWARD,
        userId = userId,
        createdAt = Timestamp.now(),
        expirationDate = calculateExpirationDate()
    )

    private fun createPointsHistory(points: Int, referenceId: String?) = PointsHistory(
        points = points,
        type = PointsHistoryType.SPENT,
        reason = "Riscatto Coupon",
        referenceId = referenceId,
        timestamp = Timestamp.now()
    )

    private suspend fun getUserId(): String? = authRepository.getCurrentUserId()

    private suspend fun getUserDocument(userId: String): DocumentSnapshot? =
        try {
            dataSource.getUserDocument(userId)
        } catch (e: Exception) {
            null
        }

    private suspend fun getCouponByCode(code: String): Coupon? =
        try {
            dataSource.getCouponByCode(code).documents.firstOrNull()?.toObject(Coupon::class.java)
        } catch (e: Exception) {
            null
        }

    private suspend fun getCouponDocument(code: String): DocumentSnapshot? =
        try {
            dataSource.getCouponByCode(code).documents.firstOrNull()
        } catch (e: Exception) {
            null
        }

    private fun generateUniqueCode(value: Int): String {
        val charPool = ('A'..'Z') + ('0'..'9')
        val random = SecureRandom()
        val timestamp = System.currentTimeMillis()
        val randomPart = (1..CouponConstants.RANDOM_CODE_LENGTH)
            .map { charPool[random.nextInt(charPool.size)] }
            .joinToString("")

        return buildString {
            append(timestamp)
            append(randomPart)
            append(value.toString().padStart(CouponConstants.VALUE_CODE_LENGTH, '0'))
        }
    }

    private fun calculateExpirationDate(): Timestamp =
        Timestamp(
            Date.from(
                LocalDateTime.now()
                    .plusYears(CouponConstants.EXPIRATION_YEARS)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            )
        )

    private fun calculateRequiredPoints(value: Int): Int =
        value * CouponConstants.POINTS_MULTIPLIER

    private fun mapException(e: Exception): DataError = when (e) {
        is FirebaseFirestoreException -> when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> DataError.User.PERMISSION_DENIED
            FirebaseFirestoreException.Code.NOT_FOUND -> DataError.User.USER_NOT_FOUND
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> DataError.User.COUPON_ALREADY_EXISTS
            FirebaseFirestoreException.Code.CANCELLED -> DataError.Network.OPERATION_CANCELLED
            else -> DataError.Network.UNKNOWN
        }

        is FirebaseNetworkException -> DataError.Network.NO_INTERNET
        else -> DataError.Network.UNKNOWN
    }

}