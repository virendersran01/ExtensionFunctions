package com.virtualstudios.extensionfunctions.core.domain

sealed interface ValidationError: Error {
    enum class PasswordError: ValidationError {
        EMPTY,
        TOO_SHORT,
        NO_UPPERCASE,
        NO_LOWERCASE,
        NO_DIGIT,
        NO_SPECIAL_CHAR,
        INVALID
    }
    enum class EmailError: ValidationError {
        EMPTY,
        INVALID
    }

}