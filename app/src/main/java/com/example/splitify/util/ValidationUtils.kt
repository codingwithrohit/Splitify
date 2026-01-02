package com.example.splitify.util

import androidx.room.RoomOpenHelper

object ValidationUtils {

    fun validAmount(amount: String): ValidationResult{
        if(amount.isBlank()){
            return ValidationResult.Error("Amount is required")
        }
        val amountValue = amount.toDoubleOrNull()
        if(amountValue == null){
            return ValidationResult.Error("Invalid amount")
        }

        if(amountValue <= 0){
            return ValidationResult.Error("Amount must be greater than 0")
        }
        if (amountValue > 1000000) {
            return ValidationResult.Error("Amount too large")
        }

        return ValidationResult.Valid
    }

    fun validateDescription(description: String): ValidationResult {
        if (description.isBlank()) {
            return ValidationResult.Error("Description is required")
        }

        if (description.length < 3) {
            return ValidationResult.Error("Description too short (min 3 characters)")
        }

        if (description.length > 100) {
            return ValidationResult.Error("Description too long (max 100 characters)")
        }

        return ValidationResult.Valid
    }

    fun validateTripName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.Error("Trip name is required")
        }

        if (name.length < 2) {
            return ValidationResult.Error("Trip name too short (min 2 characters)")
        }

        if (name.length > 50) {
            return ValidationResult.Error("Trip name too long (max 50 characters)")
        }

        return ValidationResult.Valid
    }

    fun validateMemberName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.Error("Name is required")
        }

        if (name.length < 2) {
            return ValidationResult.Error("Name too short (min 2 characters)")
        }

        if (name.length > 30) {
            return ValidationResult.Error("Name too long (max 30 characters)")
        }

        // Check for valid characters (letters, spaces, hyphens)
        if (!name.matches(Regex("^[a-zA-Z\\s-]+$"))) {
            return ValidationResult.Error("Name can only contain letters, spaces, and hyphens")
        }

        return ValidationResult.Valid
    }
}

sealed class ValidationResult{
    data object Valid: ValidationResult()
    data class Error(val message: String): ValidationResult()

    val isValid: Boolean
        get() = this is Valid

    val errorMessage: String?
        get() = (this as? Error)?.message

}