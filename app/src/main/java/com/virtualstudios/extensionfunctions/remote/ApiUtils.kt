package com.virtualstudios.extensionfunctions.remote

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName

import com.virtualstudios.extensionfunctions.TokenResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

@Keep
data class ApiResponse<T>(
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
)

@Keep
data class ApiResponsePagination<T>(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("next_page_url")
    val nextPageUrl: String? = null,
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("data")
    val data: List<T> = emptyList(),
    @SerializedName("prev_page_url")
    val prevPageUrl: String? = null,
    @SerializedName("total")
    val total: Int,

    )


@Keep
sealed class ApiCallResult<out T>{
    data class Success<T>(val data: T?) : ApiCallResult<T>()
    data class Error<T>(val exception: kotlin.Exception = Exception(), val errorMessage: String = "") : ApiCallResult<T>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception, message=$errorMessage] "
            is Exception -> "Exception=$e"
        }
    }

    data class Exception(val e: Throwable) : ApiCallResult<Nothing>()
}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}

data class State<out T>(
    val status: Status,
    val data: T?,
    val message: String?
) {

    companion object {

        fun <T> success(msg: String?, data: T?): State<T> = State(Status.SUCCESS, data, msg)
        fun <T> loading(data: T?): State<T> = State(Status.LOADING, data, null)
        fun <T> error(msg: String, data: T?): State<T> = State(Status.ERROR, data, msg)
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}
@Keep
sealed class UiState<out T> {
    object Init : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T?) : UiState<T>()
    data class Error<T>(val exception: Exception = Exception(), val errorMessage: String = "") : UiState<T>()
    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception, message=$errorMessage] "
            is Loading -> "Loading"
            Init -> "initial state for using in StateFlow"
        }
    }

}


@Keep
data class PaginationData<T>(
    val data: List<T>? = null,
    val total: Int,
    val currentPage: Int,
    val nextPageUrl: String? = null
)

suspend fun <T> handleApiCall(apiCall: suspend () -> ApiResponse<T>): ApiCallResult<T> {
    return try {
        val apiResponse = apiCall()
        when(apiResponse.status){
            1 -> ApiCallResult.Success(apiResponse.data)
            else -> ApiCallResult.Error(Exception(), apiResponse.message)
        }
    } catch(e: HttpException) {
        ApiCallResult.Error(e, e.localizedMessage ?: "An unexpected error occurred")
    } catch(e: IOException) {
        ApiCallResult.Error(e,"Couldn't reach server. Check internet connection")
    } catch (exception: Exception) {
        ApiCallResult.Error(exception, exception.message.toString())
    }
}

suspend fun <T> handleApiCallPagination(apiCall: suspend () -> ApiResponsePagination<T>): ApiCallResult<PaginationData<T>> {
    return try {
        val apiResponse = apiCall()
        when(apiResponse.status){
            1 -> ApiCallResult.Success(PaginationData(apiResponse.data, apiResponse.total, apiResponse.currentPage, apiResponse.nextPageUrl))
            else -> ApiCallResult.Error(Exception(), apiResponse.message)
        }
    } catch (exception: Exception) {
        ApiCallResult.Error(exception, exception.message.toString())
    }
}

sealed class Response<out R> {
    data class Success<out T>(val data: T) : Response<T>()
    data class Error(val errorMessage: String, val throwable: Throwable) : Response<Nothing>()
}

suspend fun <T> getResponse(invoke: suspend () -> T): Response<T> {
    return runCatching {
        Response.Success(invoke())
    }.getOrElse {
        Response.Error("Error", it)
    }
}

internal fun  singleSourceOfTruth (
    getLocalData: suspend () -> List<Any>,
    getRemoteData: suspend () -> List<Any>,
    saveDataToLocal: suspend (List<Any>) -> Unit,
): Flow<Response<List<Any>>> = flow {
    val localData = getResponse { getLocalData() }
    if (localData is Response.Success && localData.data.isNotEmpty()) {
        emit(localData)
    } else {
        val remoteData = getResponse { getRemoteData() }
        if (remoteData is Response.Success) {
            if (remoteData.data.isNotEmpty()) {
                saveDataToLocal(remoteData.data)
                val localDataUpdated = getResponse { getLocalData() }
                emit(localDataUpdated)
            }
        } else {
            emit(Response.Error("Error", (remoteData as Response.Error).throwable))
        }
    }
}

sealed class NetworkResponse<out T> {
    data class Loading(
        val isPaginating: Boolean = false,
    ): NetworkResponse<Nothing>()

    data class Success<out T>(
        val data: T,
        val isPaginationData: Boolean = false,
    ): NetworkResponse<T>()

    data class Failure(
        val errorMessage: String,
        val isPaginationError: Boolean = false,
    ): NetworkResponse<Nothing>()
}

sealed class ApiResource<out T> {
    data class Success<out T>(val value: T) : ApiResource<T>()
    data class Error(
        val isNetworkError: Boolean?,
        val errorCode: Int?,
        val errorBody: String?
    ) : ApiResource<Nothing>()

    object Loading : ApiResource<Nothing>()

}

suspend fun <T : Any> handleApiCall(
    apiCall: suspend () -> T,
) : ApiResource<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiCall.invoke()
            ApiResource.Success(response)
        } catch (throwable: Throwable) {
            when(throwable){
                is HttpException -> {
                    ApiResource.Error(false, throwable.code(), throwable.message)
                }
                else -> {
                    ApiResource.Error(true, null, throwable.message)
                }
            }
        }
    }
}


sealed class DataState<out T : Any> {
    data class Success<out T : Any>(val data: T) : DataState<T>()
    data class Error(val errorMessage: String) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
}

//https://github.com/sribanavasi/Handling_Multiple_Api
interface ApiHandler {
    suspend fun <T : Any> handleApi(
        execute: suspend () -> ApiResponse<T>
    ): ApiCallResult<T> {
        return try {
            val apiResponse = execute()
            when(apiResponse.status){
                1 -> ApiCallResult.Success(apiResponse.data)
                else -> ApiCallResult.Error(Exception(), apiResponse.message)
            }
        } catch (e: HttpException) {
            ApiCallResult.Error(e, e.message())
        } catch (e: Throwable) {
            ApiCallResult.Exception(e)
        }
    }
}

class RepositoryI(private val apiService: ApiService): ApiHandler{
    suspend fun getRemoteData(): ApiCallResult<Any> {
        return handleApi { apiService.getRemoteData() }
    }
}

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResources(
        @StringRes val resId: Int,
        vararg val args: Any
    ): UiText()

    fun asString(context: Context): String{
        return when(this){
            is DynamicString -> value
            is StringResources -> context.getString(resId, args)
        }
    }
}

sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: com.virtualstudios.extensionfunctions.remote.Error>(val error: E): Result<Nothing, E>
}

inline fun <T, E: Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when(this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

fun <T, E: Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map {  }
}

inline fun <T, E: Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}
inline fun <T, E: Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}

typealias EmptyResult<E> = Result<Unit, E>

interface Error

enum class NetworkError : Error {
    REQUEST_TIMEOUT,
    UNAUTHORIZED,
    CONFLICT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    PAYLOAD_TOO_LARGE,
    SERVER_ERROR,
    SERIALIZATION,
    UNKNOWN;
}

/*
using ktor
suspend fun censorWords(uncensored: String): Result<String, NetworkError> {
    val response = try {
        httpClient.get(
            urlString = "https://www.purgomalum.com/service/json"
        ) {
            parameter("text", uncensored)
        }
    } catch(e: UnresolvedAddressException) {
        return Result.Error(NetworkError.NO_INTERNET)
    } catch(e: SerializationException) {
        return Result.Error(NetworkError.SERIALIZATION)
    }

    return when(response.status.value) {
        in 200..299 -> {
            val censoredText = response.body<CensoredText>()
            Result.Success(censoredText.result)
        }
        401 -> Result.Error(NetworkError.UNAUTHORIZED)
        409 -> Result.Error(NetworkError.CONFLICT)
        408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
        413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
        in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
        else -> Result.Error(NetworkError.UNKNOWN)
    }
}*/
