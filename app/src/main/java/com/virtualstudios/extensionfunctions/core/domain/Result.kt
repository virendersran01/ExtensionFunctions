package com.virtualstudios.extensionfunctions.core.domain

typealias DomainError = Error

sealed interface Result<out D, out E: Error>{
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E: DomainError>(val error: E) : Result<Nothing, E>
}

inline fun <T, E: DomainError, R>Result<T, E>.map(
    map: (T) -> R
) : Result<R, E>{
    return when(this){
        is Result.Success -> Result.Success(map(data))
        is Result.Error -> Result.Error(error)
    }
}

typealias EmptyResult<E> = Result<Unit, E>

fun <T, E: DomainError> Result<T, E>.asEmptyResultData(): EmptyResult<E> {
    return map {

    }
}

fun <T, E: DomainError> Result<T, E>.onSuccess(
    action: (T) -> Unit
) : Result<T, E> {
    return when(this){
        is Result.Success -> {
            action(data)
            this
        }
        is Result.Error -> this
    }
}

fun <T, E: DomainError> Result<T, E>.onError(
    action: (E) -> Unit
): Result<T, E> {
    return when(this){
        is Result.Success -> this
        is Result.Error -> {
            action(error)
            this
        }
    }
}