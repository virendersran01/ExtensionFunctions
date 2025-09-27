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


sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    object Empty : UiState<Nothing>
    data class Success<T>(val data: T,  val message: String? = null) : UiState<T>
    data class Error(
        val message: String? = null,
        val throwable: Throwable? = null,
        val exception: Throwable? = null,
        val errorType: ErrorType = ErrorType.GENERIC
    ) : UiState<Nothing>

    enum class ErrorType {
        NETWORK,
        AUTH,
        VALIDATION,
        SERVER,
        GENERIC
    }
}

// Extension functions for easier UiState handling
inline fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) action()
    return this
}

inline fun <T> UiState<T>.onSuccess(action: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) action(data)
    return this
}

inline fun <T> UiState<T>.onError(action: (UiState.Error) -> Unit): UiState<T> {
    if (this is UiState.Error) action(this)
    return this
}

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline save: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
): Flow<ResultType> = flow {
    // 1) Observe local data first
    val data = query().first()
    emit(data)
    // 2) Decide if remote fetch is needed
    if (shouldFetch(data)) {
        try {
            val apiResponse =
                fetch()                              // :contentReference[oaicite:5]{index=5}
            save(apiResponse)                                      // :contentReference[oaicite:6]{index=6}
        } catch (_: Exception) { /* handle error */
        }
    }
    // 3) Emit updated local data
    emitAll(query())
}

fun <T> networkBoundResourceWithApiResult(
    query: () -> Flow<T>,
    fetch: suspend () -> ApiResult<T>,
    saveFetchResult: suspend (T) -> Unit,
    shouldFetch: (T) -> Boolean = { true }
): Flow<UiState<T>> = flow {
    emit(UiState.Loading)

    query().collect { data ->
        if (shouldFetch(data)) {
            when (val result = fetch()) {
                is ApiResult.Success -> {
                    result.data?.let { fetchedData ->
                        try {
                            saveFetchResult(fetchedData)
                        } catch (e: Exception) {
                            emit(UiState.Error("Failed to save: ${e.message}", e))
                            return@collect
                        }
                    }
                }
                is ApiResult.Error -> {
                    emit(UiState.Error(result.errorMessage, result.exception))
                    return@collect
                }
            }
        }
        emit(UiState.Success(data))
    }
}

// Option 1: Modified networkBoundResource that handles ApiResult internally
inline fun <ResultType, RequestType> networkBoundResourceWithApiResult(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> ApiResult<RequestType>,
    crossinline save: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
    crossinline onError: suspend (ApiResult.Error) -> Unit = { }
): Flow<ResultType> = flow {
    // 1) Observe local data first
    val data = query().first()
    emit(data)

    // 2) Decide if remote fetch is needed
    if (shouldFetch(data)) {
        when (val apiResult = fetch()) {
            is ApiResult.Success -> {
                apiResult.data?.let { responseData ->
                    try {
                        save(responseData)
                    } catch (e: Exception) {
                        // Handle save error silently or log it
                        logDebug("Failed to save data: ${e.message}")
                    }
                }
            }
            is ApiResult.Error -> {
                // Handle error (log, analytics, etc.)
                onError(apiResult)
                logDebug("API Error: ${apiResult.errorMessage}")
            }
        }
    }

    // 3) Emit updated local data
    emitAll(query())
}

// Option 2: Extension function on ApiResult for easier integration
suspend inline fun <T> ApiResult<T>.onSuccessData(
    crossinline action: suspend (T) -> Unit
): ApiResult<T> {
    if (this is ApiResult.Success && data != null) {
        action(data)
    }
    return this
}

// Option 3: Simple wrapper that converts ApiResult to regular result
inline fun <ResultType, RequestType> networkBoundResourceSimple(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetchWithApiResult: suspend () -> ApiResult<RequestType>,
    crossinline save: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
): Flow<ResultType> = flow {
    // 1) Observe local data first
    val data = query().first()
    emit(data)

    // 2) Decide if remote fetch is needed
    if (shouldFetch(data)) {
        try {
            // Convert ApiResult to exception-based approach
            val apiResponse = when (val result = fetchWithApiResult()) {
                is ApiResult.Success -> result.data ?: throw Exception("Data is null")
                is ApiResult.Error -> throw result.exception
            }
            save(apiResponse)
        } catch (e: Exception) {
            // Handle error silently
            logDebug("Network fetch failed: ${e.message}")
        }
    }

    // 3) Emit updated local data
    emitAll(query())
}

// Updated networkBoundResource with mapping support
inline fun <DomainType, EntityType, RequestType> networkBoundResourceWithMappers(
    crossinline query: () -> Flow<List<EntityType>>, // Room query returning entities
    crossinline fetch: suspend () -> ApiResult<List<RequestType>>, // API call returning API models
    crossinline mapApiToEntity: (List<RequestType>) -> List<EntityType>, // API to Entity mapper
    crossinline mapEntityToDomain: (List<EntityType>) -> List<DomainType>, // Entity to Domain mapper
    crossinline save: suspend (List<EntityType>) -> Unit, // Save entities to Room
    crossinline shouldFetch: (List<EntityType>) -> Boolean = { true }
): Flow<UiState<List<DomainType>>> = flow {

    emit(UiState.Loading)

    try {
        // Get initial data from local source (entities)
        val localEntities = query().first()

        // Convert entities to domain models and emit if available
        if (localEntities.isNotEmpty()) {
            val domainModels = mapEntityToDomain(localEntities)
            emit(UiState.Success(domainModels))
        }

        // Decide if we need to fetch from network
        if (shouldFetch(localEntities)) {
            when (val apiResult = fetch()) {
                is ApiResult.Success -> {
                    apiResult.data?.let { apiModels ->
                        try {
                            // Map API models to entities
                            val entities = mapApiToEntity(apiModels)

                            // Save entities to Room
                            save(entities)

                            // Get fresh data from Room and convert to domain
                            val updatedEntities = query().first()
                            val updatedDomainModels = mapEntityToDomain(updatedEntities)

                            emit(UiState.Success(
                                data = updatedDomainModels,
                                message = apiResult.successMessage
                            ))
                        } catch (saveException: Exception) {
                            emit(UiState.Error(
                                message = "Failed to cache data: ${saveException.message}",
                                exception = saveException
                            ))
                        }
                    } ?: run {
                        emit(UiState.Error(
                            message = "No data received from server",
                            errorType = UiState.ErrorType.SERVER
                        ))
                    }
                }
                is ApiResult.Error -> {
                    val errorType = when (apiResult.exception) {
                        is InvalidAuthorization -> UiState.ErrorType.AUTH
                        is java.net.UnknownHostException,
                        is java.net.SocketTimeoutException,
                        is java.io.IOException -> UiState.ErrorType.NETWORK
                        else -> UiState.ErrorType.SERVER
                    }

                    // If we have cached data, show it with error message
                    if (localEntities.isNotEmpty()) {
                        val cachedDomainModels = mapEntityToDomain(localEntities)
                        emit(UiState.Success(
                            data = cachedDomainModels,
                            message = "Using cached data - ${apiResult.errorMessage}"
                        ))
                    } else {
                        emit(UiState.Error(
                            message = apiResult.errorMessage.ifBlank { "Network request failed" },
                            exception = apiResult.exception,
                            errorType = errorType
                        ))
                    }
                }
            }
        }

        // Continue to observe local data for any future changes
        emitAll(
            query().map { entities ->
                val domainModels = mapEntityToDomain(entities)
                UiState.Success(domainModels)
            }
        )

    } catch (exception: Exception) {
        emit(UiState.Error(
            message = exception.message ?: "Unexpected error occurred",
            exception = exception
        ))
    }
}

// Single item version
inline fun <DomainType, EntityType, RequestType> networkBoundResourceSingleWithMappers(
    crossinline query: () -> Flow<EntityType?>, // Room query returning single entity
    crossinline fetch: suspend () -> ApiResult<RequestType>, // API call returning API model
    crossinline mapApiToEntity: (RequestType) -> EntityType, // API to Entity mapper
    crossinline mapEntityToDomain: (EntityType?) -> DomainType?, // Entity to Domain mapper
    crossinline save: suspend (EntityType) -> Unit, // Save entity to Room
    crossinline shouldFetch: (EntityType?) -> Boolean = { true }
): Flow<UiState<DomainType?>> = flow {

    emit(UiState.Loading)

    try {
        // Get initial data from local source
        val localEntity = query().first()

        // Convert entity to domain model and emit if available
        localEntity?.let {
            val domainModel = mapEntityToDomain(it)
            emit(UiState.Success(domainModel))
        }

        // Decide if we need to fetch from network
        if (shouldFetch(localEntity)) {
            when (val apiResult = fetch()) {
                is ApiResult.Success -> {
                    apiResult.data?.let { apiModel ->
                        try {
                            // Map API model to entity
                            val entity = mapApiToEntity(apiModel)

                            // Save entity to Room
                            save(entity)

                            // Get fresh data from Room and convert to domain
                            val updatedEntity = query().first()
                            val updatedDomainModel = mapEntityToDomain(updatedEntity)

                            emit(
                                UiState.Success(
                                    data = updatedDomainModel,
                                    message = apiResult.successMessage
                                )
                            )
                        } catch (saveException: Exception) {
                            emit(
                                UiState.Error(
                                    message = "Failed to cache data: ${saveException.message}",
                                    exception = saveException
                                )
                            )
                        }
                    }
                }

                is ApiResult.Error -> {
                    val errorType = when (apiResult.exception) {
                        is InvalidAuthorization -> UiState.ErrorType.AUTH
                        is java.net.UnknownHostException -> UiState.ErrorType.NETWORK
                        else -> UiState.ErrorType.SERVER
                    }

                    // If we have cached data, show it with error message
                    localEntity?.let {
                        val cachedDomainModel = mapEntityToDomain(it)
                        emit(
                            UiState.Success(
                                data = cachedDomainModel,
                                message = "Using cached data - ${apiResult.errorMessage}"
                            )
                        )
                    } ?: emit(
                        UiState.Error(
                            message = apiResult.errorMessage,
                            exception = apiResult.exception,
                            //errorType = errorType
                        )
                    )
                }
            }
        }

        // Continue to observe local data for any future changes
        emitAll(
            query().map { entity ->
                val domainModel = mapEntityToDomain(entity)
                UiState.Success(domainModel)
            }
        )

    } catch (exception: Exception) {
        emit(
            UiState.Error(
                message = exception.message ?: "Unexpected error occurred",
                exception = exception
            )
        )
    }

}