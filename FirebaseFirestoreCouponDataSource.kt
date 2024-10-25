package com.example.hammami.data.datasource.coupon

import android.util.Log
import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.domain.model.Coupon
import com.example.hammami.domain.model.CouponType
import com.example.hammami.domain.model.PointsHistory
import com.example.hammami.domain.model.PointsHistoryType
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

import com.example.hammami.util.FirestoreCollections
import com.example.hammami.util.FirestoreFields
import com.google.firebase.firestore.DocumentReference


@Singleton
class FirebaseFirestoreCouponDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val couponsCollection = firestore.collection(FirestoreCollections.COUPONS)
    private val usersCollection = firestore.collection(FirestoreCollections.USERS)

    suspend fun createCouponDocument(coupon: Coupon) {
        val docRef = couponsCollection.document()
        docRef.set(coupon.copy(id = docRef.id)).await()
    }

    suspend fun getCouponsByUserId(userId: String): QuerySnapshot =
        couponsCollection
            .whereEqualTo(FirestoreFields.USER_ID, userId)
            .get()
            .await()

    suspend fun getCouponByCode(code: String): QuerySnapshot =
        couponsCollection
            .whereEqualTo(FirestoreFields.CODE, code)
            .limit(1)
            .get()
            .await()

    suspend fun getUserDocument(userId: String): DocumentSnapshot =
        usersCollection
            .document(userId)
            .get()
            .await()

    suspend fun <T> executeTransaction(operation: (Transaction) -> T): T =
        firestore.runTransaction { transaction ->
            operation(transaction)
        }.await()

    suspend fun <T> executeTransactionSuspend(operation: suspend (Transaction) -> T): T =
        firestore.runTransaction { transaction ->
            kotlinx.coroutines.runBlocking {
                operation(transaction)
            }
        }.await()

    suspend fun createCouponWithPoints(
        coupon: Coupon,
        pointsHistory: PointsHistory,
        userId: String
    ) {
        val references = getTransactionReferences(userId)

        executeTransaction { transaction ->
            val currentPoints = getCurrentPoints(transaction, references.userRef)

            with(references) {
                transaction.set(couponRef, coupon.copy(id = couponRef.id))
                transaction.set(pointsHistoryRef, pointsHistory)
                transaction.update(userRef, FirestoreFields.POINTS, currentPoints - pointsHistory.points)
            }
        }
    }

    suspend fun updateCouponUsage(
        couponRef: DocumentReference,
        bookingId: String
    ) = executeTransaction { transaction ->
        transaction.update(
            couponRef,
            mapOf(
                "isUsed" to true,
                "usedDate" to com.google.firebase.Timestamp.now(),
                "usedInBooking" to bookingId
            )
        )
    }

    suspend fun updateUserPoints(
        userId: String,
        pointsDelta: Int
    ) = executeTransaction { transaction ->
        val userRef = getUserRef(userId)
        val currentPoints = getCurrentPoints(transaction, userRef)
        transaction.update(userRef, FirestoreFields.POINTS, currentPoints + pointsDelta)
    }

    fun getTransactionReferences(userId: String) = TransactionReferences(
        couponRef = couponsCollection.document(),
        pointsHistoryRef = getUserPointsHistoryRef(userId).document(),
        userRef = getUserRef(userId)
    )

    fun getUserRef(userId: String): DocumentReference =
        usersCollection.document(userId)

    fun getCouponRef(): DocumentReference =
        couponsCollection.document()

    fun getPointsHistoryRef(userId: String): DocumentReference =
        getUserPointsHistoryRef(userId).document()

    private fun getUserPointsHistoryRef(userId: String) =
        usersCollection
            .document(userId)
            .collection(FirestoreCollections.POINTS_HISTORY)

    private fun getCurrentPoints(
        transaction: Transaction,
        userRef: DocumentReference
    ): Long {
        val userDoc = transaction.get(userRef)
        return userDoc.getLong(FirestoreFields.POINTS) ?: 0L
    }

    suspend fun batchWriteOperation(operations: (Transaction) -> Unit) {
        executeTransaction { transaction ->
            operations(transaction)
        }
    }

    suspend fun addPointsHistoryEntry(
        userId: String,
        pointsHistory: PointsHistory
    ) {
        val historyRef = getUserPointsHistoryRef(userId).document()
        historyRef.set(pointsHistory).await()
    }
}

data class TransactionReferences(
    val couponRef: DocumentReference,
    val pointsHistoryRef: DocumentReference,
    val userRef: DocumentReference
)