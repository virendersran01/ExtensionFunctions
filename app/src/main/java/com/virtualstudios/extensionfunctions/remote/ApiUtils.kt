package com.virtualstudios.extensionfunctions.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

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
@Keep
sealed class UiState<out T> {
    class Loading<T> : UiState<T>()
    data class Success<T>(val data: T?) : UiState<T>()
    data class Error<T>(val exception: Exception = Exception(), val errorMessage: String = "") : UiState<T>()
    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception, message=$errorMessage] "
            is Loading -> "Loading"
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
