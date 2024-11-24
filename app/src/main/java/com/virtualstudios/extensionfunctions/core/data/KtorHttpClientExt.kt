package com.virtualstudios.extensionfunctions.core.data

import com.virtualstudios.extensionfunctions.core.domain.NetworkError
import com.virtualstudios.extensionfunctions.core.domain.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlin.coroutines.coroutineContext

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): Result<T, NetworkError>{
    val response = try{
        execute()
    }catch (e: SocketTimeoutException){
        return Result.Error(NetworkError.REQUEST_TIMEOUT)
    }catch (e: SerializationException){
        return Result.Error(NetworkError.SERIALIZATION)
    } catch (e: UnresolvedAddressException){
        return Result.Error(NetworkError.NO_INTERNET)
    } catch (e: Exception){
        coroutineContext.ensureActive()
        return Result.Error(NetworkError.UNKNOWN)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(
    httpResponse: HttpResponse
): Result<T, NetworkError> {
    return when(httpResponse.status.value) {
        in 200..299 -> {
            try {
                Result.Success(httpResponse.body<T>())
            }catch (e: NoTransformationFoundException){
                Result.Error(NetworkError.SERIALIZATION)
            }
        }
        408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
        429 -> Result.Error(NetworkError.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
        else -> Result.Error(NetworkError.UNKNOWN)
    }
}