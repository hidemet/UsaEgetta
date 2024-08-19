package com.example.hammami.util


sealed class RegisterValidation() {
    object Success : RegisterValidation()
    data class Failed(val message: String) : RegisterValidation()
}

data class RegisterFieldsState(
    val firstName: RegisterValidation,
    val lastName: RegisterValidation,
    val email: RegisterValidation,
    val password: RegisterValidation,
    val birthDate: RegisterValidation,
    val gender: RegisterValidation,
    val allergies: RegisterValidation,
    val disabilities: RegisterValidation,
)