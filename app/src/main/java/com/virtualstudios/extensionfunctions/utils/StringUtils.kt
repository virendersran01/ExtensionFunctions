package com.virtualstudios.extensionfunctions.utils

import java.util.regex.Pattern

val EMAIL_REGEX = Pattern.compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)


fun isEmailValid(emailAddress: String): Boolean {
    return EMAIL_REGEX.matcher(emailAddress).matches()
}

fun isEmailInvalid(emailAddress: String): Boolean {
    return !isEmailValid(emailAddress)
}

fun nonNull(string: String?): String {
    return string ?: ""
}

fun nonNullTrim(string: String?): String {
    return string?.trim { it <= ' ' } ?: ""
}

fun isNotBlank(string: String?): Boolean {
    return !nonNullTrim(string).isEmpty()
}

fun isBlank(string: String?): Boolean {
    return nonNullTrim(string).isEmpty()
}