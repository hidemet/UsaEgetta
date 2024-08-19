package com.example.hammami.database

import android.net.Uri
import com.example.hammami.models.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDb @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val usersCollection = firestore.collection("users")

    suspend fun createUser(email: String, password: String) =
        auth.createUserWithEmailAndPassword(email, password).await().user!!

    fun logoutUser() {
        auth.signOut()
    }

    suspend fun saveUserInformation(userUid: String, user: User) =
        usersCollection.document(userUid).set(user).await()

    suspend fun loginUser(email: String, password: String) =
        auth.signInWithEmailAndPassword(email, password).await().user!!

    suspend fun resetPassword(email: String) =
        auth.sendPasswordResetEmail(email).await()

    fun getCurrentUser() = auth.currentUser

    suspend fun getUserProfile(uid: String): User =
        usersCollection.document(uid).get().await().toObject(User::class.java)
            ?: throw Exception("User not found")

    suspend fun getCurrentUserProfile(): User {
        val uid = getCurrentUser()?.uid ?: throw Exception("User not authenticated")
        return getUserProfile(uid)
    }

    suspend fun reauthenticateUser(currentPassword: String) {
        val user = getCurrentUser() ?: throw Exception("User not authenticated")
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential).await()
    }

suspend fun updateUserProfile(user: User) {
    val uid = getCurrentUser()?.uid ?: throw Exception("User not authenticated")
    usersCollection.document(uid).set(user).await()

    // Se l'email è stata modificata, verifichiamo prima di aggiornarla
    if (user.email != getCurrentUser()?.email) {
        try {
            getCurrentUser()?.verifyBeforeUpdateEmail(user.email)?.await()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw Exception("The email address is malformed")
        } catch (e: FirebaseAuthUserCollisionException) {
            throw Exception("There already exists an account with the given email address")
        } catch (e: FirebaseAuthInvalidUserException) {
            throw Exception("The current user's account has been disabled, deleted, or its credentials are no longer valid")
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            throw Exception("The user's last sign-in time does not meet the security threshold. Please reauthenticate.")
        } catch (e: FirebaseAuthException) {
            throw Exception("Operation not allowed: ${e.message}")
        }
    }
}

    suspend fun uploadProfileImage(imageUri: Uri): String {
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("profile_images/$filename")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deleteUserProfile() {
        val uid = getCurrentUser()?.uid ?: throw Exception("User not authenticated")
        usersCollection.document(uid).delete().await()
        getCurrentUser()?.delete()?.await()
    }
}