package com.example.hammami.util

import android.util.Log
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object StringValidators {
    val NotBlank = object : Validator<String> {
        override fun validate(value: String) = when {
            value.isBlank() -> ValidationResult.Invalid("Questo campo è obbligatorio. Per favore, inserisci un valore.")
            else -> ValidationResult.Valid
        }
    }

    val Email = object : Validator<String> {
        override fun validate(value: String) = when {
            android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches() -> ValidationResult.Valid
            else -> ValidationResult.Invalid("L'indirizzo email inserito non è valido. Assicurati che sia nel formato corretto (es. nome@dominio.com).")
        }
    }

    val PhoneNumber = object : Validator<String> {
        private val phoneRegex = "^\\d{10}$".toRegex()
        override fun validate(value: String) = when {
            phoneRegex.matches(value) -> ValidationResult.Valid
            else -> ValidationResult.Invalid("Il numero di telefono deve contenere 10 cifre, senza spazi o altri caratteri. Ad esempio: 3401234567.")
        }
    }

    val Password = object : Validator<String> {
        private val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$".toRegex()
        override fun validate(value: String) = when {
            passwordRegex.matches(value) -> ValidationResult.Valid
            else -> ValidationResult.Invalid("La password deve avere almeno 8 caratteri e contenere almeno una lettera e un numero. Ad esempio: Password123.")
        }
    }

    val BirthDate = object : Validator<Triple<String, String, String>> {
        override fun validate(value: Triple<String, String, String>): ValidationResult {
            val (day, month, year) = value

            Log.d("BirthDateValidator", "Validating date: $day $month $year")

            return try {
                val italianLocale = Locale("it", "IT")
                val monthFormatter = DateTimeFormatter.ofPattern("MMMM", italianLocale)

                val parsedDay = day.toInt()
                val parsedMonth = Month.from(monthFormatter.parse(month))
                val parsedYear = year.toInt()

                val date = LocalDate.of(parsedYear, parsedMonth, parsedDay)

                Log.d("BirthDateValidator", "Parsed date: $date")
                when {
                    date.isAfter(LocalDate.now()) -> {
                        Log.d("BirthDateValidator", "Date is in the future")
                        ValidationResult.Invalid("La data di nascita non può essere nel futuro. Per favore, inserisci una data valida.")
                    }
                    date.isBefore(LocalDate.now().minusYears(120)) -> {
                        Log.d("BirthDateValidator", "Date is too far in the past")
                        ValidationResult.Invalid("La data di nascita sembra essere troppo lontana nel passato. Per favore, verifica e correggi se necessario.")
                    }
                    else -> {
                        Log.d("BirthDateValidator", "Date is valid")
                        ValidationResult.Valid
                    }
                }
            } catch (e: DateTimeParseException) {
                Log.e("BirthDateValidator", "DateTimeParseException: ${e.message}")
                ValidationResult.Invalid("Il formato della data non è corretto. Usa il formato GG/MM/AAAA, ad esempio 01/01/1990.")
            } catch (e: Exception) {
                Log.e("BirthDateValidator", "Exception: ${e.message}")
                ValidationResult.Invalid("Si è verificato un errore durante la validazione della data. Assicurati di inserire una data valida nel formato GG/MM/AAAA.")
            }
        }
    }
}