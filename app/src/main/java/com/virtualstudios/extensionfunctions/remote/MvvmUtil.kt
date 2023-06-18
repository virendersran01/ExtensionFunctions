package com.virtualstudios.extensionfunctions.remote

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.virtualstudios.extensionfunctions.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.io.IOException

/** Start of another way of handling data layer
 * Resource state
 *
 * @param T
 * @constructor Create empty Resource state
 */
sealed interface ResourceState<out T> {
    data class Success<T>(val data: T) : ResourceState<T>
    data class Error(val exception: AppException, val errorCode: String? = null) :
        ResourceState<Nothing>

    data class Loading(val status: Boolean) : ResourceState<Nothing>
}

open class AppException(message: String? = null, cause: Throwable? = null) :
    Throwable(message, cause)

class NetworkException(message: String? = null, cause: Throwable? = null) :
    AppException(message, cause)

class ServerException(message: String? = null, cause: Throwable? = null) :
    AppException(message, cause)

class ClientException(message: String? = null, cause: Throwable? = null) :
    AppException(message, cause)

class UnknownException(message: String? = null, cause: Throwable? = null) :
    AppException(message, cause)


suspend fun <T> asResponseResourceSuspend(apiCall: suspend () -> T): ResourceState<T> {
    return try {
        ResourceState.Loading(true)
        val response = apiCall.invoke()
        ResourceState.Success(response)
    } catch (error: Throwable) {
        val exception = when (error) {
            is HttpException -> {
                when (error.code()) {
                    in 400..499 -> {
                        ClientException(
                            message = "${Constants.CLIENT_ERROR}: ${error.code()}",
                            cause = error,
                        )
                    }

                    in 500..599 -> ServerException(
                        message = "${Constants.SERVER_ERROR}: ${error.code()}",
                        cause = error
                    )

                    else -> UnknownException(
                        message = "${Constants.HTTP_UNKNOWN_ERROR}: ${error.code()}",
                        cause = error
                    )
                }
            }

            is IOException -> NetworkException(
                message = Constants.NETWORK_ERROR,
                cause = error
            )

            else -> AppException(
                message = Constants.UNKNOWN_ERROR,
                cause = error
            )
        }

        val errorCode = when (error) {
            is HttpException -> {
                when (error.code()) {
                    in 400..499 -> {
                        "#ER${error.code()}"
                    }

                    in 500..599 -> {
                        "#ER${error.code()}"
                    }

                    else -> {
                        "#ER${error.code()}"
                    }
                }
            }

            else -> {
                error.cause?.message.toString()
            }
        }
        ResourceState.Error(exception, errorCode)
    } finally {
        ResourceState.Loading(false)
    }
}

public fun <T> Flow<T>.asResponseResourceFlow(): Flow<ResourceState<T>> {
    return this
        .map<T, ResourceState<T>> {
            ResourceState.Success(it)
        }
        .onStart { emit(ResourceState.Loading(true)) }
        .onCompletion { emit(ResourceState.Loading(false)) }
        .catch { error ->
            val exception = when (error) {
                is HttpException -> {
                    when (error.code()) {
                        in 400..499 -> {
                            ClientException(
                                message = "${Constants.CLIENT_ERROR}: ${error.code()}",
                                cause = error,
                            )
                        }

                        in 500..599 -> {
                            ServerException(
                                message = "${Constants.SERVER_ERROR}: ${error.code()}",
                                cause = error
                            )
                        }

                        else -> {
                            UnknownException(
                                message = "${Constants.HTTP_UNKNOWN_ERROR}: ${error.code()}",
                                cause = error
                            )
                        }
                    }
                }

                is IOException -> NetworkException(
                    message = Constants.NETWORK_ERROR,
                    cause = error
                )

                else -> AppException(
                    message = Constants.UNKNOWN_ERROR,
                    cause = error
                )
            }

            val errorCode = when (error) {
                is HttpException -> {
                    when (error.code()) {
                        in 400..499 -> {
                            "#ER${error.code()}"
                        }

                        in 500..599 -> {
                            "#ER${error.code()}"
                        }

                        else -> {
                            "#ER${error.code()}"
                        }
                    }
                }

                else -> {
                    error.cause?.message.toString()
                }
            }
            emit(ResourceState.Error(exception, errorCode))
        }
}

interface ApiService2 {
    @GET("list/{id}")
    suspend fun getResponse(
        @Header("Authorization") token: String,
        @Path(value = "id", encoded = true) id: Int
    ): ApiResponse<Any>
}

class DataSource(private val apiService2: ApiService2){
    suspend fun getResponse(
        token: String,
        id: Int
    ): ResourceState<ApiResponse<Any>> {
        return asResponseResourceSuspend {
            apiService2.getResponse(
                token = token,
                id = id
            )
        }
    }

    fun getResponseFlow(
        token: String,
        id: Int
    ): Flow<ApiResponse<Any>> {
        return flow {
            while (true) {
                val getDetail = apiService2.getResponse(
                    token = token,
                    id = id
                )
                emit(getDetail)
                delay(5000L)
            }
        }
    }
}

interface Repository{
    suspend fun getResponse(id: Int) : ResourceState<ApiResponse<Any>>
    suspend fun getResponseFlow(id: Int) : Flow<ResourceState<ApiResponse<Any>>>
}

class RepositoryImpl(private val dataSource: DataSource) : Repository{
    override suspend fun getResponse(id: Int): ResourceState<ApiResponse<Any>> {
        return dataSource.getResponse(
            token = "",
            id = id
        )
    }

    override suspend fun getResponseFlow(id: Int): Flow<ResourceState<ApiResponse<Any>>> {
        return dataSource.getResponseFlow(
            token = "",
            id = id
        ).asResponseResourceFlow()
    }
}

interface UseCase{
    suspend fun getResponse(id: Int) : ResourceState<ApiResponse<Any>>
    suspend fun getResponseFlow(id: Int) : Flow<ResourceState<ApiResponse<Any>>>
}

class UseCaseImpl(private val repository: Repository): UseCase{
    override suspend fun getResponse(id: Int): ResourceState<ApiResponse<Any>> {
        return repository.getResponse(id)
    }

    override suspend fun getResponseFlow(id: Int): Flow<ResourceState<ApiResponse<Any>>> {
        return repository.getResponseFlow(id)
    }
}

//in UiEvent.kt
sealed interface VisitingUiEvent {
    data class ShowErrorMessageStatic(
        val staticError: String?,
        val dynamicError: String?,
        val errorCode: String?
    ) : VisitingUiEvent
}

//in UiState.kt
sealed interface VisitingDetailUiState {
    object Loading : VisitingDetailUiState
    data class Error(val error: String) : VisitingDetailUiState
    data class Success(
        val detail: Any? = null
    ) : VisitingDetailUiState
}

class TempViewModel : ViewModel(){

    val useCase = UseCaseImpl(RepositoryImpl(DataSource()))

    private val _uiStateDetail =
        MutableStateFlow<VisitingDetailUiState>(VisitingDetailUiState.Loading)
    val uiStateDetail = _uiStateDetail.asStateFlow()

    private val _eventFlow = MutableSharedFlow<VisitingUiEvent>(replay = 1)
    val eventFlow = _eventFlow.asSharedFlow()

    fun getResponse() {
        val visitId = ""//savedStateHandle.get<Int>("visitId")
        viewModelScope.launch {
            val visitDetail = visitId.let { useCase.getResponse(id = it.toInt()) }
            when (visitDetail) {
                is ResourceState.Loading -> {
                    _uiStateDetail.value = VisitingDetailUiState.Loading
                }

                is ResourceState.Success -> {
                    _uiStateDetail.value = VisitingDetailUiState.Success(visitDetail.data.data)
                }

                is ResourceState.Error -> {
                    _eventFlow.emit(
                        VisitingUiEvent.ShowErrorMessageStatic(
                            staticError = visitDetail.exception.message,
                            dynamicError = visitDetail.exception.cause?.message.toString(),
                            errorCode = visitDetail.errorCode
                        )
                    )
                    _uiStateDetail.value =
                        VisitingDetailUiState.Error(error = visitDetail.exception.toString())
                }

                else -> {
                    _uiStateDetail.value =
                        VisitingDetailUiState.Error(error = Constants.UNKNOWN_ERROR)
                }
            }
        }
    }

    fun getResponseFlow() {
        val visitId = "" //savedStateHandle.get<Int>("visitId")
        viewModelScope.launch {
            if (visitId != null) {
                useCase.getResponseFlow(id = visitId.toInt())
                    .collectLatest { visitDetail ->
                        when (visitDetail) {
                            is ResourceState.Loading -> {
                                _uiStateDetail.value = VisitingDetailUiState.Loading
                            }

                            is ResourceState.Success -> {
                                _uiStateDetail.value =
                                    VisitingDetailUiState.Success(visitDetail.data.data)
                            }

                            is ResourceState.Error -> {
                                _eventFlow.emit(
                                    VisitingUiEvent.ShowErrorMessageStatic(
                                        staticError = visitDetail.exception.message,
                                        dynamicError = visitDetail.exception.cause?.message.toString(),
                                        errorCode = visitDetail.errorCode
                                    )
                                )
                                _uiStateDetail.value =
                                    VisitingDetailUiState.Error(error = visitDetail.exception.toString())
                            }
                        }

                    }
            }
        }
    }

    val visitId = 1 //savedStateHandle.get<Int>("visitId")

    val agentDetail: StateFlow<VisitingDetailUiState>? =
        visitId?.let {
            useCase.getResponseFlow(id = it).map { visitDetail ->
                when (visitDetail) {
                    is ResourceState.Loading -> {
                        VisitingDetailUiState.Loading
                    }

                    is ResourceState.Success -> {
                        VisitingDetailUiState.Success(visitDetail.data.data)
                    }

                    is ResourceState.Error -> {
                        _eventFlow.emit(
                            VisitingUiEvent.ShowErrorMessageStatic(
                                staticError = visitDetail.exception.message,
                                dynamicError = visitDetail.exception.cause?.message.toString(),
                                errorCode = visitDetail.errorCode
                            )
                        )
                        VisitingDetailUiState.Error(error = visitDetail.exception.toString())
                    }
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                VisitingDetailUiState.Loading
            )
        }
}


class TempFragment: Fragment(){

    val tempViewModel = TempViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tempViewModel.getResponse()
        observeVisitDetail()
    }


    private fun observeVisitDetail() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tempViewModel.uiStateDetail.collect { visitingUiState ->
                    when (visitingUiState) {
                        is VisitingDetailUiState.Success -> {
                            // HANDLE SUCCESS EVENT
                        }

                        is VisitingDetailUiState.Error -> {
                            tempViewModel.eventFlow.collectLatest {
                                when (it) {
                                    is VisitingUiEvent.ShowErrorMessageStatic -> {
//                                        Timber.tag("MYTAG").d("observeVisitDetail: %s", it.staticError)
//                                        Timber.tag("MYTAG").d("observeVisitDetail: %s", it.dynamicError)
//                                        Timber.tag("MYTAG").d("observeVisitDetail: %s", it.errorCode)
                                    }
                                }
                            }
                        }

                        is VisitingDetailUiState.Loading -> {
                            // HANDLE LOADING EVENT
                        }
                    }
                }
            }
        }
    }
}

/*@Composable
Fun stateFullScreen(){
    // lets say we done handle our viewmodels
    Val uiState by viewmodel.uiState.collectWithLifeCycle()

    LaunchedEffect() {
        viewmodel.getAgentVisitDetailFullResponse()
    }

    stateLessScreen(
        uiState = uiState,
        viewModel = viewmodel
}
}

@Composable
Fun stateLessScreen(
uiState: VisitingDetailUiState,
viewModel: ViewModel
) {
    When (uiState) {
        Is VisitingUIState.Success -> {
        // HANDLE SUCCESS EVENT
    }
        Is VisitingUIState.Loading -> {
        // HANDLE LOADING EVENT
    }
        Is VisitingUIState.Error -> {
        viewModel.eventFlow.collectLatest {
            when (it) {
                is VisitingUiEvent.ShowErrorMessageStatic -> {
                    Timber.tag("MYTAG").d("observeVisitDetail: %s", it.staticError)
                    Timber.tag("MYTAG").d("observeVisitDetail: %s", it.dynamicError)
                    Timber.tag("MYTAG").d("observeVisitDetail: %s", it.errorCode)
                }
            }
        }
    }
  */  }
}