package com.virtualstudios.extensionfunctions.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    data class Error<T>(val exception: Exception = Exception(), val errorMessage: String = "") : ApiCallResult<T>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception, message=$errorMessage] "
        }
    }

}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
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

suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResponse<T>): ApiCallResult<T> {
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

suspend fun <T> safeApiCallPagination(apiCall: suspend () -> ApiResponsePagination<T>): ApiCallResult<PaginationData<T>> {
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

suspend fun <T : Any> safeApiCall(
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