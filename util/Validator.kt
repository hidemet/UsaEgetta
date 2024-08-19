package com.example.hammami.util

interface Validator<T> {
    fun validate(value: T): ValidationResult
}