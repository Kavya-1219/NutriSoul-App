package com.simats.nutrisoul

import android.util.Patterns

object Validation {

    fun isEmailValid(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    /**
     * Professional Password Rules:
     * - At least 8 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 digit
     * - At least 1 special character
     */
    fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val specialChars = "!@#$%^&*"
        val hasSpecial = password.any { it in specialChars }
        return hasUpper && hasLower && hasDigit && hasSpecial
    }

    fun passwordHint(): String =
        "Password must be at least 8 characters and include uppercase, lowercase, a number, and a special character."
}
