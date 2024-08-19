import android.view.View
import android.widget.TextView
import com.example.hammami.util.StringValidators
import com.example.hammami.util.ValidationResult
import com.example.hammami.util.Validator
import com.google.android.material.textfield.TextInputLayout


// Classe di utilità per la convalida
object ValidationUtil {
    fun validateField(field: TextInputLayout, validator: Validator<String>): Boolean {
        val value = field.editText?.text.toString()
        return when (val result = validator.validate(value)) {
            is ValidationResult.Valid -> {
                field.error = null
                true
            }

            is ValidationResult.Invalid -> {
                field.error = result.errorMessage
                false
            }
        }
    }

    fun validateBirthDate(
        dayField: TextInputLayout,
        monthField: TextInputLayout,
        yearField: TextInputLayout,
        errorTextView: TextView
    ): Boolean {
        val day = dayField.editText?.text.toString()
        val month = monthField.editText?.text.toString()
        val year = yearField.editText?.text.toString()





        return when (val result = StringValidators.BirthDate.validate(Triple(day, month, year))) {
            is ValidationResult.Valid -> {
                errorTextView.visibility = View.GONE
                dayField.error = null
                monthField.error = null
                yearField.error = null
                true
            }

            is ValidationResult.Invalid -> {
                errorTextView.apply {
                    text = result.errorMessage
                    visibility = View.VISIBLE
                }
                dayField.error = ""
                monthField.error = ""
                yearField.error = ""
                false
            }

        }
    }

    fun validatePasswords(password: String, confirmPassword: String): ValidationResult {
        return when {
            password.isEmpty() || confirmPassword.isEmpty() ->
                ValidationResult.Invalid("I campi password non possono essere vuoti")
            password != confirmPassword ->
                ValidationResult.Invalid("Le password non corrispondono. Per favore, riprova.")
            !StringValidators.Password.validate(password).isValid() ->
                ValidationResult.Invalid("La password non soddisfa i requisiti di sicurezza. La password deve essere lunga almeno 8 caratteri e contenere almeno una lettera e un numero")
            else -> ValidationResult.Valid
        }
    }

    private fun ValidationResult.isValid() = this is ValidationResult.Valid

}








